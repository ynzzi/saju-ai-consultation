package com.portfolio.saju.consultation.dto;

import com.portfolio.saju.consultation.domain.AiConsultation;
import java.time.LocalDateTime;

public record ConsultationResponse(
        Long id,
        Long userId,
        Long profileId,
        String question,
        String answer,
        LocalDateTime createdAt
) {

    public static ConsultationResponse from(AiConsultation consultation) {
        return new ConsultationResponse(
                consultation.getId(),
                consultation.getUser().getId(),
                consultation.getProfile().getId(),
                consultation.getQuestion(),
                consultation.getAnswer(),
                consultation.getCreatedAt()
        );
    }
}
