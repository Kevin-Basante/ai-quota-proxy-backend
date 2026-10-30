package com.tollgate.proxy;

import com.tollgate.domain.Plan;
import com.tollgate.dto.GenerationRequest;
import com.tollgate.dto.GenerationResponse;
import com.tollgate.exception.RateLimitExceededException;
import com.tollgate.repository.UserAccountRepository;
import com.tollgate.service.AIGenerationService;
import com.tollgate.service.RateLimitRegistry;

public class RateLimitProxyService implements AIGenerationService {

    private final AIGenerationService delegate;
    private final UserAccountRepository accountRepository;
    private final RateLimitRegistry rateLimitRegistry;

    public RateLimitProxyService(AIGenerationService delegate, UserAccountRepository accountRepository, RateLimitRegistry rateLimitRegistry) {
        this.delegate = delegate;
        this.accountRepository = accountRepository;
        this.rateLimitRegistry = rateLimitRegistry;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        Plan plan = accountRepository.getById(request.userId()).getPlan();
        if (!rateLimitRegistry.tryAcquire(request.userId(), plan.getRequestsPerMinute())) {
            throw new RateLimitExceededException(plan.getRequestsPerMinute(), rateLimitRegistry.secondsUntilReset());
        }
        return delegate.generate(request);
    }
}
