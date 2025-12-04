package com.memorygame.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.memorygame.model.Score;

@Getter
@RequiredArgsConstructor
public class ScoreRecordDto {
    private final int scoreValue;
    private final String user;
    private final String provider;

    public static ScoreRecordDto from(Score score) {
        return new ScoreRecordDto(
                score.getScoreValue(),
                score.getUser().getName(),
                score.getUser().getProvider().toString());
    }
}
