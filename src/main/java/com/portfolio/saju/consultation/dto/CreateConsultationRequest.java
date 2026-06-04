package com.portfolio.saju.consultation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateConsultationRequest(
        @NotBlank @Size(max = 2000) String question
) {
}
