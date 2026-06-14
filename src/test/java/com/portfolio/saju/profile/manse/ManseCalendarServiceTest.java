package com.portfolio.saju.profile.manse;

import static org.assertj.core.api.Assertions.assertThat;

import com.portfolio.saju.profile.domain.CalendarType;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ManseCalendarServiceTest {

    private final ManseCalendarService service = new ManseCalendarService();

    @Test
    void calculateSolarProfile() {
        ManseCalendarResult result = service.calculate(
                LocalDate.of(1998, 3, 15),
                LocalTime.of(9, 30),
                CalendarType.SOLAR,
                false
        );

        assertThat(result.yearPillar()).isNotBlank();
        assertThat(result.monthPillar()).isNotBlank();
        assertThat(result.dayPillar()).isNotBlank();
        assertThat(result.hourPillar()).isNotBlank();
        assertThat(result.fiveElements().values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(8);
        assertThat(result.yinYang().values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(8);
    }

    @Test
    void calculateLunarProfile() {
        ManseCalendarResult result = service.calculate(
                LocalDate.of(1998, 2, 17),
                LocalTime.of(23, 10),
                CalendarType.LUNAR,
                false
        );

        assertThat(result.yearPillar()).isNotBlank();
        assertThat(result.warning()).contains("음력 입력");
    }

    @Test
    void calculateLeapMonthProfile() {
        ManseCalendarResult result = service.calculate(
                LocalDate.of(2023, 2, 15),
                LocalTime.of(8, 0),
                CalendarType.LUNAR,
                true
        );

        assertThat(result.hourPillar()).isNotBlank();
        assertThat(result.warning()).contains("윤달");
    }
}
