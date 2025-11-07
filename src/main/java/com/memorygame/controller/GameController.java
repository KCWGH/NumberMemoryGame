package com.memorygame.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.memorygame.model.Score;
import com.memorygame.service.GameService;

@RestController
@RequestMapping("/api")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/leaderboard")
    public List<Score> getLeaderboard() {
        return gameService.getLeaderboard();
    }

    @PostMapping("/score")
    public ResponseEntity<?> submitScore(
            @AuthenticationPrincipal OAuth2User oauth2User,
            @RequestParam("score") int scoreValue) {

        if (oauth2User == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login required.");
        }

        try {
            String userId = oauth2User.getAttribute("sub");

            Score submittedScore = gameService.submitScore(userId, scoreValue);
            return ResponseEntity.ok(submittedScore);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Score submission failed.");
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User != null) {
            return ResponseEntity.ok(
                "User: " + oauth2User.getAttribute("name") +
                ", ID: " + oauth2User.getAttribute("sub")
            );
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in.");
    }
}