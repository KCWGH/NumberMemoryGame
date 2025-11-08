package com.memorygame.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.memorygame.model.Score;
import com.memorygame.model.User;
import com.memorygame.repository.ScoreRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScoreService {

    private final ScoreRepository scoreRepository;

    @Transactional
    public void saveScore(User user, int scoreValue) {
        if (user != null && scoreValue > 0) {
            // 최근 10초 이내 동일 사용자의 동일한 점수가 있는지 확인
            boolean isDuplicate = scoreRepository.existsByUserAndScoreValueAndPlayedAtGreaterThan(
                user,
                scoreValue,
                LocalDateTime.now().minusSeconds(10)
            );
            
            if (isDuplicate) {
                throw new IllegalStateException("동일한 점수가 10초 이내에 이미 제출되었습니다");
            }
            
            Score score = Score.builder()
                .user(user)
                .scoreValue(scoreValue)
                .build();
            scoreRepository.save(score);
        }
    }

    // DTO를 사용하여 필요한 정보만 반환
    public List<ScoreResponseDto> getLeaderboard() {
        return scoreRepository.findTop10ByOrderByScoreValueDesc().stream()
            .map(score -> new ScoreResponseDto(score.getScoreValue(), score.getUser().getName()))
            .toList();
    }

    // 리더보드 응답을 위한 DTO
    @Getter
    @RequiredArgsConstructor
    public static class ScoreResponseDto {
        private final int scoreValue;
        private final String name; // user.name을 직접 사용

        // 프론트엔드 응답 형식에 맞추기 위해 JSON 프로퍼티 이름 조정
        public String getUser() {
            return name;
        }
    }
}