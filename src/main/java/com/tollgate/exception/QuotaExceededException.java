package com.tollgate.exception;

import com.tollgate.domain.Plan;

import java.time.LocalDate;

public class QuotaExceededException extends RuntimeException {

    private final Plan plan;
    private final long tokensUsed;
    private final long tokenQuota;
    private final LocalDate resetDate;

    public QuotaExceededException(Plan plan, long tokensUsed, long tokenQuota, LocalDate resetDate) {
        super("Monthly token quota of the " + plan + " plan is exhausted");
        this.plan = plan;
        this.tokensUsed = tokensUsed;
        this.tokenQuota = tokenQuota;
        this.resetDate = resetDate;
    }

    public Plan getPlan() {
        return plan;
    }

    public long getTokensUsed() {
        return tokensUsed;
    }

    public long getTokenQuota() {
        return tokenQuota;
    }

    public LocalDate getResetDate() {
        return resetDate;
    }
}
