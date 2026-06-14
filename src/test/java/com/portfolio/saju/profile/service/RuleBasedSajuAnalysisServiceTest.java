package com.portfolio.saju.profile.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.portfolio.saju.profile.domain.CalendarType;
import com.portfolio.saju.profile.domain.Gender;
import com.portfolio.saju.profile.manse.ManseCalendarService;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class RuleBasedSajuAnalysisServiceTest {

    @Test
    void cacheKeyContainsManseVersionAndLeapMonth() {
        RuleBasedSajuAnalysisService service = new RuleBasedSajuAnalysisService(null, null, null);

        String key = service.cacheKey(
                LocalDate.of(1998, 3, 15),
                LocalTime.of(9, 30),
                CalendarType.LUNAR,
                true,
                Gender.FEMALE,
                ManseCalendarService.VERSION
        );

        assertThat(key).startsWith("saju:analysis:v3:manse:");
        assertThat(key).contains("1998-03-15");
        assertThat(key).contains("09:30");
        assertThat(key).contains("LUNAR");
        assertThat(key).contains("true");
        assertThat(key).contains(ManseCalendarService.VERSION);
    }
}
