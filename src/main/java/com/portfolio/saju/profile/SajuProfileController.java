package com.portfolio.saju.profile;

import com.portfolio.saju.profile.dto.CreateSajuProfileRequest;
import com.portfolio.saju.profile.dto.SajuProfileResponse;
import com.portfolio.saju.profile.service.SajuProfileService;
import com.portfolio.saju.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class SajuProfileController {

    private final SajuProfileService sajuProfileService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SajuProfileResponse create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateSajuProfileRequest request
    ) {
        return sajuProfileService.create(userDetails.getId(), request);
    }

    @GetMapping
    public List<SajuProfileResponse> getProfiles(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return sajuProfileService.getProfiles(userDetails.getId());
    }

    @GetMapping("/{profileId}")
    public SajuProfileResponse getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long profileId
    ) {
        return sajuProfileService.getProfile(userDetails.getId(), profileId);
    }

    @PostMapping("/{profileId}/reanalyze")
    public SajuProfileResponse reanalyzeProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long profileId
    ) {
        return sajuProfileService.reanalyzeProfile(userDetails.getId(), profileId);
    }

    @DeleteMapping("/{profileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long profileId
    ) {
        sajuProfileService.delete(userDetails.getId(), profileId);
    }
}
