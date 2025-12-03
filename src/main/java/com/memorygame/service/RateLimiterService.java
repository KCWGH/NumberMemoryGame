package com.memorygame.service;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private static final int MAX_REQUESTS = 10;
    private static final long TIME_WINDOW_MS = 10000;

    private final ConcurrentHashMap<String, Queue<Long>> requestTimestamps = new ConcurrentHashMap<>();

    public boolean allowRequest(String identifier) {
        long currentTime = System.currentTimeMillis();
        Queue<Long> timestamps = requestTimestamps.computeIfAbsent(identifier, k -> new ConcurrentLinkedQueue<>());

        timestamps.removeIf(timestamp -> currentTime - timestamp > TIME_WINDOW_MS);

        if (timestamps.size() >= MAX_REQUESTS) {
            return false;
        }

        timestamps.add(currentTime);
        return true;
    }

    public void cleanupOldEntries() {
        long currentTime = System.currentTimeMillis();
        requestTimestamps.entrySet().removeIf(entry -> {
            Queue<Long> timestamps = entry.getValue();
            timestamps.removeIf(timestamp -> currentTime - timestamp > TIME_WINDOW_MS);
            return timestamps.isEmpty();
        });
    }
}
