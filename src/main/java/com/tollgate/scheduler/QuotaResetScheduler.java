package com.tollgate.scheduler;

import com.tollgate.service.QuotaService;
import com.tollgate.service.RateLimitRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QuotaResetScheduler {

    private final RateLimitRegistry rateLimitRegistry;
    private final QuotaService quotaService;

    public QuotaResetScheduler(RateLimitRegistry rateLimitRegistry, QuotaService quotaService) {
        this.rateLimitRegistry = rateLimitRegistry;
        this.quotaService = quotaService;
    }

    @Scheduled(cron = "0 * * * * *", zone = "${app.time-zone}")
    public void resetRateLimitWindow() {
        rateLimitRegistry.resetAll();
    }

    @Scheduled(cron = "0 0 0 1 * *", zone = "${app.time-zone}")
    public void resetMonthlyQuotas() {
        quotaService.resetMonthlyQuotas();
    }
}
