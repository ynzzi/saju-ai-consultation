package com.portfolio.saju.consultation.ai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.portfolio.saju.common.exception.BusinessException;
import com.portfolio.saju.common.exception.ErrorCode;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai")
public class OpenAiClient implements AiClient {

    private static final String USER_SAFE_ERROR_MESSAGE = "AI 답변 생성 중 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";

    private final String apiKey;
    private final String model;

    public OpenAiClient(
            @Value("${openai.api-key:}") String apiKey,
            @Value("${app.ai.model:gpt-5.5}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String generateAnswer(AiPrompt prompt) {
        if (!StringUtils.hasText(apiKey)) {
            log.warn("OpenAI provider is enabled but OPENAI_API_KEY is missing.");
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, USER_SAFE_ERROR_MESSAGE);
        }

        try {
            OpenAIClient client = OpenAIOkHttpClient.builder()
                    .apiKey(apiKey)
                    .build();
            ResponseCreateParams params = ResponseCreateParams.builder()
                    .model(model)
                    .instructions(systemInstructions())
                    .input(buildInput(prompt))
                    .build();

            Response response = client.responses().create(params);
            String outputText = extractOutputText(response);
            if (!StringUtils.hasText(outputText)) {
                log.warn("OpenAI response did not contain output text. model={}", model);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, USER_SAFE_ERROR_MESSAGE);
            }
            return outputText.trim();
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("OpenAI API call failed. model={}, error={}", model, exception.getMessage(), exception);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, USER_SAFE_ERROR_MESSAGE);
        }
    }

    private String systemInstructions() {
        return """
                너는 사주 분석 정보를 참고해 현실적인 조언을 제공하는 한국어 상담 도우미다.
                사주/운세 해석은 참고용 관점으로 표현하고 단정적인 예언은 피한다.
                사용자의 선택을 강요하지 말고, 건강/투자/법률 등 고위험 영역은 전문 상담을 대체하지 않는다고 안내한다.
                프로필 분석 결과를 바탕으로 답하되, 현실적인 행동 조언을 포함한다.
                답변은 4~8문장 정도로 간결하게 작성한다.
                """;
    }

    private String extractOutputText(Response response) {
        return response.output().stream()
                .filter(ResponseOutputItem::isMessage)
                .map(ResponseOutputItem::asMessage)
                .map(ResponseOutputMessage::content)
                .flatMap(contents -> contents.stream()
                        .filter(ResponseOutputMessage.Content::isOutputText)
                        .map(content -> content.asOutputText().text()))
                .collect(Collectors.joining("\n"))
                .trim();
    }

    private String buildInput(AiPrompt prompt) {
        return """
                사용자 질문:
                %s

                선택된 사주 프로필:
                %s

                사주 풀이 결과:
                %s

                이전 상담 일부:
                %s
                """.formatted(
                prompt.question(),
                prompt.profileContext(),
                prompt.analysisContext(),
                prompt.previousConsultations()
        );
    }
}
