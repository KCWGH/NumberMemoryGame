package com.memorygame.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.memorygame.dto.ScoreRecordDto;
import com.memorygame.model.Score;
import com.memorygame.model.User;
import com.memorygame.repository.ScoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScoreService {

    private final ScoreRepository scoreRepository;

    @Transactional
    public void saveScore(User user, int scoreValue) {
        if (user != null && scoreValue > 0) {
            java.time.LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            java.time.LocalDateTime endOfDay = LocalDate.now().atTime(java.time.LocalTime.MAX);

            boolean isDuplicate = scoreRepository.existsByUserAndScoreValueAndPlayedAtBetween(user, scoreValue,
                    startOfDay, endOfDay);

            if (isDuplicate) {
                throw new IllegalStateException("동일한 점수(" + scoreValue + ")가 오늘 이미 기록되어 있습니다.");
            }

            Score score = Score.builder()
                    .user(user)
                    .scoreValue(scoreValue)
                    .build();
            scoreRepository.save(score);
        }
    }

    public List<ScoreRecordDto> getLeaderboard() {
        return scoreRepository.findTop10ByOrderByScoreValueDesc().stream()
                .map(score -> new ScoreRecordDto(
                        score.getScoreValue(),
                        score.getUser().getName(),
                        score.getUser().getProvider().toString()))
                .toList();
    }

    public List<ScoreRecordDto> getMyScores(User user) {
        return scoreRepository.findByUserOrderByScoreValueDesc(user).stream()
                .map(score -> new ScoreRecordDto(
                        score.getScoreValue(),
                        score.getUser().getName(),
                        score.getUser().getProvider().toString()))
                .toList();
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldScores() {
        java.time.LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<Score> oldScores = scoreRepository.findByPlayedAtBefore(startOfDay);
        scoreRepository.deleteAll(oldScores);
    }
}