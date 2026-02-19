package com.example.springdemo.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_ARGUMENT("INVALID_ARGUMENT", HttpStatus.BAD_REQUEST, "request parameter is invalid"),
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", HttpStatus.BAD_REQUEST, "request violates business rules"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, "resource not found"),
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", HttpStatus.BAD_REQUEST, "resource already exists"),
    UPLOAD_FILE_TOO_LARGE("UPLOAD_FILE_TOO_LARGE", HttpStatus.BAD_REQUEST, "uploaded file exceeds size limit"),
    INTEGRATION_IO_ERROR("INTEGRATION_IO_ERROR", HttpStatus.SERVICE_UNAVAILABLE, "dependent service is temporarily unavailable"),
    SYSTEM_INTERNAL_ERROR("SYSTEM_INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "system is busy, please retry later");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
