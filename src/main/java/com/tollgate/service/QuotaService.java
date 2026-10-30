package com.tollgate.service;

import com.tollgate.domain.BillingPeriod;
import com.tollgate.domain.DailyUsage;
import com.tollgate.domain.Plan;
import com.tollgate.domain.UserAccount;
import com.tollgate.dto.DailyUsageView;
import com.tollgate.dto.QuotaStatus;
import com.tollgate.dto.UpgradeReceipt;
import com.tollgate.dto.UpgradeRequest;
import com.tollgate.dto.UsageHistory;
import com.tollgate.exception.InvalidUpgradeException;
import com.tollgate.repository.UsageHistoryRepository;
import com.tollgate.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

@Service
public class QuotaService {

    private static final int HISTORY_DAYS = 7;

    private final UserAccountRepository accountRepository;
    private final UsageHistoryRepository usageHistoryRepository;
    private final RateLimitRegistry rateLimitRegistry;
    private final PaymentSimulator paymentSimulator;
    private final Clock clock;

    public QuotaService(UserAccountRepository accountRepository, UsageHistoryRepository usageHistoryRepository,
                        RateLimitRegistry rateLimitRegistry, PaymentSimulator paymentSimulator, Clock clock) {
        this.accountRepository = accountRepository;
        this.usageHistoryRepository = usageHistoryRepository;
        this.rateLimitRegistry = rateLimitRegistry;
        this.paymentSimulator = paymentSimulator;
        this.clock = clock;
    }

    public QuotaStatus status(String userId) {
        UserAccount account = accountRepository.getById(userId);
        Plan plan = account.getPlan();
        long used = account.getTokensUsed();
        boolean unlimitedTokens = plan.hasUnlimitedTokens();
        double usagePercent = unlimitedTokens ? 0.0 : Math.min(100.0, used * 100.0 / plan.getMonthlyTokenQuota());
        return new QuotaStatus(
                account.getId(),
                account.getDisplayName(),
                plan,
                used,
                unlimitedTokens ? null : account.getTokensRemaining(),
                unlimitedTokens ? null : plan.getMonthlyTokenQuota(),
                unlimitedTokens,
                Math.round(usagePercent * 100.0) / 100.0,
                account.getQuotaResetDate(),
                rateLimitRegistry.requestsThisMinute(userId),
                plan.hasUnlimitedRequests() ? null : plan.getRequestsPerMinute(),
                plan.hasUnlimitedRequests(),
                rateLimitRegistry.secondsUntilReset()
        );
    }

    public UsageHistory history(String userId) {
        accountRepository.getById(userId);
        LocalDate today = LocalDate.now(clock);
        List<DailyUsageView> days = IntStream.rangeClosed(0, HISTORY_DAYS - 1)
                .mapToObj(offset -> today.minusDays(HISTORY_DAYS - 1L - offset))
                .map(date -> toView(usageHistoryRepository.find(userId, date)))
                .toList();
        long totalTokens = days.stream().mapToLong(DailyUsageView::tokens).sum();
        int totalRequests = days.stream().mapToInt(DailyUsageView::requests).sum();
        return new UsageHistory(userId, totalTokens, totalRequests, days);
    }

    public UpgradeReceipt upgrade(String userId, UpgradeRequest request) {
        UserAccount account = accountRepository.getById(userId);
        Plan previousPlan = account.getPlan();
        if (!previousPlan.isUpgradeTo(request.targetPlan())) {
            throw new InvalidUpgradeException("Cannot move from " + previousPlan + " to " + request.targetPlan());
        }
        PaymentSimulator.PaymentConfirmation confirmation = paymentSimulator.charge(
                request.cardHolder(), request.cardNumber(), request.targetPlan().getMonthlyPriceUsd());
        account.changePlan(request.targetPlan());
        return new UpgradeReceipt(
                confirmation.transactionId(),
                previousPlan,
                request.targetPlan(),
                confirmation.amountUsd(),
                confirmation.cardLast4(),
                confirmation.paidAt(),
                status(userId)
        );
    }

    public void resetMonthlyQuotas() {
        LocalDate nextResetDate = BillingPeriod.nextResetDate(LocalDate.now(clock));
        accountRepository.findAll().forEach(account -> account.startNewBillingPeriod(nextResetDate));
    }

    private DailyUsageView toView(DailyUsage usage) {
        String label = usage.getDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        return new DailyUsageView(usage.getDate(), label, usage.getTokens(), usage.getRequests());
    }
}
