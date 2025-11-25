package com.memorygame.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.memorygame.dto.ScoreResponseDto;
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

            // 1. 사용자(user)의 동일한 점수(scoreValue)가 오늘 날짜 내에 이미 존재하는지 확인
            java.time.LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            java.time.LocalDateTime endOfDay = LocalDate.now().atTime(java.time.LocalTime.MAX);

            boolean isDuplicate = scoreRepository.existsByUserAndScoreValueAndPlayedAtBetween(user, scoreValue,
                    startOfDay, endOfDay);

            if (isDuplicate) {
                // 중복 점수가 이미 존재하는 경우, 저장을 막고 예외 발생
                throw new IllegalStateException("동일한 점수(" + scoreValue + ")가 오늘 이미 기록되어 있습니다.");
            }

            // 2. 중복이 아니면 저장 진행
            Score score = Score.builder()
                    .user(user)
                    .scoreValue(scoreValue)
                    .build();
            scoreRepository.save(score);
        }
    }

    // DTO를 사용하여 필요한 정보만 반환
    public List<ScoreResponseDto> getLeaderboard() {
        return scoreRepository.findTop10ByOrderByScoreValueDesc(LocalDate.now().atStartOfDay()).stream()
                // DTO 생성자 변경: (scoreValue, userName, provider)
                .map(score -> new ScoreResponseDto(
                        score.getScoreValue(),
                        score.getUser().getName(),
                        score.getUser().getProvider().toString()))
                .toList();
    }

    public List<ScoreResponseDto> getMyScores(User user) {
        return scoreRepository.findByUserOrderByPlayedAtDesc(user).stream()
                .map(score -> new ScoreResponseDto(
                        score.getScoreValue(),
                        score.getUser().getName(),
                        score.getUser().getProvider().toString()))
                .toList();
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldScores() {
        scoreRepository.deleteByPlayedAtBefore(LocalDate.now().atStartOfDay());
    }
}