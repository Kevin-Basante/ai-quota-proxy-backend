package com.tollgate.service;

import com.tollgate.domain.BillingPeriod;
import com.tollgate.domain.Plan;
import com.tollgate.domain.UserAccount;
import com.tollgate.dto.PlanView;
import com.tollgate.dto.UserSummary;
import com.tollgate.repository.UsageHistoryRepository;
import com.tollgate.repository.UserAccountRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class DemoDataService implements ApplicationRunner {

    private static final double[] WEEKLY_SHAPE = {0.11, 0.17, 0.09, 0.21, 0.14, 0.19};
    private static final int AVERAGE_TOKENS_PER_REQUEST = 140;

    private final UserAccountRepository accountRepository;
    private final UsageHistoryRepository usageHistoryRepository;
    private final RateLimitRegistry rateLimitRegistry;
    private final Clock clock;

    public DemoDataService(UserAccountRepository accountRepository, UsageHistoryRepository usageHistoryRepository,
                           RateLimitRegistry rateLimitRegistry, Clock clock) {
        this.accountRepository = accountRepository;
        this.usageHistoryRepository = usageHistoryRepository;
        this.rateLimitRegistry = rateLimitRegistry;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        reset();
    }

    public synchronized void reset() {
        accountRepository.deleteAll();
        usageHistoryRepository.deleteAll();
        rateLimitRegistry.resetAll();
        createAccount("aurora", "Aurora Vega", "Indie writer", Plan.FREE, 49_420L);
        createAccount("nova", "Nova Ruiz", "Student", Plan.FREE, 6_850L);
        createAccount("atlas", "Atlas Moreno", "Startup engineer", Plan.PRO, 212_300L);
        createAccount("orion", "Orion Labs", "Research team", Plan.ENTERPRISE, 1_482_900L);
    }

    public List<UserSummary> users() {
        return accountRepository.findAll().stream()
                .sorted(Comparator.comparing(UserAccount::getPlan).thenComparing(UserAccount::getDisplayName))
                .map(account -> new UserSummary(account.getId(), account.getDisplayName(), account.getRole(), account.getPlan()))
                .toList();
    }

    public List<PlanView> plans() {
        return Arrays.stream(Plan.values())
                .map(plan -> new PlanView(
                        plan,
                        plan.hasUnlimitedRequests() ? null : plan.getRequestsPerMinute(),
                        plan.hasUnlimitedTokens() ? null : plan.getMonthlyTokenQuota(),
                        plan.getMonthlyPriceUsd()))
                .toList();
    }

    private void createAccount(String id, String displayName, String role, Plan plan, long tokensUsed) {
        LocalDate today = LocalDate.now(clock);
        accountRepository.save(new UserAccount(id, displayName, role, plan, tokensUsed, BillingPeriod.nextResetDate(today)));
        long weeklyBudget = Math.round(Math.min(tokensUsed, tokensUsed * 0.6));
        for (int index = 0; index < WEEKLY_SHAPE.length; index++) {
            LocalDate date = today.minusDays(WEEKLY_SHAPE.length - index);
            if (date.getMonth() != today.getMonth()) {
                continue;
            }
            long tokens = Math.round(weeklyBudget * WEEKLY_SHAPE[index]);
            int requests = (int) Math.max(1, tokens / AVERAGE_TOKENS_PER_REQUEST);
            usageHistoryRepository.seed(id, date, tokens, requests);
        }
    }
}
