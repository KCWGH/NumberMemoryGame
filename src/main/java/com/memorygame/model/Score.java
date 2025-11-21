package com.memorygame.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "game_score", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "score_value" })
})
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int scoreValue;

    @Column(nullable = false)
    private LocalDateTime playedAt;

    @Builder
    public Score(User user, int scoreValue, LocalDateTime playedAt) {
        this.user = user;
        this.scoreValue = scoreValue;
        this.playedAt = (playedAt != null) ? playedAt : LocalDateTime.now();
    }
}
