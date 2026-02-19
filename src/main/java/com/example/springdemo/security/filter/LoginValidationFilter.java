package com.example.springdemo.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LoginValidationFilter extends OncePerRequestFilter {
    private static final int USERNAME_MAX_LENGTH = 32;
    private static final int PASSWORD_MAX_LENGTH = 64;

    private final AntPathRequestMatcher loginRequestMatcher = new AntPathRequestMatcher("/login", "POST");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (!loginRequestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = trimToEmpty(request.getParameter("username"));
        String password = request.getParameter("password");
        if (username.isEmpty() || password == null || password.isEmpty()) {
            response.sendRedirect("/login?error=true");
            return;
        }
        if (username.length() > USERNAME_MAX_LENGTH || password.length() > PASSWORD_MAX_LENGTH) {
            response.sendRedirect("/login?error=true");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
