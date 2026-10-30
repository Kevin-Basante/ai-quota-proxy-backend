package com.tollgate.dto;

import java.time.Instant;
import java.util.Map;

public record ApiError(int status, String error, String message, Instant timestamp, Map<String, Object> details) {
}
