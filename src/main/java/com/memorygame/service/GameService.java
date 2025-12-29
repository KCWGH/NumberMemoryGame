package com.memorygame.service;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.memorygame.dto.GameEndRequestDto;
import com.memorygame.dto.GameStartResponseDto;
import com.memorygame.dto.StageCompleteRequestDto;
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
    public String completeStage(StageCompleteRequestDto request) {
        GameSession session = gameSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new InvalidGameSessionException("Invalid session ID"));

        if (session.getStatus() != GameSession.SessionStatus.IN_PROGRESS) {
            throw new InvalidGameSessionException("Session is not in progress");
        }

        if (!request.getStage().equals(session.getCurrentStage())) {
            throw new InvalidScoreException(
                "Stage mismatch. Expected stage " + session.getCurrentStage() + " but got " + request.getStage());
        }

        int expectedClicks = request.getStage() + 2;
        if (!request.getScoreGained().equals(expectedClicks)) {
            throw new InvalidScoreException(
                "Invalid score for stage " + request.getStage() + ". Expected " + expectedClicks + " but got " + request.getScoreGained());
        }

        if (session.getLastStageCompletedAt() != null) {
            long timeSinceLastStage = Duration.between(session.getLastStageCompletedAt(), java.time.LocalDateTime.now()).toMillis();
            long minTimeRequired = expectedClicks * MIN_TIME_PER_CLICK_MS;
            
            if (timeSinceLastStage < minTimeRequired) {
                throw new InvalidScoreException("Stage completed too quickly. Possible cheating detected.");
            }
        } else {
            long timeSinceStart = Duration.between(session.getStartTime(), java.time.LocalDateTime.now()).toMillis();
            long minTimeRequired = expectedClicks * MIN_TIME_PER_CLICK_MS + 2000; // +2s for initial reveal
            
            if (timeSinceStart < minTimeRequired) {
                throw new InvalidScoreException("Stage completed too quickly. Possible cheating detected.");
            }
        }

        session.advanceStage(request.getScoreGained());
        gameSessionRepository.save(session);

        return "Stage " + request.getStage() + " completed. Score: " + session.getTotalScore();
    }

    @Transactional
    public String endGame(GameEndRequestDto request) {
        GameSession session = gameSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new InvalidGameSessionException("Invalid session ID"));

        if (session.getStatus() != GameSession.SessionStatus.IN_PROGRESS) {
            throw new InvalidGameSessionException("Session is not in progress");
        }

        session.endSession();

        int finalScore = session.getTotalScore();

        if (session.getUser() != null && finalScore > 0) {
            scoreService.saveScore(session.getUser(), finalScore);
        }

        return "Score recorded: " + finalScore;
    }
}
