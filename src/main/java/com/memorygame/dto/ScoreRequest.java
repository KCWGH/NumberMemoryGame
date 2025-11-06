package com.memorygame.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ScoreRequest {

    @NotNull
    @Min(0)
    private Integer score;

    @NotNull
    @Min(0)
    private Integer stagesCompleted;

    @NotNull
    @Min(0)
    private Double totalPlayTimeSeconds;

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getStagesCompleted() {
        return stagesCompleted;
    }

    public void setStagesCompleted(Integer stagesCompleted) {
        this.stagesCompleted = stagesCompleted;
    }

    public Double getTotalPlayTimeSeconds() {
        return totalPlayTimeSeconds;
    }

    public void setTotalPlayTimeSeconds(Double totalPlayTimeSeconds) {
        this.totalPlayTimeSeconds = totalPlayTimeSeconds;
    }
}





