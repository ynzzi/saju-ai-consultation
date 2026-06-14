package com.portfolio.saju.profile.domain;

import com.portfolio.saju.common.entity.BaseTimeEntity;
import com.portfolio.saju.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "saju_profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SajuProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 60)
    private String profileName;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private LocalTime birthTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CalendarType calendarType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Column(length = 120)
    private String birthPlace;

    @Column
    private Boolean leapMonth;

    @Column(nullable = false, length = 1000)
    private String analysisSummary;

    @Column(nullable = false, length = 1000)
    private String elementSummary;

    @Column(nullable = false, length = 1000)
    private String strengths;

    @Column(nullable = false, length = 1000)
    private String cautions;

    @Column(nullable = false, length = 1000)
    private String recommendedQuestions;

    @Column(length = 20)
    private String yearPillar;

    @Column(length = 20)
    private String monthPillar;

    @Column(length = 20)
    private String dayPillar;

    @Column(length = 20)
    private String hourPillar;

    @Column(length = 1000)
    private String fiveElementsSummary;

    @Column(length = 1000)
    private String yinYangSummary;

    @Column(length = 1000)
    private String calculationStandard;

    @Column(length = 1000)
    private String calculationWarning;

    @Column(length = 60)
    private String manseCalendarVersion;

    @Builder
    private SajuProfile(
            User user,
            String profileName,
            LocalDate birthDate,
            LocalTime birthTime,
            CalendarType calendarType,
            Gender gender,
            String birthPlace,
            Boolean leapMonth,
            String analysisSummary,
            String elementSummary,
            String strengths,
            String cautions,
            String recommendedQuestions,
            String yearPillar,
            String monthPillar,
            String dayPillar,
            String hourPillar,
            String fiveElementsSummary,
            String yinYangSummary,
            String calculationStandard,
            String calculationWarning,
            String manseCalendarVersion
    ) {
        this.user = user;
        this.profileName = profileName;
        this.birthDate = birthDate;
        this.birthTime = birthTime;
        this.calendarType = calendarType;
        this.gender = gender;
        this.birthPlace = birthPlace;
        this.leapMonth = leapMonth;
        this.analysisSummary = analysisSummary;
        this.elementSummary = elementSummary;
        this.strengths = strengths;
        this.cautions = cautions;
        this.recommendedQuestions = recommendedQuestions;
        this.yearPillar = yearPillar;
        this.monthPillar = monthPillar;
        this.dayPillar = dayPillar;
        this.hourPillar = hourPillar;
        this.fiveElementsSummary = fiveElementsSummary;
        this.yinYangSummary = yinYangSummary;
        this.calculationStandard = calculationStandard;
        this.calculationWarning = calculationWarning;
        this.manseCalendarVersion = manseCalendarVersion;
    }

    public void updateAnalysis(
            String analysisSummary,
            String elementSummary,
            String strengths,
            String cautions,
            String recommendedQuestions,
            String yearPillar,
            String monthPillar,
            String dayPillar,
            String hourPillar,
            String fiveElementsSummary,
            String yinYangSummary,
            String calculationStandard,
            String calculationWarning,
            String manseCalendarVersion
    ) {
        this.analysisSummary = analysisSummary;
        this.elementSummary = elementSummary;
        this.strengths = strengths;
        this.cautions = cautions;
        this.recommendedQuestions = recommendedQuestions;
        this.yearPillar = yearPillar;
        this.monthPillar = monthPillar;
        this.dayPillar = dayPillar;
        this.hourPillar = hourPillar;
        this.fiveElementsSummary = fiveElementsSummary;
        this.yinYangSummary = yinYangSummary;
        this.calculationStandard = calculationStandard;
        this.calculationWarning = calculationWarning;
        this.manseCalendarVersion = manseCalendarVersion;
    }
}
