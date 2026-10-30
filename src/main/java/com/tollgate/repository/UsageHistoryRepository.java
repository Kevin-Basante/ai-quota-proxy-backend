package com.tollgate.repository;

import com.tollgate.domain.DailyUsage;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UsageHistoryRepository {

    private final Map<String, Map<LocalDate, DailyUsage>> usageByUser = new ConcurrentHashMap<>();

    public void register(String userId, LocalDate date, long tokens) {
        dayOf(userId, date).register(tokens);
    }

    public void seed(String userId, LocalDate date, long tokens, int requests) {
        usageByUser.computeIfAbsent(userId, key -> new ConcurrentHashMap<>())
                .put(date, new DailyUsage(date, tokens, requests));
    }

    public DailyUsage find(String userId, LocalDate date) {
        return usageByUser.getOrDefault(userId, Map.of()).getOrDefault(date, new DailyUsage(date, 0L, 0));
    }

    public void deleteAll() {
        usageByUser.clear();
    }

    private DailyUsage dayOf(String userId, LocalDate date) {
        return usageByUser.computeIfAbsent(userId, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(date, key -> new DailyUsage(key, 0L, 0));
    }
}
