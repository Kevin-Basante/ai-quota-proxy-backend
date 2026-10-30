package com.tollgate.controller;

import com.tollgate.dto.GenerationRequest;
import com.tollgate.dto.GenerationResponse;
import com.tollgate.dto.GenerationResult;
import com.tollgate.dto.PromptPayload;
import com.tollgate.service.AIGenerationService;
import com.tollgate.service.QuotaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIGenerationService aiGenerationService;
    private final QuotaService quotaService;

    public AIController(AIGenerationService aiGenerationService, QuotaService quotaService) {
        this.aiGenerationService = aiGenerationService;
        this.quotaService = quotaService;
    }

    @PostMapping("/generate")
    public GenerationResult generate(@RequestHeader(UserHeader.NAME) String userId, @Valid @RequestBody PromptPayload payload) {
        GenerationResponse response = aiGenerationService.generate(new GenerationRequest(userId, payload.prompt()));
        return new GenerationResult(response, quotaService.status(userId));
    }
}
