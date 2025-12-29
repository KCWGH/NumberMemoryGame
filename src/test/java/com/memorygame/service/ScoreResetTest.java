package com.memorygame.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.memorygame.dto.ScoreRecordDto;
import com.memorygame.model.ProviderType;
import com.memorygame.model.Score;
import com.memorygame.model.User;
import com.memorygame.repository.ScoreRepository;
import com.memorygame.repository.UserRepository;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ScoreResetTest {

        @Autowired
        private ScoreService scoreService;

        @Autowired
        private ScoreRepository scoreRepository;

        @Autowired
        private UserRepository userRepository;

        @SuppressWarnings("null")
        @Test
        void testDailyScoreResetLogic() {
                // Given
                User user = User.builder()
                                .provider(ProviderType.GOOGLE)
                                .providerId("12345")
                                .name("TestUser")
                                .email("test@example.com")
                                .build();
                userRepository.save(user);

                LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
                LocalDateTime today = LocalDateTime.now();

                Score oldScore = Score.builder()
                                .user(user)
                                .scoreValue(100)
                                .playedAt(yesterday)
                                .build();
                scoreRepository.save(oldScore);

                Score newScore = Score.builder()
                                .user(user)
                                .scoreValue(200)
                                .playedAt(today)
                                .build();
                scoreRepository.save(newScore);

                // When: Get Leaderboard
                List<ScoreRecordDto> leaderboard = scoreService.getLeaderboard();

                // Then: Should only show today's score
                assertThat(leaderboard).hasSize(1);
                assertThat(leaderboard.get(0).getScoreValue()).isEqualTo(200);

                // When: Delete Old Scores
                scoreService.deleteOldScores();

                // Then: Old score should be deleted from DB
                assertThat(scoreRepository.findAll()).hasSize(1);
                assertThat(scoreRepository.findAll().get(0).getScoreValue()).isEqualTo(200);
        }
}
