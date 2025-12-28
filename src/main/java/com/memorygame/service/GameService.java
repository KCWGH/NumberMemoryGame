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



        int totalClicks = 0;
        for (int i = 1; i < request.getStage(); i++) {
            totalClicks += (i + 2);
        }
        totalClicks += request.getClicksInCurrentStage();

        int calculatedScore = totalClicks;


        int maxPossibleClicks = 0;
        for (int i = 1; i <= request.getStage(); i++) {
            maxPossibleClicks += (i + 2);
        }

        if (calculatedScore > maxPossibleClicks) {
            throw new InvalidScoreException("Score exceeds maximum possible for stage " + request.getStage());
        }
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
