package com.memorygame.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.memorygame.model.Score;
import com.memorygame.model.User;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    // 상위 10개 점수 (최고 점수 순)
    @Query("SELECT s FROM Score s ORDER BY s.scoreValue DESC, s.playedAt ASC LIMIT 10")
    List<Score> findTop10ByOrderByScoreValueDesc();
    
    // 중복 점수 체크를 위한 메서드
    boolean existsByUserAndScoreValueAndPlayedAtGreaterThan(User user, int scoreValue, LocalDateTime time);
}