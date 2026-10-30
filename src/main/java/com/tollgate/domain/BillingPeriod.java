package com.tollgate.domain;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public final class BillingPeriod {

    private BillingPeriod() {
    }

    public static LocalDate nextResetDate(LocalDate today) {
        return today.with(TemporalAdjusters.firstDayOfNextMonth());
    }
}
