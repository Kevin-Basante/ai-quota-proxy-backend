package com.tollgate.domain;

import java.time.LocalDate;

public class DailyUsage {

    private final LocalDate date;
    private long tokens;
    private int requests;

    public DailyUsage(LocalDate date, long tokens, int requests) {
        this.date = date;
        this.tokens = tokens;
        this.requests = requests;
    }

    public LocalDate getDate() {
        return date;
    }

    public synchronized long getTokens() {
        return tokens;
    }

    public synchronized int getRequests() {
        return requests;
    }

    public synchronized void register(long consumedTokens) {
        this.tokens += consumedTokens;
        this.requests += 1;
    }
}
