package com.example.springdemo.service.exception;

public class EthAddressLimitExceededException extends RuntimeException {
    public EthAddressLimitExceededException(String message) {
        super(message);
    }
}
