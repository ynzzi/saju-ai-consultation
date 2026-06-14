package com.portfolio.saju.profile.service;

import com.portfolio.saju.profile.domain.CalendarType;
import com.portfolio.saju.profile.domain.Gender;
import com.portfolio.saju.profile.dto.SajuAnalysisResult;
import java.time.LocalDate;
import java.time.LocalTime;

public interface SajuAnalysisService {

    SajuAnalysisResult analyze(
            LocalDate birthDate,
            LocalTime birthTime,
            CalendarType calendarType,
            Gender gender,
            boolean leapMonth
    );
}
