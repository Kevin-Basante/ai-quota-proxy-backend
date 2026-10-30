package com.tollgate.proxy;

import com.tollgate.domain.UserAccount;
import com.tollgate.dto.GenerationRequest;
import com.tollgate.dto.GenerationResponse;
import com.tollgate.exception.QuotaExceededException;
import com.tollgate.repository.UsageHistoryRepository;
import com.tollgate.repository.UserAccountRepository;
import com.tollgate.service.AIGenerationService;
import com.tollgate.service.TokenEstimator;

import java.time.Clock;
import java.time.LocalDate;

public class QuotaProxyService implements AIGenerationService {

    private final AIGenerationService delegate;
    private final UserAccountRepository accountRepository;
    private final UsageHistoryRepository usageHistoryRepository;
    private final TokenEstimator tokenEstimator;
    private final Clock clock;

    public QuotaProxyService(AIGenerationService delegate, UserAccountRepository accountRepository,
                             UsageHistoryRepository usageHistoryRepository, TokenEstimator tokenEstimator, Clock clock) {
        this.delegate = delegate;
        this.accountRepository = accountRepository;
        this.usageHistoryRepository = usageHistoryRepository;
        this.tokenEstimator = tokenEstimator;
        this.clock = clock;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        UserAccount account = accountRepository.getById(request.userId());
        ensureQuotaAvailable(account, tokenEstimator.estimate(request.prompt()));
        GenerationResponse response = delegate.generate(request);
        long charged = account.consumeTokens(response.totalTokens());
        usageHistoryRepository.register(account.getId(), LocalDate.now(clock), charged);
        return response;
    }

    private void ensureQuotaAvailable(UserAccount account, int promptTokens) {
        if (account.getPlan().hasUnlimitedTokens()) {
            return;
        }
        if (account.isQuotaExhausted() || account.getTokensRemaining() < promptTokens) {
            throw new QuotaExceededException(
                    account.getPlan(),
                    account.getTokensUsed(),
                    account.getPlan().getMonthlyTokenQuota(),
                    account.getQuotaResetDate()
            );
        }
    }
}
