package com.tollgate.service;

import com.tollgate.dto.GenerationRequest;
import com.tollgate.dto.GenerationResponse;

public interface AIGenerationService {

    GenerationResponse generate(GenerationRequest request);
}
