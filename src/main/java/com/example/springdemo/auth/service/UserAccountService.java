package com.example.springdemo.auth.service;

import com.example.springdemo.common.error.BusinessException;
import com.example.springdemo.common.error.ErrorCode;
import com.example.springdemo.common.logging.LogContext;
import com.example.springdemo.domain.AppUser;
import com.example.springdemo.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAccountService {
    private static final Logger log = LoggerFactory.getLogger(UserAccountService.class);
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "123456";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void ensureDefaultAdminUser() {
        if (appUserRepository.existsByUsernameIgnoreCase(DEFAULT_ADMIN_USERNAME)) {
            return;
        }
        AppUser admin = new AppUser(
            DEFAULT_ADMIN_USERNAME,
            passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD),
            Boolean.TRUE
        );
        appUserRepository.save(admin);
        log.info(
            "audit traceId={} userId={} action=init_default_admin target=username:{}",
            LogContext.traceId(),
            LogContext.currentUserId(),
            DEFAULT_ADMIN_USERNAME
        );
    }

    public AppUser register(String username, String password, String confirmPassword) {
        String normalizedUsername = normalizeUsername(username);
        String normalizedPassword = validatePassword(password);
        validateConfirmPassword(normalizedPassword, confirmPassword);

        if (appUserRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "username already exists");
        }

        AppUser user = new AppUser(
            normalizedUsername,
            passwordEncoder.encode(normalizedPassword),
            Boolean.TRUE
        );
        AppUser saved = appUserRepository.save(user);
        log.info(
            "business traceId={} userId={} operation=register_user details=username:{} result=SUCCESS",
            LogContext.traceId(),
            LogContext.currentUserId(),
            saved.getUsername()
        );
        log.info(
            "audit traceId={} userId={} action=register_user target=username:{}",
            LogContext.traceId(),
            LogContext.currentUserId(),
            saved.getUsername()
        );
        return saved;
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "username is required");
        }
        String normalized = username.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "username is required");
        }
        if (normalized.length() < 3 || normalized.length() > 32) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "username length must be between 3 and 32");
        }
        if (!normalized.matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "username only supports letters, numbers and underscore");
        }
        return normalized;
    }

    private String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "password is required");
        }
        if (password.length() < 6 || password.length() > 64) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "password length must be between 6 and 64");
        }
        return password;
    }

    private void validateConfirmPassword(String password, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "confirmPassword is required");
        }
        if (!password.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "password and confirmPassword do not match");
        }
    }
}
