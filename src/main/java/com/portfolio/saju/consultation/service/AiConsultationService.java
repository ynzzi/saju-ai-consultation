package com.portfolio.saju.consultation.service;

import com.portfolio.saju.common.exception.BusinessException;
import com.portfolio.saju.common.exception.ErrorCode;
import com.portfolio.saju.consultation.ai.AiClient;
import com.portfolio.saju.consultation.ai.AiPrompt;
import com.portfolio.saju.consultation.domain.AiConsultation;
import com.portfolio.saju.consultation.dto.ConsultationResponse;
import com.portfolio.saju.consultation.dto.CreateConsultationRequest;
import com.portfolio.saju.consultation.repository.AiConsultationRepository;
import com.portfolio.saju.profile.domain.SajuProfile;
import com.portfolio.saju.profile.service.SajuProfileService;
import com.portfolio.saju.user.domain.User;
import com.portfolio.saju.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiConsultationService {

    private final UserRepository userRepository;
    private final SajuProfileService sajuProfileService;
    private final AiConsultationRepository aiConsultationRepository;
    private final AiRateLimiter aiRateLimiter;
    private final AiClient aiClient;

    @Transactional
    public ConsultationResponse create(Long userId, Long profileId, CreateConsultationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        SajuProfile profile = sajuProfileService.findOwnedProfile(userId, profileId);
        aiRateLimiter.checkAndIncrease(userId);
        List<AiConsultation> histories = aiConsultationRepository.findTop5ByUserIdAndProfileIdOrderByCreatedAtDesc(userId, profileId);

        String answer = aiClient.generateAnswer(new AiPrompt(
                request.question(),
                profileContext(profile),
                analysisContext(profile),
                historyContext(histories)
        ));

        AiConsultation consultation = AiConsultation.builder()
                .user(user)
                .profile(profile)
                .question(request.question())
                .answer(answer)
                .build();

        return ConsultationResponse.from(aiConsultationRepository.save(consultation));
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> getConsultations(Long userId, Long profileId) {
        sajuProfileService.findOwnedProfile(userId, profileId);
        return aiConsultationRepository.findAllByUserIdAndProfileIdOrderByCreatedAtDesc(userId, profileId).stream()
                .map(ConsultationResponse::from)
                .toList();
    }

    private String profileContext(SajuProfile profile) {
        return """
                이름: %s
                생년월일: %s
                출생시간: %s
                달력: %s
                성별: %s
                출생지: %s
                """.formatted(
                profile.getProfileName(),
                profile.getBirthDate(),
                profile.getBirthTime(),
                profile.getCalendarType(),
                profile.getGender(),
                profile.getBirthPlace() == null ? "" : profile.getBirthPlace()
        );
    }

    private String analysisContext(SajuProfile profile) {
        return """
                기본 성향: %s
                오행 요약: %s
                강점: %s
                주의점: %s
                추천 질문: %s
                """.formatted(
                profile.getAnalysisSummary(),
                profile.getElementSummary(),
                profile.getStrengths(),
                profile.getCautions(),
                profile.getRecommendedQuestions()
        );
    }

    private String historyContext(List<AiConsultation> histories) {
        if (histories.isEmpty()) {
            return "이전 상담 기록 없음";
        }
        return histories.stream()
                .map(history -> "Q: %s\nA: %s".formatted(history.getQuestion(), history.getAnswer()))
                .reduce((left, right) -> left + "\n\n" + right)
                .orElse("이전 상담 기록 없음");
    }
}
