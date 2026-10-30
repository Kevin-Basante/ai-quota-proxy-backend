package com.tollgate.dto;

import com.tollgate.domain.Plan;

import java.math.BigDecimal;
import java.time.Instant;

public record UpgradeReceipt(
        String transactionId,
        Plan previousPlan,
        Plan newPlan,
        BigDecimal amountUsd,
        String cardLast4,
        Instant paidAt,
        QuotaStatus quota
) {
}
