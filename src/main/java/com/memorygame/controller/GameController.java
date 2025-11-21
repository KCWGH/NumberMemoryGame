package com.memorygame.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.memorygame.dto.ScoreSubmissionDto;
import com.memorygame.dto.UserResponseDto;
import com.memorygame.model.ProviderType;
import com.memorygame.model.User;
import com.memorygame.repository.UserRepository;
import com.memorygame.service.ScoreService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GameController {

    private final UserRepository userRepository;
    private final ScoreService scoreService;

    @GetMapping("/user")
    public ResponseEntity<UserResponseDto> getUserInfo(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return ResponseEntity.ok(UserResponseDto.builder()
                    .authenticated(false)
                    .build());
        }

        try {
            OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext()
                    .getAuthentication();
            String registrationId = authentication.getAuthorizedClientRegistrationId();
            ProviderType providerType = ProviderType.valueOf(registrationId.toUpperCase());
            String providerId = oauth2User.getName();

            User user = userRepository.findByProviderAndProviderId(providerType, providerId)
                    .orElseThrow(() -> new IllegalStateException("인증된 사용자를 찾을 수 없습니다."));

            return ResponseEntity.ok(UserResponseDto.builder()
                    .authenticated(true)
                    .name(user.getName())
                    .email(user.getEmail())
                    .provider(user.getProvider().toString())
                    .build());

        } catch (Exception e) {
            String name = oauth2User.getAttribute("name");
            String email = oauth2User.getAttribute("email");

            if (name == null && email == null) {
                name = "인증된 사용자";
            }

            return ResponseEntity.ok(UserResponseDto.builder()
                    .authenticated(true)
                    .name(name != null ? name : email)
                    .email(email)
                    .provider("UNKNOWN")
                    .build());
        }
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

        String providerId = oauth2User.getName();

        OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext()
                .getAuthentication();
        String registrationId = authentication.getAuthorizedClientRegistrationId();
        ProviderType providerType = ProviderType.valueOf(registrationId.toUpperCase());

        User user = userRepository.findByProviderAndProviderId(providerType, providerId)
                .orElseThrow(() -> new IllegalStateException("인증된 사용자를 DB에서 찾을 수 없습니다."));

        scoreService.saveScore(user, score);
        return ResponseEntity.ok("점수가 성공적으로 기록되었습니다.");
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        return ResponseEntity.ok(scoreService.getLeaderboard());
    }
}