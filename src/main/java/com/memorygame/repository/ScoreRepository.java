package com.memorygame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.memorygame.model.Score;
import com.memorygame.model.User;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    @Query("SELECT s FROM Score s WHERE s.playedAt >= :startOfDay ORDER BY s.scoreValue DESC, s.playedAt ASC LIMIT 10")
    List<Score> findTop10ByOrderByScoreValueDesc(java.time.LocalDateTime startOfDay);

    boolean existsByUserAndScoreValue(User user, int scoreValue);

    void deleteByPlayedAtBefore(java.time.LocalDateTime dateTime);

}