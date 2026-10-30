package com.tollgate.domain;

import java.math.BigDecimal;

public enum Plan {
    FREE(10, 50_000L, new BigDecimal("0.00"), 0),
    PRO(60, 500_000L, new BigDecimal("19.00"), 1),
    ENTERPRISE(-1, -1L, new BigDecimal("199.00"), 2);

    private final int requestsPerMinute;
    private final long monthlyTokenQuota;
    private final BigDecimal monthlyPriceUsd;
    private final int tier;

    Plan(int requestsPerMinute, long monthlyTokenQuota, BigDecimal monthlyPriceUsd, int tier) {
        this.requestsPerMinute = requestsPerMinute;
        this.monthlyTokenQuota = monthlyTokenQuota;
        this.monthlyPriceUsd = monthlyPriceUsd;
        this.tier = tier;
    }

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public long getMonthlyTokenQuota() {
        return monthlyTokenQuota;
    }

    public BigDecimal getMonthlyPriceUsd() {
        return monthlyPriceUsd;
    }

    public boolean hasUnlimitedRequests() {
        return requestsPerMinute < 0;
    }

    public boolean hasUnlimitedTokens() {
        return monthlyTokenQuota < 0;
    }

    public boolean isUpgradeTo(Plan target) {
        return target != null && target.tier > this.tier;
    }
}
