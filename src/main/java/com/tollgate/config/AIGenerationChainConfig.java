package com.tollgate.config;

import com.tollgate.proxy.AIGenerationChainBuilder;
import com.tollgate.proxy.QuotaProxyService;
import com.tollgate.proxy.RateLimitProxyService;
import com.tollgate.repository.UsageHistoryRepository;
import com.tollgate.repository.UserAccountRepository;
import com.tollgate.service.AIGenerationService;
import com.tollgate.service.MockAIGenerationService;
import com.tollgate.service.RateLimitRegistry;
import com.tollgate.service.TokenEstimator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AIGenerationChainConfig {

    @Bean
    public AIGenerationService aiGenerationService(UserAccountRepository accountRepository,
                                                   UsageHistoryRepository usageHistoryRepository,
                                                   RateLimitRegistry rateLimitRegistry,
                                                   TokenEstimator tokenEstimator,
                                                   Clock clock) {
        return AIGenerationChainBuilder
                .forRealService(new MockAIGenerationService(tokenEstimator))
                .through(next -> new RateLimitProxyService(next, accountRepository, rateLimitRegistry))
                .through(next -> new QuotaProxyService(next, accountRepository, usageHistoryRepository, tokenEstimator, clock))
                .build();
    }
}
