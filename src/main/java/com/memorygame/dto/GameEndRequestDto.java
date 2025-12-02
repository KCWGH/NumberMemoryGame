package com.memorygame.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GameEndRequestDto {
    private String sessionId;
    private int stage;
    private int clicksInCurrentStage;
}
