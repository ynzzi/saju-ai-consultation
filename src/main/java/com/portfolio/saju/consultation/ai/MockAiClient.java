package com.portfolio.saju.consultation.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiClient implements AiClient {

    @Override
    public String generateAnswer(AiPrompt prompt) {
        return """
                [Mock AI 상담 답변]
                질문을 보면 지금은 방향을 단정하기보다 기준을 정리하는 단계가 좋아 보입니다.
                선택한 프로필의 사주팔자와 오행/음양 분포는 참고용 기본 계산 기준으로 함께 반영했습니다.
                프로필 분석에서 드러난 강점은 꾸준함과 관계 속 신뢰이며, 이를 바탕으로 작은 선택부터 검증해보세요.
                참고 질문: "%s"
                """.formatted(prompt.question()).trim();
    }
}
