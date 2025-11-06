package com.memorygame.controller;

import com.memorygame.dto.ScoreRequest;
import com.memorygame.model.Score;
import com.memorygame.repository.ScoreRepository;
import com.memorygame.service.ScoreService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
public class GameController {

    private final ScoreService scoreService;
    private final ScoreRepository scoreRepository;

    public GameController(ScoreService scoreService, ScoreRepository scoreRepository) {
        this.scoreService = scoreService;
        this.scoreRepository = scoreRepository;
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Service is alive");
    }

    @PostMapping("/score")
    public ResponseEntity<Void> submitScore(@AuthenticationPrincipal OAuth2User principal,
                            @RequestBody @Valid ScoreRequest request) {
        scoreService.submitScore(principal, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/leaderboard")
    public List<Score> getLeaderboard() {
        return scoreRepository.findTop10ByOrderByScoreDesc();
    }
}
