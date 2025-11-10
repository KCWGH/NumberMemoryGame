package com.memorygame.service;

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
            
            // 1. 사용자(user)의 동일한 점수(scoreValue)가 DB에 이미 존재하는지 확인
            boolean isDuplicate = scoreRepository.existsByUserAndScoreValue(user, scoreValue);
            
            if (isDuplicate) {
                // 중복 점수가 이미 존재하는 경우, 저장을 막고 예외 발생
                throw new IllegalStateException("동일한 점수(" + scoreValue + ")가 해당 사용자 ID로 이미 기록되어 있습니다.");
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