package com.example.springdemo.service.exception;

public class EthWithdrawalException extends RuntimeException {
    public EthWithdrawalException(String message) {
        super(message);
    }

    public EthWithdrawalException(String message, Throwable cause) {
        super(message, cause);
    }
}
