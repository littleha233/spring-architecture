package com.example.springdemo.common.error;

public abstract class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    protected AppException(ErrorCode errorCode, String message) {
        super(resolveMessage(errorCode, message));
        this.errorCode = errorCode;
    }

    protected AppException(ErrorCode errorCode, String message, Throwable cause) {
        super(resolveMessage(errorCode, message), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    private static String resolveMessage(ErrorCode errorCode, String message) {
        if (message == null || message.isBlank()) {
            return errorCode.getDefaultMessage();
        }
        return message;
    }
}
