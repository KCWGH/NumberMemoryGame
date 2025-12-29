package com.memorygame.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.memorygame.dto.ScoreRecordDto;
import com.memorygame.exception.DuplicateScoreException;
import com.memorygame.model.Score;
import com.memorygame.model.User;
import com.memorygame.repository.ScoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScoreService {

    private final ScoreRepository scoreRepository;

    @SuppressWarnings("null")
    @Transactional
    public void saveScore(User user, int scoreValue) {
        if (user != null && scoreValue > 0) {
            ZoneId kstZone = ZoneId.of("Asia/Seoul");
            ZonedDateTime nowKst = ZonedDateTime.now(kstZone);

            ZonedDateTime startOfDayKst = nowKst.toLocalDate().atStartOfDay(kstZone);
            ZonedDateTime endOfDayKst = nowKst.toLocalDate().atTime(LocalTime.MAX).atZone(kstZone);

            LocalDateTime startOfDaySystem = startOfDayKst.withZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDateTime();
            LocalDateTime endOfDaySystem = endOfDayKst.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

            boolean isDuplicate = scoreRepository.existsByUserAndScoreValueAndPlayedAtBetween(user, scoreValue,
                    startOfDaySystem, endOfDaySystem);

            if (isDuplicate) {
                throw new DuplicateScoreException("동일한 점수(" + scoreValue + ")가 오늘 이미 기록되어 있습니다.");
            }

            Score score = Score.builder()
                    .user(user)
                    .scoreValue(scoreValue)
                    .build();
            scoreRepository.save(score);
        }
    }

    public List<ScoreRecordDto> getLeaderboard() {
        return scoreRepository.findTop10ByOrderByScoreValueDesc().stream()
                .map(ScoreRecordDto::from)
                .toList();
    }

    public List<ScoreRecordDto> getMyScores(User user) {
        return scoreRepository.findByUserOrderByScoreValueDesc(user).stream()
                .map(ScoreRecordDto::from)
                .toList();
    }

    @SuppressWarnings("null")
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void deleteOldScores() {
        ZoneId kstZone = ZoneId.of("Asia/Seoul");
        ZonedDateTime startOfDayKst = LocalDate.now(kstZone).atStartOfDay(kstZone);

        LocalDateTime startOfDaySystem = startOfDayKst.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

        List<Score> oldScores = scoreRepository.findByPlayedAtBefore(startOfDaySystem);
        scoreRepository.deleteAll(oldScores);
    }
}