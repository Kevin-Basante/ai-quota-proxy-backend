package com.tollgate.dto;

import java.util.List;

public record UsageHistory(String userId, long totalTokens, int totalRequests, List<DailyUsageView> days) {
}
