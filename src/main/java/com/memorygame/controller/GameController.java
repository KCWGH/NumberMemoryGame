package com.memorygame.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.memorygame.dto.ScoreSubmissionDto;
import com.memorygame.dto.UserResponseDto;
import com.memorygame.model.User;
import com.memorygame.service.ScoreService;
import com.memorygame.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GameController {

    private final UserService userService;
    private final ScoreService scoreService;

    @GetMapping("/user")
    public ResponseEntity<UserResponseDto> getUserInfo(@AuthenticationPrincipal OAuth2User oauth2User) {
        return ResponseEntity.ok(userService.convertToDto(oauth2User));
    }

    @PostMapping("/score")
    public ResponseEntity<?> submitScore(@RequestBody ScoreSubmissionDto scoreDto,
            @AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        int score = scoreDto.getScore();
        if (score <= 0) {
            return ResponseEntity.badRequest().body("점수는 0보다 커야 합니다.");
        }

        User user = userService.getUserFromOAuth2User(oauth2User);
        scoreService.saveScore(user, score);
        return ResponseEntity.ok("점수가 성공적으로 기록되었습니다.");
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(@RequestParam(required = false) String filter,
            @AuthenticationPrincipal OAuth2User oauth2User) {

        if ("my".equalsIgnoreCase(filter)) {
            if (oauth2User == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            User user = userService.getUserFromOAuth2User(oauth2User);
            return ResponseEntity.ok(scoreService.getMyScores(user));
        }

        return ResponseEntity.ok(scoreService.getLeaderboard());
    }
}