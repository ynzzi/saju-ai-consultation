package com.portfolio.saju.profile.manse;

import java.util.Map;

public record ManseCalendarResult(
        String yearPillar,
        String monthPillar,
        String dayPillar,
        String hourPillar,
        Map<String, Integer> fiveElements,
        Map<String, Integer> yinYang,
        String calculationStandard,
        String warning,
        String manseCalendarVersion
) {
}
