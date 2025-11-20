package com.memorygame.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.memorygame.dto.ScoreResponseDto;
import com.memorygame.model.ProviderType;
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

    @GetMapping("/user")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal OAuth2User oauth2User) {
        Map<String, Object> userInfo = new HashMap<>();

        if (oauth2User == null) {
            userInfo.put("authenticated", false);
            return ResponseEntity.ok(userInfo);
        }

        userInfo.put("authenticated", true);

        try {
            OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext()
                    .getAuthentication();
            String registrationId = authentication.getAuthorizedClientRegistrationId();
            ProviderType providerType = ProviderType.valueOf(registrationId.toUpperCase());
            String providerId = oauth2User.getName();

            User user = userRepository.findByProviderAndProviderId(providerType, providerId)
                    .orElseThrow(() -> new IllegalStateException("인증된 사용자를 찾을 수 없습니다."));

            userInfo.put("name", user.getName());
            userInfo.put("email", user.getEmail());

        } catch (Exception e) {
            String name = oauth2User.getAttribute("name");
            String email = oauth2User.getAttribute("email");

            userInfo.put("name", name != null ? name : email);
            userInfo.put("email", email);

            if (userInfo.get("name") == null && userInfo.get("email") == null) {
                userInfo.put("name", "인증된 사용자");
            }
        }

        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/score")
    public ResponseEntity<?> submitScore(@RequestParam int score, @AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        if (score <= 0) {
            return ResponseEntity.badRequest().body("점수는 0보다 커야 합니다.");
        }

        String providerId = oauth2User.getName();

        try {
            OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext()
                    .getAuthentication();
            String registrationId = authentication.getAuthorizedClientRegistrationId();
            ProviderType providerType = ProviderType.valueOf(registrationId.toUpperCase());

            User user = userRepository.findByProviderAndProviderId(providerType, providerId)
                    .orElseThrow(() -> new IllegalStateException("인증된 사용자를 DB에서 찾을 수 없습니다."));

            scoreService.saveScore(user, score);
            return ResponseEntity.ok("점수가 성공적으로 기록되었습니다.");

        } catch (ClassCastException | NullPointerException e) {
            return new ResponseEntity<>("인증 정보가 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("지원되지 않는 로그인 공급자입니다.", HttpStatus.UNAUTHORIZED);
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<ScoreResponseDto>> getLeaderboard() {
        List<ScoreResponseDto> scores = scoreService.getLeaderboard();
        return ResponseEntity.ok(scores);
    }
}