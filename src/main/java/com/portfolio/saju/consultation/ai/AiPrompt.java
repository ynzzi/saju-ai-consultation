package com.portfolio.saju.consultation.ai;

public record AiPrompt(
        String question,
        String profileContext,
        String analysisContext,
        String previousConsultations
) {
}
