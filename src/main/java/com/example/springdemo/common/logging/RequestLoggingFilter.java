package com.example.springdemo.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Principal;
import java.util.UUID;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final String TRACE_ID_KEY = "traceId";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String traceId = resolveTraceId(request.getHeader(TRACE_ID_HEADER));
        MDC.put(TRACE_ID_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            String requestUri = request.getRequestURI();
            String queryString = request.getQueryString();
            if (queryString != null && !queryString.isBlank()) {
                requestUri = requestUri + "?" + queryString;
            }
            log.info(
                "access traceId={} userId={} requestUri={} method={} status={} responseTime={}ms",
                traceId,
                resolveUserId(request),
                requestUri,
                request.getMethod(),
                response.getStatus(),
                responseTime
            );
            MDC.remove(TRACE_ID_KEY);
        }
    }

    private String resolveTraceId(String incomingTraceId) {
        if (incomingTraceId == null || incomingTraceId.isBlank()) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        String normalized = incomingTraceId.trim();
        if (normalized.length() > 64 || !normalized.matches("^[A-Za-z0-9._-]+$")) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        return normalized;
    }

    private String resolveUserId(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            return principal.getName();
        }
        return LogContext.currentUserId();
    }
}
