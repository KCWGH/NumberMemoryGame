package com.memorygame.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ScoreRecordDto {
    private final int scoreValue;
    private final String user;
    private final String provider;
}
