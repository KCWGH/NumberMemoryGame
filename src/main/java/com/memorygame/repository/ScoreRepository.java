package com.memorygame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.memorygame.model.Score;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findTop10ByOrderByScoreDesc();
}
