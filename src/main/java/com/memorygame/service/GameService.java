package com.memorygame.service;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.memorygame.dto.GameEndRequestDto;
import com.memorygame.dto.GameStartResponseDto;
import com.memorygame.exception.InvalidGameSessionException;
import com.memorygame.exception.InvalidScoreException;
import com.memorygame.model.GameSession;
import com.memorygame.model.User;
import com.memorygame.repository.GameSessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameSessionRepository gameSessionRepository;
    private final ScoreService scoreService;

    // Minimum time per click in milliseconds (e.g., 200ms)
    private static final long MIN_TIME_PER_CLICK_MS = 200;

    @Transactional
    public GameStartResponseDto startGame(User user) {
        GameSession session = new GameSession(user);
        gameSessionRepository.save(session);
        return new GameStartResponseDto(session.getId());
    }

    @Transactional
    public String endGame(GameEndRequestDto request) {
        GameSession session = gameSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new InvalidGameSessionException("Invalid session ID"));

        if (session.getStatus() != GameSession.SessionStatus.IN_PROGRESS) {
            throw new InvalidGameSessionException("Session is not in progress");
        }

        session.endSession();

        // Calculate total score
        // Score logic: (stage - 1) * (stage + 2) / 2 + clicksInCurrentStage
        // Or simply: sum of numbers from 1 to (stage-1) + clicksInCurrentStage
        // Let's assume the frontend logic: totalScore accumulates 1 point per correct
        // click.
        // Total clicks = sum of (i + 2) for i = 1 to (stage - 1) + clicksInCurrentStage

        int totalClicks = 0;
        for (int i = 1; i < request.getStage(); i++) {
            totalClicks += (i + 2);
        }
        totalClicks += request.getClicksInCurrentStage();

        int calculatedScore = totalClicks;

        // Calculate maximum possible score for the stage
        int maxPossibleClicks = 0;
        for (int i = 1; i <= request.getStage(); i++) {
            maxPossibleClicks += (i + 2);
        }

        if (calculatedScore > maxPossibleClicks) {
            throw new InvalidScoreException("Score exceeds maximum possible for stage " + request.getStage());
        }

        // Validation
        long durationMs = Duration.between(session.getStartTime(), session.getEndTime()).toMillis();
        long minDurationMs = totalClicks * MIN_TIME_PER_CLICK_MS;

        if (durationMs < minDurationMs) {
            throw new InvalidScoreException("Game duration too short. Possible cheating detected.");
        }

        if (session.getUser() != null) {
            scoreService.saveScore(session.getUser(), calculatedScore);
        }

        return "Score recorded: " + calculatedScore;
    }
}
