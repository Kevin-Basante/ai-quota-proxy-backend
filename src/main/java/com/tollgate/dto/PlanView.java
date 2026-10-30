package com.tollgate.dto;

import com.tollgate.domain.Plan;

import java.math.BigDecimal;

public record PlanView(Plan plan, Integer requestsPerMinute, Long monthlyTokenQuota, BigDecimal monthlyPriceUsd) {
}
