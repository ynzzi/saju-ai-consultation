package com.portfolio.saju.consultation.domain;

import com.portfolio.saju.profile.domain.SajuProfile;
import com.portfolio.saju.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "ai_consultations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiConsultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private SajuProfile profile;

    @Column(nullable = false, length = 2000)
    private String question;

    @Column(nullable = false, length = 5000)
    private String answer;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private AiConsultation(User user, SajuProfile profile, String question, String answer) {
        this.user = user;
        this.profile = profile;
        this.question = question;
        this.answer = answer;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
