package com.tollgate.proxy;

import com.tollgate.service.AIGenerationService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class AIGenerationChainBuilder {

    private final AIGenerationService realService;
    private final List<UnaryOperator<AIGenerationService>> proxiesInRequestOrder = new ArrayList<>();

    private AIGenerationChainBuilder(AIGenerationService realService) {
        this.realService = realService;
    }

    public static AIGenerationChainBuilder forRealService(AIGenerationService realService) {
        return new AIGenerationChainBuilder(realService);
    }

    public AIGenerationChainBuilder through(UnaryOperator<AIGenerationService> proxyFactory) {
        proxiesInRequestOrder.add(proxyFactory);
        return this;
    }

    public AIGenerationService build() {
        AIGenerationService current = realService;
        for (int index = proxiesInRequestOrder.size() - 1; index >= 0; index--) {
            current = proxiesInRequestOrder.get(index).apply(current);
        }
        return current;
    }
}
