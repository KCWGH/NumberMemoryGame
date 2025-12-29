package com.memorygame.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameSession {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column(nullable = false)
    private Integer currentStage = 1;

    @Column(nullable = false)
    private Integer totalScore = 0;

    private LocalDateTime lastStageCompletedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.startTime == null) {
            this.startTime = LocalDateTime.now();
        }
    }

    public GameSession(User user) {
        this.user = user;
        this.status = SessionStatus.IN_PROGRESS;
        this.currentStage = 1;
        this.totalScore = 0;
    }

    public void advanceStage(int scoreGained) {
        this.currentStage++;
        this.totalScore += scoreGained;
        this.lastStageCompletedAt = LocalDateTime.now();
    }

    public void endSession() {
        this.endTime = LocalDateTime.now();
        this.status = SessionStatus.COMPLETED;
    }

    public enum SessionStatus {
        IN_PROGRESS, COMPLETED, INVALID
    }
}
