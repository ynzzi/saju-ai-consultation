package com.portfolio.saju.profile.service;

import com.portfolio.saju.common.exception.BusinessException;
import com.portfolio.saju.common.exception.ErrorCode;
import com.portfolio.saju.profile.domain.SajuProfile;
import com.portfolio.saju.profile.dto.CreateSajuProfileRequest;
import com.portfolio.saju.profile.dto.SajuAnalysisResult;
import com.portfolio.saju.profile.dto.SajuProfileResponse;
import com.portfolio.saju.profile.repository.SajuProfileRepository;
import com.portfolio.saju.user.domain.User;
import com.portfolio.saju.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SajuProfileService {

    private final UserRepository userRepository;
    private final SajuProfileRepository sajuProfileRepository;
    private final SajuAnalysisService sajuAnalysisService;

    @Transactional
    public SajuProfileResponse create(Long userId, CreateSajuProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        SajuAnalysisResult analysis = sajuAnalysisService.analyze(
                request.birthDate(),
                request.birthTime(),
                request.calendarType(),
                request.gender()
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
                .analysisSummary(analysisText.analysisSummary())
                .elementSummary(analysisText.elementSummary())
                .strengths(analysisText.strengths())
                .cautions(analysisText.cautions())
                .recommendedQuestions(analysisText.recommendedQuestions())
                .build();

        return SajuProfileResponse.from(sajuProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public List<SajuProfileResponse> getProfiles(Long userId) {
        return sajuProfileRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(SajuProfileResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SajuProfileResponse getProfile(Long userId, Long profileId) {
        return SajuProfileResponse.from(findOwnedProfile(userId, profileId));
    }

    @Transactional
    public void delete(Long userId, Long profileId) {
        SajuProfile profile = findOwnedProfile(userId, profileId);
        sajuProfileRepository.delete(profile);
    }

    @Transactional
    public SajuProfileResponse reanalyzeProfile(Long userId, Long profileId) {
        SajuProfile profile = findOwnedProfile(userId, profileId);
        SajuAnalysisResult analysis = sajuAnalysisService.analyze(
                profile.getBirthDate(),
                profile.getBirthTime(),
                profile.getCalendarType(),
                profile.getGender()
        );
        AnalysisText analysisText = toAnalysisText(analysis);
        profile.updateAnalysis(
                analysisText.analysisSummary(),
                analysisText.elementSummary(),
                analysisText.strengths(),
                analysisText.cautions(),
                analysisText.recommendedQuestions()
        );
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

    private AnalysisText toAnalysisText(SajuAnalysisResult analysis) {
        return new AnalysisText(
                analysis.analysisSummary(),
                analysis.elementSummary(),
                join(analysis.strengths()),
                join(analysis.cautions()),
                join(analysis.recommendedQuestions())
        );
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
