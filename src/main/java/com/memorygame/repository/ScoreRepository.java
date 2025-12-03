package com.memorygame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.memorygame.model.Score;
import com.memorygame.model.User;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    @Query("SELECT s FROM Score s ORDER BY s.scoreValue DESC, s.playedAt ASC LIMIT 10")
    List<Score> findTop10ByOrderByScoreValueDesc();

    boolean existsByUserAndScoreValueAndPlayedAtBetween(User user, int scoreValue, java.time.LocalDateTime start,
            java.time.LocalDateTime end);

    List<Score> findByUserOrderByScoreValueDesc(User user);

    List<Score> findByPlayedAtBefore(java.time.LocalDateTime dateTime);

}