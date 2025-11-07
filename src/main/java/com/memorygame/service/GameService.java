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

    @Transactional
    public Score submitScore(String userId, int scoreValue) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Score newScore = new Score();
        newScore.setScoreValue(scoreValue);
        newScore.setUser(user);

        return scoreRepository.save(newScore);
    }

    @Transactional(readOnly = true)
    public List<Score> getLeaderboard() {
        return scoreRepository.findTop10ByScoreValueDesc();
    }
}