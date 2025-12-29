package com.memorygame.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StageCompleteRequestDto {

    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @NotNull(message = "Stage is required")
    @Min(value = 1, message = "Stage must be at least 1")
    private Integer stage;

    @NotNull(message = "Score gained is required")
    @Min(value = 0, message = "Score gained must be at least 0")
    private Integer scoreGained;
}
