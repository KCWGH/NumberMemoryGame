package com.memorygame.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
			return new ResponseEntity<>("Please log in first.", HttpStatus.UNAUTHORIZED); // 401
		}

		if (score <= 0) {
			return ResponseEntity.badRequest().body("Score must be greater than 0.");
		}

		// 3. OAuth2 ID(providerId)를 사용하여 DB에서 실제 User 엔티티 조회
		String providerId = oauth2User.getName(); // Google sub ID

		User user = userRepository.findByProviderId(providerId)
				.orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB."));

		// 4. 영속 User 엔티티를 Service에 전달
		scoreService.saveScore(user, score);

		return ResponseEntity.ok("Score submitted successfully.");
	}

	// 리더보드 조회 엔드포인트 (GET /api/leaderboard)
	@GetMapping("/leaderboard")
	public ResponseEntity<List<Map<String, Object>>> getLeaderboard() {
		List<ScoreService.ScoreResponseDto> scores = scoreService.getLeaderboard();

		// 프론트엔드의 기대 응답 형식: [{ user: { name: "...", ... }, scoreValue: 123 }, ...]
		// 여기서는 ScoreResponseDto를 List<Map<String, Object>> 형태로 변환하여 JSON 응답
		List<Map<String, Object>> response = scores.stream().map(dto -> {
			return Map.of("scoreValue", dto.getScoreValue(),
					// 'user' 객체 안에 'name'을 넣는 프론트엔드의 응답 구조에 맞추기 위해 사용
					"user", Map.of("name", dto.getName()));
		}).toList();

		return ResponseEntity.ok(response);
	}
}