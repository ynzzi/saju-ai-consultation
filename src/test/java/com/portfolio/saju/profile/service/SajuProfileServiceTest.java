package com.portfolio.saju.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.portfolio.saju.consultation.repository.AiConsultationRepository;
import com.portfolio.saju.profile.domain.CalendarType;
import com.portfolio.saju.profile.domain.Gender;
import com.portfolio.saju.profile.domain.SajuProfile;
import com.portfolio.saju.profile.dto.CreateSajuProfileRequest;
import com.portfolio.saju.profile.dto.SajuAnalysisResult;
import com.portfolio.saju.profile.dto.SajuProfileResponse;
import com.portfolio.saju.profile.repository.SajuProfileRepository;
import com.portfolio.saju.user.domain.Role;
import com.portfolio.saju.user.domain.User;
import com.portfolio.saju.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SajuProfileServiceTest {

    @Test
    void createSolarProfile() {
        TestFixture fixture = new TestFixture();
        when(fixture.userRepository.findById(1L)).thenReturn(Optional.of(testUser()));
        when(fixture.analysisService.analyze(any(), any(), any(), any(), anyBoolean()))
                .thenReturn(testAnalysis(false));
        when(fixture.profileRepository.save(any(SajuProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SajuProfileResponse response = fixture.service.create(1L, new CreateSajuProfileRequest(
                "solar",
                LocalDate.of(1998, 3, 15),
                LocalTime.of(9, 30),
                CalendarType.SOLAR,
                Gender.FEMALE,
                "Seoul",
                null
        ));

        assertThat(response.leapMonth()).isFalse();
        assertThat(response.yearPillar()).isEqualTo("무인");
    }

    @Test
    void createLunarProfile() {
        TestFixture fixture = new TestFixture();
        when(fixture.userRepository.findById(1L)).thenReturn(Optional.of(testUser()));
        when(fixture.analysisService.analyze(any(), any(), any(), any(), anyBoolean()))
                .thenReturn(testAnalysis(false));
        when(fixture.profileRepository.save(any(SajuProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SajuProfileResponse response = fixture.service.create(1L, new CreateSajuProfileRequest(
                "lunar",
                LocalDate.of(1998, 2, 17),
                LocalTime.of(9, 30),
                CalendarType.LUNAR,
                Gender.FEMALE,
                "Seoul",
                false
        ));

        assertThat(response.leapMonth()).isFalse();
        assertThat(response.hourPillar()).isEqualTo("기사");
    }

    @Test
    void createLeapMonthProfile() {
        TestFixture fixture = new TestFixture();
        when(fixture.userRepository.findById(1L)).thenReturn(Optional.of(testUser()));
        when(fixture.analysisService.analyze(any(), any(), any(), any(), anyBoolean()))
                .thenReturn(testAnalysis(true));
        when(fixture.profileRepository.save(any(SajuProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SajuProfileResponse response = fixture.service.create(1L, new CreateSajuProfileRequest(
                "leap",
                LocalDate.of(2023, 2, 15),
                LocalTime.of(8, 0),
                CalendarType.LUNAR,
                Gender.FEMALE,
                "Seoul",
                true
        ));

        assertThat(response.leapMonth()).isTrue();
    }

    @Test
    void deleteProfileAlsoDeletesConsultations() {
        TestFixture fixture = new TestFixture();
        SajuProfile profile = SajuProfile.builder()
                .profileName("test")
                .birthDate(LocalDate.of(1998, 3, 15))
                .birthTime(LocalTime.of(9, 30))
                .calendarType(CalendarType.SOLAR)
                .gender(Gender.FEMALE)
                .birthPlace("Seoul")
                .leapMonth(false)
                .analysisSummary("summary")
                .elementSummary("element")
                .strengths("strength")
                .cautions("caution")
                .recommendedQuestions("question")
                .build();
        when(fixture.profileRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(profile));

        fixture.service.delete(1L, 10L);

        verify(fixture.consultationRepository).deleteAllByUserIdAndProfileId(1L, 10L);
        verify(fixture.profileRepository).delete(profile);
    }

    @Test
    void getProfileBackfillsMissingManseAnalysis() {
        TestFixture fixture = new TestFixture();
        SajuProfile profile = SajuProfile.builder()
                .profileName("legacy")
                .birthDate(LocalDate.of(1998, 3, 15))
                .birthTime(LocalTime.of(9, 30))
                .calendarType(CalendarType.SOLAR)
                .gender(Gender.FEMALE)
                .leapMonth(false)
                .analysisSummary("old summary")
                .elementSummary("old element")
                .strengths("old strength")
                .cautions("old caution")
                .recommendedQuestions("old question")
                .build();
        when(fixture.profileRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(profile));
        when(fixture.analysisService.analyze(any(), any(), any(), any(), anyBoolean()))
                .thenReturn(testAnalysis(false));

        SajuProfileResponse response = fixture.service.getProfile(1L, 10L);

        assertThat(response.yearPillar()).isEqualTo("무인");
        assertThat(response.fiveElementsSummary()).containsExactly("목: 2", "화: 1", "토: 2", "금: 1", "수: 2");
        verify(fixture.analysisService).analyze(
                LocalDate.of(1998, 3, 15),
                LocalTime.of(9, 30),
                CalendarType.SOLAR,
                Gender.FEMALE,
                false
        );
    }

    private User testUser() {
        return User.builder()
                .email("user@example.com")
                .password("encoded")
                .nickname("tester")
                .role(Role.USER)
                .build();
    }

    private SajuAnalysisResult testAnalysis(boolean leapMonth) {
        Map<String, Integer> elements = new LinkedHashMap<>();
        elements.put("목", 2);
        elements.put("화", 1);
        elements.put("토", 2);
        elements.put("금", 1);
        elements.put("수", 2);
        Map<String, Integer> yinYang = new LinkedHashMap<>();
        yinYang.put("양", 4);
        yinYang.put("음", 4);
        return new SajuAnalysisResult(
                "summary",
                "element",
                List.of("strength"),
                List.of("caution"),
                List.of("question"),
                leapMonth,
                "무인",
                "을묘",
                "갑진",
                "기사",
                elements,
                yinYang,
                "standard",
                "warning",
                "manse-v1-mvp"
        );
    }

    private static class TestFixture {
        private final UserRepository userRepository = mock(UserRepository.class);
        private final SajuProfileRepository profileRepository = mock(SajuProfileRepository.class);
        private final SajuAnalysisService analysisService = mock(SajuAnalysisService.class);
        private final AiConsultationRepository consultationRepository = mock(AiConsultationRepository.class);
        private final SajuProfileService service = new SajuProfileService(
                userRepository,
                profileRepository,
                analysisService,
                consultationRepository
        );
    }
}
