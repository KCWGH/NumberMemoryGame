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

import com.memorygame.dto.ScoreResponseDto;
import com.memorygame.model.User;
import com.memorygame.repository.UserRepository;
import com.memorygame.service.ScoreService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GameController {

	private final ScoreService scoreService;

	private final UserRepository userRepository;

	// 점수 제출 엔드포인트 (POST /api/score?score=123)
	// 인증된 사용자만 접근 가능 (SecurityConfig에서 설정)
	@PostMapping("/score")
    public ResponseEntity<?> submitScore(@RequestParam int score, @AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return new ResponseEntity<>("Please log in first.", HttpStatus.UNAUTHORIZED);
        }

        if (score <= 0) {
            return ResponseEntity.badRequest().body("Score must be greater than 0.");
        }

        try {
            String providerId = oauth2User.getName();
            User user = userRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB."));
            
            scoreService.saveScore(user, score);
            return ResponseEntity.ok("Score submitted successfully.");
            
        } catch (IllegalStateException e) {
            // 중복 점수 제출이나 기타 예외 처리
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(e.getMessage());
        }
    }

	// 리더보드 조회 엔드포인트 (GET /api/leaderboard)
	@GetMapping("/leaderboard")
    public ResponseEntity<List<ScoreResponseDto>> getLeaderboard() {
        List<ScoreResponseDto> scores = scoreService.getLeaderboard();
        
        return ResponseEntity.ok(scores);
	}
}