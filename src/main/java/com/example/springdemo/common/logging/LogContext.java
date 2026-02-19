package com.example.springdemo.common.logging;

import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class LogContext {
    private static final String TRACE_ID_KEY = "traceId";

    private LogContext() {
    }

    public static String traceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        return traceId == null || traceId.isBlank() ? "-" : traceId;
    }

    public static String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "ANONYMOUS";
        }
        String username = authentication.getName();
        return username == null || username.isBlank() ? "ANONYMOUS" : username;
    }
}
