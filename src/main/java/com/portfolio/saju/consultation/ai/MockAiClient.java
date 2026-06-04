package com.portfolio.saju.consultation.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiClient implements AiClient {

    @Override
    public String generateAnswer(AiPrompt prompt) {
        return """
                [Mock AI 상담 답변]
                질문을 보면 지금은 방향을 단정하기보다 기준을 정리하는 단계가 좋아 보입니다.
                프로필 분석에서 드러난 강점은 꾸준함과 관계 속 신뢰이며, 이를 바탕으로 작은 선택부터 검증해보세요.
                참고 질문: "%s"
                """.formatted(prompt.question()).trim();
    }
}
