package com.tollgate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PromptPayload(
        @NotBlank(message = "Prompt must not be empty")
        @Size(max = 4000, message = "Prompt must have at most 4000 characters")
        String prompt
) {
}
