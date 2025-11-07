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

    /**
     * GET /api/leaderboard : 점수판 (리더보드) 조회
     */
    @GetMapping("/leaderboard")
    public List<Score> getLeaderboard() {
        return gameService.getLeaderboard();
    }

    /**
     * POST /api/score : 점수 제출 및 기록
     * @param oauth2User 현재 로그인한 사용자 정보 (Spring Security가 자동 주입)
     * @param scoreValue 쿼리 파라미터로 받은 점수 (프론트엔드에서 ?score=10 형태로 보냄)
     */
    @PostMapping("/score")
    public ResponseEntity<?> submitScore(
            @AuthenticationPrincipal OAuth2User oauth2User,
            @RequestParam("score") int scoreValue) {
        
        if (oauth2User == null) {
            // SecurityConfig에서 인증된 사용자만 접근 가능하도록 막았지만, 혹시 모를 경우 대비
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login required.");
        }
        
        try {
            // Google ID (sub)를 가져옵니다.
            String userId = oauth2User.getAttribute("sub"); 
            
            Score submittedScore = gameService.submitScore(userId, scoreValue);
            return ResponseEntity.ok(submittedScore);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Score submission failed.");
        }
    }
    
    /**
     * GET /api/user : 현재 로그인한 사용자 정보 조회 (선택 사항)
     */
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