package com.tollgate.dto;

import java.time.LocalDate;

public record DailyUsageView(LocalDate date, String dayLabel, long tokens, int requests) {
}
