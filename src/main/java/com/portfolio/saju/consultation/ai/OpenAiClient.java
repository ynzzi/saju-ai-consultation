package com.portfolio.saju.consultation.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.portfolio.saju.common.exception.BusinessException;
import com.portfolio.saju.common.exception.ErrorCode;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAiClient implements AiClient {

    private final ObjectMapper objectMapper;

    @Value("${ai.openai.api-key}")
    private String apiKey;

    @Value("${ai.openai.model}")
    private String model;

    @Override
    public String generateAnswer(AiPrompt prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "OPENAI_API_KEY is missing");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", model);
            ArrayNode messages = body.putArray("messages");
            messages.addObject()
                    .put("role", "system")
                    .put("content", "너는 사주 분석 정보를 참고해 현실적인 조언을 제공하는 한국어 상담 도우미야. 단정적 예언이나 의학/법률/투자 확답은 피한다.");
            messages.addObject()
                    .put("role", "user")
                    .put("content", buildContent(prompt));

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(
                    URI.create("https://api.openai.com/v1/chat/completions"),
                    new HttpEntity<>(body.toString(), headers),
                    String.class
            );
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").path(0).path("message").path("content").asText();
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "AI response generation failed");
        }
    }

    private String buildContent(AiPrompt prompt) {
        return """
                사용자 질문:
                %s

                선택된 사주 프로필:
                %s

                기본 분석 결과:
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
