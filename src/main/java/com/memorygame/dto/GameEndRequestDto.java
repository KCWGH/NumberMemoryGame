package com.memorygame.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GameEndRequestDto {
    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @Min(value = 1, message = "Stage must be at least 1")
    private int stage;

    @Min(value = 0, message = "Clicks cannot be negative")
    private int clicksInCurrentStage;
}
