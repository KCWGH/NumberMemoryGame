package com.memorygame.repository;

import com.memorygame.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    // 상위 10개 점수 (최고 점수 순)
    @Query("SELECT s FROM Score s ORDER BY s.scoreValue DESC, s.playedAt ASC LIMIT 10")
    List<Score> findTop10ByOrderByScoreValueDesc();
}