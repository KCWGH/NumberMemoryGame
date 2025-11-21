package com.memorygame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.beans.factory.annotation.Autowired;
import com.memorygame.service.ScoreService;

@SpringBootApplication
@EnableScheduling
public class MemoryGameApplication {

    @Autowired
    private ScoreService scoreService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        scoreService.deleteOldScores();
    }

    public static void main(String[] args) {
        SpringApplication.run(MemoryGameApplication.class, args);
    }
}