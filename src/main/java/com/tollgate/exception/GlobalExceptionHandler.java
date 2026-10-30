package com.tollgate.exception;

import com.tollgate.dto.ApiError;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiError> handleRateLimit(RateLimitExceededException exception) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("limit", exception.getLimit());
        details.put("retryAfterSeconds", exception.getRetryAfterSeconds());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(exception.getRetryAfterSeconds()))
                .body(body(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage(), details));
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<ApiError> handleQuota(QuotaExceededException exception) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("plan", exception.getPlan());
        details.put("tokensUsed", exception.getTokensUsed());
        details.put("tokenQuota", exception.getTokenQuota());
        details.put("resetDate", exception.getResetDate());
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(body(HttpStatus.PAYMENT_REQUIRED, exception.getMessage(), details));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(body(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of()));
    }

    @ExceptionHandler(InvalidUpgradeException.class)
    public ResponseEntity<ApiError> handleInvalidUpgrade(InvalidUpgradeException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(body(HttpStatus.CONFLICT, exception.getMessage(), Map.of()));
    }

    @ExceptionHandler(PaymentDeclinedException.class)
    public ResponseEntity<ApiError> handlePaymentDeclined(PaymentDeclinedException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(body(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, Object> details = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> details.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(body(HttpStatus.BAD_REQUEST, "Request validation failed", details));
    }

    @ExceptionHandler({MissingRequestHeaderException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception exception) {
        return ResponseEntity.badRequest()
                .body(body(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of()));
    }

    private ApiError body(HttpStatus status, String message, Map<String, Object> details) {
        return new ApiError(status.value(), status.getReasonPhrase(), message, Instant.now(), details);
    }
}
