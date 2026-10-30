package com.tollgate.exception;

public class RateLimitExceededException extends RuntimeException {

    private final int limit;
    private final long retryAfterSeconds;

    public RateLimitExceededException(int limit, long retryAfterSeconds) {
        super("Rate limit of " + limit + " requests per minute exceeded");
        this.limit = limit;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getLimit() {
        return limit;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
