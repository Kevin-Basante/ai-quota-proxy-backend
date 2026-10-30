package com.tollgate.service;

import com.tollgate.dto.GenerationRequest;
import com.tollgate.dto.GenerationResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class MockAIGenerationService implements AIGenerationService {

    private static final String MODEL_NAME = "tollgate-mock-1";
    private static final long SIMULATED_LATENCY_MS = 1200L;

    private static final List<String> PREDEFINED_ANSWERS = List.of(
            "A proxy is a stand-in that shares the interface of the real object. Clients talk to it without noticing, while it decides whether the call deserves to reach the expensive service behind it.",
            "Rate limiting protects shared infrastructure. By counting requests inside a fixed one-minute window, the platform keeps a single noisy client from starving everyone else of compute.",
            "Monthly quotas turn compute into a predictable budget. Every generated token is metered, and when the budget runs dry the platform asks for an upgrade instead of silently degrading.",
            "Here is a short draft: our team ships small, observable changes every day. We measure before we optimize, we write down decisions, and we treat every incident as a lesson rather than a blame game.",
            "Three ideas for your weekend project: a plant watering tracker with soil sensors, a pocket budget that groups expenses by mood, and a tiny CLI that summarizes yesterday's commits into a stand-up note.",
            "In distributed systems, latency is a feature you design, not an accident you discover. Caching, batching and back-pressure are the three levers that keep response times honest under load.",
            "The quick answer is yes, but with a caveat. The approach works well for small datasets; once you cross a few million rows you will want an index on the filter column and a paginated query.",
            "Summary: the document proposes moving the billing service behind a gateway, adding per-tenant limits, and publishing a usage dashboard so customers can see exactly what they consume each day.",
            "Think of tokens as fragments of words. A short sentence costs a handful of them, a long essay costs thousands, and the model charges for both what you send and what it writes back.",
            "A haiku about proxies: quiet gatekeeper, counting every knock at night, the server sleeps sound.",
            "To improve this function, extract the validation into its own method, return early on invalid input, and replace the nested loops with a map lookup. The result is shorter and runs in linear time.",
            "Good onboarding emails are short, specific and kind. Welcome the person by name, tell them the one thing to do first, and give them a real human to contact when something feels confusing."
    );

    private final TokenEstimator tokenEstimator;

    public MockAIGenerationService(TokenEstimator tokenEstimator) {
        this.tokenEstimator = tokenEstimator;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        long startedAt = System.currentTimeMillis();
        simulateInference();
        String text = pickAnswer();
        int promptTokens = tokenEstimator.estimate(request.prompt());
        int completionTokens = tokenEstimator.estimate(text);
        return new GenerationResponse(
                UUID.randomUUID().toString(),
                text,
                MODEL_NAME,
                promptTokens,
                completionTokens,
                promptTokens + completionTokens,
                System.currentTimeMillis() - startedAt,
                Instant.now()
        );
    }

    private void simulateInference() {
        try {
            Thread.sleep(SIMULATED_LATENCY_MS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Generation was interrupted", exception);
        }
    }

    private String pickAnswer() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String first = PREDEFINED_ANSWERS.get(random.nextInt(PREDEFINED_ANSWERS.size()));
        if (random.nextInt(3) != 0) {
            return first;
        }
        String second = PREDEFINED_ANSWERS.get(random.nextInt(PREDEFINED_ANSWERS.size()));
        return second.equals(first) ? first : first + "\n\n" + second;
    }
}
