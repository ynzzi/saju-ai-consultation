package com.portfolio.saju.profile.dto;

import com.portfolio.saju.profile.domain.CalendarType;
import com.portfolio.saju.profile.domain.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateSajuProfileRequest(
        @NotBlank @Size(max = 60) String profileName,
        @NotNull LocalDate birthDate,
        @NotNull LocalTime birthTime,
        @NotNull CalendarType calendarType,
        @NotNull Gender gender,
        @Size(max = 120) String birthPlace,
        Boolean leapMonth
) {
    public boolean leapMonthValue() {
        return Boolean.TRUE.equals(leapMonth);
    }
}
