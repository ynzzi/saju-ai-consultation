package com.portfolio.saju.profile.service;

import com.portfolio.saju.common.exception.BusinessException;
import com.portfolio.saju.common.exception.ErrorCode;
import com.portfolio.saju.consultation.repository.AiConsultationRepository;
import com.portfolio.saju.profile.domain.SajuProfile;
import com.portfolio.saju.profile.dto.CreateSajuProfileRequest;
import com.portfolio.saju.profile.dto.SajuAnalysisResult;
import com.portfolio.saju.profile.dto.SajuProfileResponse;
import com.portfolio.saju.profile.repository.SajuProfileRepository;
import com.portfolio.saju.user.domain.User;
import com.portfolio.saju.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SajuProfileService {

    private final UserRepository userRepository;
    private final SajuProfileRepository sajuProfileRepository;
    private final SajuAnalysisService sajuAnalysisService;
    private final AiConsultationRepository aiConsultationRepository;

    @Transactional
    public SajuProfileResponse create(Long userId, CreateSajuProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        SajuAnalysisResult analysis = sajuAnalysisService.analyze(
                request.birthDate(),
                request.birthTime(),
                request.calendarType(),
                request.gender(),
                request.leapMonthValue()
        );
        AnalysisText analysisText = toAnalysisText(analysis);

        SajuProfile profile = SajuProfile.builder()
                .user(user)
                .profileName(request.profileName())
                .birthDate(request.birthDate())
                .birthTime(request.birthTime())
                .calendarType(request.calendarType())
                .gender(request.gender())
                .birthPlace(request.birthPlace())
                .leapMonth(request.leapMonthValue())
                .analysisSummary(analysisText.analysisSummary())
                .elementSummary(analysisText.elementSummary())
                .strengths(analysisText.strengths())
                .cautions(analysisText.cautions())
                .recommendedQuestions(analysisText.recommendedQuestions())
                .yearPillar(analysis.yearPillar())
                .monthPillar(analysis.monthPillar())
                .dayPillar(analysis.dayPillar())
                .hourPillar(analysis.hourPillar())
                .fiveElementsSummary(formatCounts(analysis.fiveElements()))
                .yinYangSummary(formatCounts(analysis.yinYang()))
                .calculationStandard(analysis.calculationStandard())
                .calculationWarning(analysis.calculationWarning())
                .manseCalendarVersion(analysis.manseCalendarVersion())
                .build();

        return SajuProfileResponse.from(sajuProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public List<SajuProfileResponse> getProfiles(Long userId) {
        return sajuProfileRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(SajuProfileResponse::from)
                .toList();
    }

    @Transactional
    public SajuProfileResponse getProfile(Long userId, Long profileId) {
        SajuProfile profile = findOwnedProfile(userId, profileId);
        backfillAnalysisIfNeeded(profile);
        return SajuProfileResponse.from(profile);
    }

    @Transactional
    public void delete(Long userId, Long profileId) {
        SajuProfile profile = findOwnedProfile(userId, profileId);
        aiConsultationRepository.deleteAllByUserIdAndProfileId(userId, profileId);
        sajuProfileRepository.delete(profile);
    }

    @Transactional
    public SajuProfileResponse reanalyzeProfile(Long userId, Long profileId) {
        SajuProfile profile = findOwnedProfile(userId, profileId);
        updateAnalysis(profile);
        return SajuProfileResponse.from(profile);
    }

    @Transactional(readOnly = true)
    public SajuProfile findOwnedProfile(Long userId, Long profileId) {
        return sajuProfileRepository.findByIdAndUserId(profileId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }

    private String join(List<String> values) {
        return String.join("\n", values);
    }

    private String formatCounts(Map<String, Integer> counts) {
        if (counts == null || counts.isEmpty()) {
            return "";
        }
        return counts.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }

    private AnalysisText toAnalysisText(SajuAnalysisResult analysis) {
        return new AnalysisText(
                analysis.analysisSummary(),
                analysis.elementSummary(),
                join(analysis.strengths()),
                join(analysis.cautions()),
                join(analysis.recommendedQuestions())
        );
    }

    private void backfillAnalysisIfNeeded(SajuProfile profile) {
        if (!needsAnalysisBackfill(profile)) {
            return;
        }

        updateAnalysis(profile);
    }

    private boolean needsAnalysisBackfill(SajuProfile profile) {
        return isBlank(profile.getYearPillar())
                || isBlank(profile.getMonthPillar())
                || isBlank(profile.getDayPillar())
                || isBlank(profile.getHourPillar())
                || isBlank(profile.getFiveElementsSummary())
                || isBlank(profile.getYinYangSummary())
                || isBlank(profile.getCalculationStandard())
                || isBlank(profile.getCalculationWarning())
                || isBlank(profile.getManseCalendarVersion())
                || containsLegacyDisplayTerm(profile.getAnalysisSummary())
                || containsLegacyDisplayTerm(profile.getElementSummary())
                || containsLegacyDisplayTerm(profile.getCalculationStandard())
                || containsLegacyDisplayTerm(profile.getCalculationWarning());
    }

    private void updateAnalysis(SajuProfile profile) {
        SajuAnalysisResult analysis = sajuAnalysisService.analyze(
                profile.getBirthDate(),
                profile.getBirthTime(),
                profile.getCalendarType(),
                profile.getGender(),
                Boolean.TRUE.equals(profile.getLeapMonth())
        );
        AnalysisText analysisText = toAnalysisText(analysis);
        profile.updateAnalysis(
                analysisText.analysisSummary(),
                analysisText.elementSummary(),
                analysisText.strengths(),
                analysisText.cautions(),
                analysisText.recommendedQuestions(),
                analysis.yearPillar(),
                analysis.monthPillar(),
                analysis.dayPillar(),
                analysis.hourPillar(),
                formatCounts(analysis.fiveElements()),
                formatCounts(analysis.yinYang()),
                analysis.calculationStandard(),
                analysis.calculationWarning(),
                analysis.manseCalendarVersion()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean containsLegacyDisplayTerm(String value) {
        return value != null && value.contains("MVP");
    }

    private record AnalysisText(
            String analysisSummary,
            String elementSummary,
            String strengths,
            String cautions,
            String recommendedQuestions
    ) {
    }
}
