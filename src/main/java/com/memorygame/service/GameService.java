package com.memorygame.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.memorygame.model.Score;
import com.memorygame.model.User;
import com.memorygame.repository.ScoreRepository;
import com.memorygame.repository.UserRepository;

@Service
public class GameService {

    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;

    public GameService(ScoreRepository scoreRepository, UserRepository userRepository) {
        this.scoreRepository = scoreRepository;
        this.userRepository = userRepository;
    }

    /**
     * 점수를 기록하고 DB에 저장합니다.
     * @param userId 현재 로그인한 사용자의 ID (Google sub)
     * @param scoreValue 사용자가 획득한 점수
     * @return 저장된 Score 엔티티
     */
    @Transactional
    public Score submitScore(String userId, int scoreValue) {
        // 1. 사용자 ID로 User 엔티티를 찾습니다.
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // 2. Score 엔티티 생성 및 저장
        Score newScore = new Score();
        newScore.setScoreValue(scoreValue);
        newScore.setUser(user);
        
        return scoreRepository.save(newScore);
    }

    /**
     * 최고 점수 10개를 조회하여 반환합니다.
     * @return 상위 10개의 Score 리스트
     */
    @Transactional(readOnly = true)
    public List<Score> getLeaderboard() {
        return scoreRepository.findTop10ByScoreValueDesc();
    }
}