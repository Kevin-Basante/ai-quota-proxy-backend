package com.tollgate.dto;

import com.tollgate.domain.Plan;

public record UserSummary(String id, String displayName, String role, Plan plan) {
}
