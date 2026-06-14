package com.portfolio.saju.profile.manse;

import com.portfolio.saju.profile.domain.CalendarType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ManseCalendarService {

    public static final String VERSION = "manse-v1-mvp";

    // Current scope: lunar-to-solar conversion and exact solar-term timestamps are documented limitations.
    private static final String[] STEMS = {"갑", "을", "병", "정", "무", "기", "경", "신", "임", "계"};
    private static final String[] BRANCHES = {"자", "축", "인", "묘", "진", "사", "오", "미", "신", "유", "술", "해"};
    private static final String[] STEM_ELEMENTS = {"목", "목", "화", "화", "토", "토", "금", "금", "수", "수"};
    private static final String[] BRANCH_ELEMENTS = {"수", "토", "목", "목", "토", "화", "화", "토", "금", "금", "토", "수"};
    private static final LocalDate SUPPORTED_FROM = LocalDate.of(1900, 1, 1);
    private static final LocalDate SUPPORTED_TO = LocalDate.of(2099, 12, 31);
    private static final LocalDate DAY_PILLAR_ANCHOR = LocalDate.of(1900, 1, 31);

    public ManseCalendarResult calculate(
            LocalDate birthDate,
            LocalTime birthTime,
            CalendarType calendarType,
            boolean leapMonth
    ) {
        validate(birthDate, birthTime, calendarType, leapMonth);

        LocalDate calculationDate = birthDate;
        String warning = warning(calendarType, leapMonth);
        int yearStemIndex = yearStemIndex(calculationDate);
        int monthStemIndex = monthStemIndex(yearStemIndex, calculationDate);
        int monthBranchIndex = monthBranchIndex(calculationDate);
        int dayStemIndex = dayStemIndex(calculationDate);
        int dayBranchIndex = dayBranchIndex(calculationDate);
        int hourBranchIndex = hourBranchIndex(birthTime);
        int hourStemIndex = hourStemIndex(dayStemIndex, hourBranchIndex);

        Map<String, Integer> fiveElements = newCountMap("목", "화", "토", "금", "수");
        addElement(fiveElements, STEM_ELEMENTS[yearStemIndex], BRANCH_ELEMENTS[yearBranchIndex(calculationDate)]);
        addElement(fiveElements, STEM_ELEMENTS[monthStemIndex], BRANCH_ELEMENTS[monthBranchIndex]);
        addElement(fiveElements, STEM_ELEMENTS[dayStemIndex], BRANCH_ELEMENTS[dayBranchIndex]);
        addElement(fiveElements, STEM_ELEMENTS[hourStemIndex], BRANCH_ELEMENTS[hourBranchIndex]);

        Map<String, Integer> yinYang = newCountMap("양", "음");
        addYinYang(yinYang, yearStemIndex, yearBranchIndex(calculationDate));
        addYinYang(yinYang, monthStemIndex, monthBranchIndex);
        addYinYang(yinYang, dayStemIndex, dayBranchIndex);
        addYinYang(yinYang, hourStemIndex, hourBranchIndex);

        return new ManseCalendarResult(
                pillar(yearStemIndex, yearBranchIndex(calculationDate)),
                pillar(monthStemIndex, monthBranchIndex),
                pillar(dayStemIndex, dayBranchIndex),
                pillar(hourStemIndex, hourBranchIndex),
                fiveElements,
                yinYang,
                "기본 계산 기준: 한국 표준시, 입춘 기준 년주, 절기 경계 근사 월주, 1900-01-31 갑진 기준 일주, 야자시 미적용",
                warning,
                VERSION
        );
    }

    private void validate(LocalDate birthDate, LocalTime birthTime, CalendarType calendarType, boolean leapMonth) {
        if (birthDate == null || birthTime == null || calendarType == null) {
            throw new IllegalArgumentException("생년월일, 출생시간, 달력 유형은 필수입니다.");
        }
        if (birthDate.isBefore(SUPPORTED_FROM) || birthDate.isAfter(SUPPORTED_TO)) {
            throw new IllegalArgumentException("기본 계산은 1900-01-01부터 2099-12-31까지만 지원합니다.");
        }
        if (calendarType == CalendarType.SOLAR && leapMonth) {
            throw new IllegalArgumentException("윤달은 음력 입력에서만 선택할 수 있습니다.");
        }
    }

    private String warning(CalendarType calendarType, boolean leapMonth) {
        if (calendarType == CalendarType.LUNAR) {
            return leapMonth
                    ? "음력 윤달 입력은 별도 양력 변환 라이브러리 없이 입력일을 계산 기준일로 사용합니다."
                    : "음력 입력은 별도 양력 변환 라이브러리 없이 입력일을 계산 기준일로 사용합니다.";
        }
        return "참고: 현재 풀이는 기본 계산 기준으로 제공되며, 전문 감정과 차이가 있을 수 있습니다.";
    }

    private int yearStemIndex(LocalDate date) {
        return Math.floorMod(adjustedYear(date) - 1984, 10);
    }

    private int yearBranchIndex(LocalDate date) {
        return Math.floorMod(adjustedYear(date) - 1984, 12);
    }

    private int adjustedYear(LocalDate date) {
        LocalDate ipchun = LocalDate.of(date.getYear(), 2, 4);
        return date.isBefore(ipchun) ? date.getYear() - 1 : date.getYear();
    }

    private int monthBranchIndex(LocalDate date) {
        return Math.floorMod(2 + monthOffset(date), 12);
    }

    private int monthStemIndex(int yearStemIndex, LocalDate date) {
        int firstMonthStemIndex = switch (yearStemIndex) {
            case 0, 5 -> 2;
            case 1, 6 -> 4;
            case 2, 7 -> 6;
            case 3, 8 -> 8;
            default -> 0;
        };
        return Math.floorMod(firstMonthStemIndex + monthOffset(date), 10);
    }

    private int monthOffset(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        if (month == 1) {
            return day >= 6 ? 11 : 10;
        }
        if (month == 2) {
            return day >= 4 ? 0 : 11;
        }
        if (month == 3) {
            return day >= 6 ? 1 : 0;
        }
        if (month == 4) {
            return day >= 5 ? 2 : 1;
        }
        if (month == 5) {
            return day >= 6 ? 3 : 2;
        }
        if (month == 6) {
            return day >= 6 ? 4 : 3;
        }
        if (month == 7) {
            return day >= 7 ? 5 : 4;
        }
        if (month == 8) {
            return day >= 8 ? 6 : 5;
        }
        if (month == 9) {
            return day >= 8 ? 7 : 6;
        }
        if (month == 10) {
            return day >= 8 ? 8 : 7;
        }
        if (month == 11) {
            return day >= 7 ? 9 : 8;
        }
        return day >= 7 ? 10 : 9;
    }

    private int dayStemIndex(LocalDate date) {
        return Math.floorMod(dayPillarIndex(date), 10);
    }

    private int dayBranchIndex(LocalDate date) {
        return Math.floorMod(dayPillarIndex(date), 12);
    }

    private int dayPillarIndex(LocalDate date) {
        return Math.floorMod((int) ChronoUnit.DAYS.between(DAY_PILLAR_ANCHOR, date) + 40, 60);
    }

    private int hourBranchIndex(LocalTime time) {
        int hour = time.getHour();
        if (hour == 23 || hour == 0) {
            return 0;
        }
        return ((hour + 1) / 2) % 12;
    }

    private int hourStemIndex(int dayStemIndex, int hourBranchIndex) {
        int firstHourStemIndex = switch (dayStemIndex) {
            case 0, 5 -> 0;
            case 1, 6 -> 2;
            case 2, 7 -> 4;
            case 3, 8 -> 6;
            default -> 8;
        };
        return Math.floorMod(firstHourStemIndex + hourBranchIndex, 10);
    }

    private String pillar(int stemIndex, int branchIndex) {
        return STEMS[stemIndex] + BRANCHES[branchIndex];
    }

    private Map<String, Integer> newCountMap(String... keys) {
        Map<String, Integer> values = new LinkedHashMap<>();
        for (String key : keys) {
            values.put(key, 0);
        }
        return values;
    }

    private void addElement(Map<String, Integer> fiveElements, String stemElement, String branchElement) {
        fiveElements.computeIfPresent(stemElement, (key, value) -> value + 1);
        fiveElements.computeIfPresent(branchElement, (key, value) -> value + 1);
    }

    private void addYinYang(Map<String, Integer> yinYang, int stemIndex, int branchIndex) {
        yinYang.computeIfPresent(stemIndex % 2 == 0 ? "양" : "음", (key, value) -> value + 1);
        yinYang.computeIfPresent(branchIndex % 2 == 0 ? "양" : "음", (key, value) -> value + 1);
    }
}
