package com.portfolio.saju.consultation;

import com.portfolio.saju.consultation.dto.ConsultationResponse;
import com.portfolio.saju.consultation.dto.CreateConsultationRequest;
import com.portfolio.saju.consultation.service.AiConsultationService;
import com.portfolio.saju.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles/{profileId}/consultations")
@RequiredArgsConstructor
public class AiConsultationController {

    private final AiConsultationService aiConsultationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultationResponse create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long profileId,
            @Valid @RequestBody CreateConsultationRequest request
    ) {
        return aiConsultationService.create(userDetails.getId(), profileId, request);
    }

    @GetMapping
    public List<ConsultationResponse> getConsultations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long profileId
    ) {
        return aiConsultationService.getConsultations(userDetails.getId(), profileId);
    }
}
