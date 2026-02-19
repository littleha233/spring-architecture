package com.example.springdemo.common.web;

import java.time.Instant;

public record ApiErrorResponse(
    String errorCode,
    String message,
    String traceId,
    Instant timestamp
) {
}
