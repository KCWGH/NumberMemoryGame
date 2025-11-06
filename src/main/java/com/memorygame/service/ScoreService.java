package com.memorygame.service;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.memorygame.dto.ScoreRequest;
import com.memorygame.model.Score;
import com.memorygame.model.User;
import com.memorygame.repository.ScoreRepository;
import com.memorygame.repository.UserRepository;

@Service
public class ScoreService {

    private final UserRepository userRepository;
    private final ScoreRepository scoreRepository;

    public ScoreService(UserRepository userRepository, ScoreRepository scoreRepository) {
        this.userRepository = userRepository;
        this.scoreRepository = scoreRepository;
    }

    @Transactional
    public void submitScore(OAuth2User principal, ScoreRequest request) {
        if (principal == null) {
            return;
        }

        String googleId = principal.getAttribute("sub");

        User user = userRepository.findByGoogleId(googleId)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setGoogleId(googleId);
                String name = principal.getAttribute("name");
                String email = principal.getAttribute("email");
                newUser.setName(name != null ? name : "");
                newUser.setEmail(email != null ? email : "");
                return userRepository.save(newUser);
            });

        Score score = new Score();
        score.setUser(user);
        score.setScore(request.getScore());
        score.setTotalPlayTimeSeconds(request.getTotalPlayTimeSeconds());
        score.setStagesCompleted(request.getStagesCompleted());
        scoreRepository.save(score);
    }
}
