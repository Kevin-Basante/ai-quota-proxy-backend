package com.tollgate.service;

import org.springframework.stereotype.Component;

@Component
public class TokenEstimator {

    private static final double CHARACTERS_PER_TOKEN = 4.0;

    public int estimate(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return (int) Math.ceil(text.trim().length() / CHARACTERS_PER_TOKEN);
    }
}
