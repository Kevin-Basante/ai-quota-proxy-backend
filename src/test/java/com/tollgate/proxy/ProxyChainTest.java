package com.tollgate.proxy;

import com.tollgate.domain.BillingPeriod;
import com.tollgate.domain.Plan;
import com.tollgate.domain.UserAccount;
import com.tollgate.dto.GenerationRequest;
import com.tollgate.dto.GenerationResponse;
import com.tollgate.exception.QuotaExceededException;
import com.tollgate.exception.RateLimitExceededException;
import com.tollgate.repository.UsageHistoryRepository;
import com.tollgate.repository.UserAccountRepository;
import com.tollgate.service.AIGenerationService;
import com.tollgate.service.RateLimitRegistry;
import com.tollgate.service.TokenEstimator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProxyChainTest {

    private final Clock clock = Clock.systemDefaultZone();
    private final List<String> calls = new ArrayList<>();
    private UserAccountRepository accounts;
    private UsageHistoryRepository usage;
    private RateLimitRegistry registry;
    private AIGenerationService chain;

    @BeforeEach
    void setUp() {
        accounts = new UserAccountRepository();
        usage = new UsageHistoryRepository();
        registry = new RateLimitRegistry(clock);
        TokenEstimator estimator = new TokenEstimator();
        AIGenerationService fakeRealService = request -> {
            calls.add(request.userId());
            return new GenerationResponse(UUID.randomUUID().toString(), "ok", "test", 10, 90, 100, 0, Instant.now());
        };
        chain = AIGenerationChainBuilder.forRealService(fakeRealService)
                .through(next -> new RateLimitProxyService(next, accounts, registry))
                .through(next -> new QuotaProxyService(next, accounts, usage, estimator, clock))
                .build();
    }

    @Test
    void freePlanIsBlockedAfterTenRequestsPerMinute() {
        save("free", Plan.FREE, 0);
        for (int index = 0; index < 10; index++) {
            chain.generate(new GenerationRequest("free", "hello"));
        }
        RateLimitExceededException exception = assertThrows(RateLimitExceededException.class,
                () -> chain.generate(new GenerationRequest("free", "hello")));
        assertEquals(10, exception.getLimit());
        assertEquals(10, calls.size());
    }

    @Test
    void quotaIsDeductedAndExhaustedWithPaymentRequired() {
        save("free", Plan.FREE, 49_950);
        chain.generate(new GenerationRequest("free", "hello"));
        assertEquals(50_000, accounts.getById("free").getTokensUsed());
        assertThrows(QuotaExceededException.class, () -> chain.generate(new GenerationRequest("free", "hello")));
        assertEquals(1, calls.size());
    }

    @Test
    void rateLimitIsCheckedBeforeQuota() {
        save("free", Plan.FREE, 50_000);
        for (int index = 0; index < 10; index++) {
            assertThrows(QuotaExceededException.class, () -> chain.generate(new GenerationRequest("free", "hello")));
        }
        assertThrows(RateLimitExceededException.class, () -> chain.generate(new GenerationRequest("free", "hello")));
    }

    @Test
    void enterprisePlanHasNoLimits() {
        save("corp", Plan.ENTERPRISE, 9_000_000);
        for (int index = 0; index < 100; index++) {
            chain.generate(new GenerationRequest("corp", "hello"));
        }
        assertEquals(100, calls.size());
        assertEquals(100, registry.requestsThisMinute("corp"));
    }

    private void save(String id, Plan plan, long used) {
        accounts.save(new UserAccount(id, id, "test", plan, used, BillingPeriod.nextResetDate(LocalDate.now(clock))));
    }
}
