package com.tollgate.domain;

import java.time.LocalDate;

public class UserAccount {

    private final String id;
    private final String displayName;
    private final String role;
    private Plan plan;
    private long tokensUsed;
    private LocalDate quotaResetDate;

    public UserAccount(String id, String displayName, String role, Plan plan, long tokensUsed, LocalDate quotaResetDate) {
        this.id = id;
        this.displayName = displayName;
        this.role = role;
        this.plan = plan;
        this.tokensUsed = tokensUsed;
        this.quotaResetDate = quotaResetDate;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRole() {
        return role;
    }

    public synchronized Plan getPlan() {
        return plan;
    }

    public synchronized long getTokensUsed() {
        return tokensUsed;
    }

    public synchronized LocalDate getQuotaResetDate() {
        return quotaResetDate;
    }

    public synchronized long getTokensRemaining() {
        if (plan.hasUnlimitedTokens()) {
            return -1L;
        }
        return Math.max(0L, plan.getMonthlyTokenQuota() - tokensUsed);
    }

    public synchronized boolean isQuotaExhausted() {
        return !plan.hasUnlimitedTokens() && tokensUsed >= plan.getMonthlyTokenQuota();
    }

    public synchronized long consumeTokens(long tokens) {
        long charged = plan.hasUnlimitedTokens() ? tokens : Math.min(tokens, getTokensRemaining());
        tokensUsed += charged;
        return charged;
    }

    public synchronized void changePlan(Plan newPlan) {
        this.plan = newPlan;
    }

    public synchronized void startNewBillingPeriod(LocalDate nextResetDate) {
        this.tokensUsed = 0L;
        this.quotaResetDate = nextResetDate;
    }
}
