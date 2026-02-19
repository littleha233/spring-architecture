package com.example.springdemo.common.web;

import com.example.springdemo.common.error.BusinessException;
import com.example.springdemo.common.error.ErrorCode;
import com.example.springdemo.common.error.IntegrationException;
import com.example.springdemo.common.logging.LogContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;

@RestControllerAdvice(annotations = RestController.class)
public class GlobalRestExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalRestExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException exception, HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        String message = resolveMessage(exception.getMessage(), errorCode.getDefaultMessage());
        log.warn(
            "business_error traceId={} userId={} errorCode={} requestUri={} message={}",
            LogContext.traceId(),
            LogContext.currentUserId(),
            errorCode.getCode(),
            request.getRequestURI(),
            message
        );
        return buildResponse(errorCode.getHttpStatus(), errorCode, message);
    }

    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<ApiErrorResponse> handleIntegrationException(IntegrationException exception, HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        String message = resolveMessage(exception.getMessage(), errorCode.getDefaultMessage());
        log.error(
            "integration_error traceId={} userId={} errorCode={} requestUri={} message={}",
            LogContext.traceId(),
            LogContext.currentUserId(),
            errorCode.getCode(),
            request.getRequestURI(),
            message,
            exception
        );
        return buildResponse(errorCode.getHttpStatus(), errorCode, message);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceeded(
        MaxUploadSizeExceededException exception,
        HttpServletRequest request
    ) {
        log.warn(
            "business_error traceId={} userId={} errorCode={} requestUri={} message={}",
            LogContext.traceId(),
            LogContext.currentUserId(),
            ErrorCode.UPLOAD_FILE_TOO_LARGE.getCode(),
            request.getRequestURI(),
            "icon file size exceeds upload limit"
        );
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            ErrorCode.UPLOAD_FILE_TOO_LARGE,
            "icon file size exceeds upload limit"
        );
    }

    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class,
        HttpMessageNotReadableException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<ApiErrorResponse> handleInvalidArguments(Exception exception, HttpServletRequest request) {
        String message = resolveMessage(exception.getMessage(), ErrorCode.INVALID_ARGUMENT.getDefaultMessage());
        log.warn(
            "business_error traceId={} userId={} errorCode={} requestUri={} message={}",
            LogContext.traceId(),
            LogContext.currentUserId(),
            ErrorCode.INVALID_ARGUMENT.getCode(),
            request.getRequestURI(),
            message
        );
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_ARGUMENT, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleSystemException(Exception exception, HttpServletRequest request) {
        log.error(
            "system_error traceId={} userId={} errorCode={} requestUri={} message={}",
            LogContext.traceId(),
            LogContext.currentUserId(),
            ErrorCode.SYSTEM_INTERNAL_ERROR.getCode(),
            request.getRequestURI(),
            exception.getMessage(),
            exception
        );
        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorCode.SYSTEM_INTERNAL_ERROR,
            ErrorCode.SYSTEM_INTERNAL_ERROR.getDefaultMessage()
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, ErrorCode errorCode, String message) {
        ApiErrorResponse response = new ApiErrorResponse(
            errorCode.getCode(),
            message,
            LogContext.traceId(),
            Instant.now()
        );
        return ResponseEntity.status(status).body(response);
    }

    private String resolveMessage(String rawMessage, String fallback) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return fallback;
        }
        return rawMessage;
    }
}
