package com.example.springdemo.common.error;

public class IntegrationException extends AppException {
    public IntegrationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public IntegrationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
