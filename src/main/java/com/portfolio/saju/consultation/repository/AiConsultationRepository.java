package com.portfolio.saju.consultation.repository;

import com.portfolio.saju.consultation.domain.AiConsultation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiConsultationRepository extends JpaRepository<AiConsultation, Long> {

    List<AiConsultation> findAllByUserIdAndProfileIdOrderByCreatedAtDesc(Long userId, Long profileId);

    List<AiConsultation> findTop5ByUserIdAndProfileIdOrderByCreatedAtDesc(Long userId, Long profileId);

    void deleteAllByUserIdAndProfileId(Long userId, Long profileId);
}
