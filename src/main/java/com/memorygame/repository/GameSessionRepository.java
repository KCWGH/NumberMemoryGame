package com.memorygame.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.memorygame.model.GameSession;

public interface GameSessionRepository extends JpaRepository<GameSession, String> {
}
