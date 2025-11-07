package com.memorygame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.memorygame.model.Score;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    
    // 점수가 높은 순서대로 최대 10개의 기록을 조회
    // JOIN FETCH를 사용하여 N+1 쿼리 문제 방지
    @Query("SELECT s FROM Score s JOIN FETCH s.user ORDER BY s.scoreValue DESC, s.recordedAt ASC LIMIT 10")
    List<Score> findTop10ByScoreValueDesc();
}