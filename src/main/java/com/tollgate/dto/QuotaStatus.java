package com.tollgate.dto;

import com.tollgate.domain.Plan;

import java.time.LocalDate;

public record QuotaStatus(
        String userId,
        String displayName,
        Plan plan,
        long tokensUsed,
        Long tokensRemaining,
        Long tokenQuota,
        boolean unlimitedTokens,
        double usagePercent,
        LocalDate resetDate,
        int requestsThisMinute,
        Integer requestsPerMinuteLimit,
        boolean unlimitedRequests,
        long secondsUntilRateReset
) {
}
