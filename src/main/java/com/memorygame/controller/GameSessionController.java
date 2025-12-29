package com.memorygame.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.memorygame.dto.GameEndRequestDto;
import com.memorygame.dto.GameStartResponseDto;
import com.memorygame.dto.StageCompleteRequestDto;
import com.memorygame.model.User;
import com.memorygame.service.GameService;
import com.memorygame.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameSessionController {

    private final UserService userService;
    private final GameService gameService;

    @PostMapping("/start")
    public ResponseEntity<GameStartResponseDto> startGame(@AuthenticationPrincipal OAuth2User oauth2User) {
        User user = null;
        if (oauth2User != null) {
            user = userService.getUserFromOAuth2User(oauth2User);
        }
        return ResponseEntity.ok(gameService.startGame(user));
    }

    @PostMapping("/stage")
    public ResponseEntity<?> completeStage(@Valid @RequestBody StageCompleteRequestDto request) {
        String result = gameService.completeStage(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/end")
    public ResponseEntity<?> endGame(@Valid @RequestBody GameEndRequestDto request) {
        String result = gameService.endGame(request);
        return ResponseEntity.ok(result);
    }
}
