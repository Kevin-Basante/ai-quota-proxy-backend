package com.tollgate.service;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitRegistry {

    private final Map<String, AtomicInteger> requestsInWindow = new ConcurrentHashMap<>();
    private final Clock clock;

    public RateLimitRegistry(Clock clock) {
        this.clock = clock;
    }

    public boolean tryAcquire(String userId, int limit) {
        AtomicInteger counter = requestsInWindow.computeIfAbsent(userId, key -> new AtomicInteger());
        if (limit < 0) {
            counter.incrementAndGet();
            return true;
        }
        while (true) {
            int current = counter.get();
            if (current >= limit) {
                return false;
            }
            if (counter.compareAndSet(current, current + 1)) {
                return true;
            }
        }
    }

    public int requestsThisMinute(String userId) {
        AtomicInteger counter = requestsInWindow.get(userId);
        return counter == null ? 0 : counter.get();
    }

    public long secondsUntilReset() {
        int second = LocalTime.now(clock).getSecond();
        return Math.max(1, 60 - second);
    }

    public void resetAll() {
        requestsInWindow.clear();
    }
}
