package com.tollgate.dto;

import com.tollgate.domain.Plan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpgradeRequest(
        @NotNull(message = "Target plan is required")
        Plan targetPlan,
        @NotBlank(message = "Card holder is required")
        String cardHolder,
        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "\\d{13,19}", message = "Card number must contain between 13 and 19 digits")
        String cardNumber
) {
}
