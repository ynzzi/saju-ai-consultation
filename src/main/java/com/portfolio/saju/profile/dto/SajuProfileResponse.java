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
        String analysisSummary,
        String elementSummary,
        List<String> strengths,
        List<String> cautions,
        List<String> recommendedQuestions,
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
                profile.getAnalysisSummary(),
                profile.getElementSummary(),
                split(profile.getStrengths()),
                split(profile.getCautions()),
                split(profile.getRecommendedQuestions()),
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
