package com.portfolio.saju.profile.dto;

import java.util.List;

public record SajuAnalysisResult(
        String analysisSummary,
        String elementSummary,
        List<String> strengths,
        List<String> cautions,
        List<String> recommendedQuestions
) {
}
