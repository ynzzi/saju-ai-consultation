package com.portfolio.saju.profile.repository;

import com.portfolio.saju.profile.domain.SajuProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SajuProfileRepository extends JpaRepository<SajuProfile, Long> {

    List<SajuProfile> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<SajuProfile> findByIdAndUserId(Long id, Long userId);
}
