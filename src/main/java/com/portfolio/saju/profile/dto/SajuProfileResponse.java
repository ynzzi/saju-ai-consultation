package com.portfolio.saju.profile.dto;

import com.portfolio.saju.profile.domain.CalendarType;
import com.portfolio.saju.profile.domain.Gender;
import com.portfolio.saju.profile.domain.SajuProfile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record SajuProfileResponse(
        Long id,
        String profileName,
        LocalDate birthDate,
        LocalTime birthTime,
        CalendarType calendarType,
        Gender gender,
        String birthPlace,
        Boolean leapMonth,
        String analysisSummary,
        String elementSummary,
        List<String> strengths,
        List<String> cautions,
        List<String> recommendedQuestions,
        String yearPillar,
        String monthPillar,
        String dayPillar,
        String hourPillar,
        List<String> fiveElementsSummary,
        List<String> yinYangSummary,
        String calculationStandard,
        String calculationWarning,
        String manseCalendarVersion,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static SajuProfileResponse from(SajuProfile profile) {
        return new SajuProfileResponse(
                profile.getId(),
                profile.getProfileName(),
                profile.getBirthDate(),
                profile.getBirthTime(),
                profile.getCalendarType(),
                profile.getGender(),
                profile.getBirthPlace(),
                Boolean.TRUE.equals(profile.getLeapMonth()),
                profile.getAnalysisSummary(),
                profile.getElementSummary(),
                split(profile.getStrengths()),
                split(profile.getCautions()),
                split(profile.getRecommendedQuestions()),
                profile.getYearPillar(),
                profile.getMonthPillar(),
                profile.getDayPillar(),
                profile.getHourPillar(),
                split(profile.getFiveElementsSummary()),
                split(profile.getYinYangSummary()),
                profile.getCalculationStandard(),
                profile.getCalculationWarning(),
                profile.getManseCalendarVersion(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private static List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split("\\n"));
    }
}
