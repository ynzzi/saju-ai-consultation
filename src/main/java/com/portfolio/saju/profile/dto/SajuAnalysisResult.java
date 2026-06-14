package com.portfolio.saju.profile.dto;

import java.util.List;
import java.util.Map;

public record SajuAnalysisResult(
        String analysisSummary,
        String elementSummary,
        List<String> strengths,
        List<String> cautions,
        List<String> recommendedQuestions,
        Boolean leapMonth,
        String yearPillar,
        String monthPillar,
        String dayPillar,
        String hourPillar,
        Map<String, Integer> fiveElements,
        Map<String, Integer> yinYang,
        String calculationStandard,
        String calculationWarning,
        String manseCalendarVersion
) {
}
