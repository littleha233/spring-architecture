package com.example.springdemo.controller;

import com.example.springdemo.domain.EthWallet;
import com.example.springdemo.service.EthereumWalletService;
import com.example.springdemo.service.exception.EthAddressLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/eth-addresses")
public class EthWalletApiController {
    private final EthereumWalletService ethereumWalletService;

    public EthWalletApiController(EthereumWalletService ethereumWalletService) {
        this.ethereumWalletService = ethereumWalletService;
    }

    @PostMapping("/generate")
    public EthWallet generate(@RequestBody GenerateEthAddressRequest request) {
        if (request == null || request.userId() == null || request.userId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId must be a positive number");
        }
        return ethereumWalletService.generateAndSave(request.userId());
    }

    @GetMapping
    public List<EthWallet> list(@RequestParam(required = false) Long userId) {
        if (userId != null && userId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId must be a positive number");
        }
        if (userId == null) {
            return ethereumWalletService.listAll();
        }
        return ethereumWalletService.listByUserId(userId);
    }

    @ExceptionHandler(EthAddressLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleLimit(EthAddressLimitExceededException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
    }

    public record GenerateEthAddressRequest(Long userId) {
    }

    public record ErrorResponse(String message) {
    }
}
