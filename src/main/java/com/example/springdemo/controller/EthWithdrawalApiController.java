package com.example.springdemo.controller;

import com.example.springdemo.biz.EthWithdrawalBiz;
import com.example.springdemo.domain.EthWithdrawal;
import com.example.springdemo.service.exception.EthWithdrawalException;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/eth-withdrawals")
public class EthWithdrawalApiController {
    private final EthWithdrawalBiz ethWithdrawalBiz;

    public EthWithdrawalApiController(EthWithdrawalBiz ethWithdrawalBiz) {
        this.ethWithdrawalBiz = ethWithdrawalBiz;
    }

    @PostMapping("/send")
    public EthWithdrawal send(@RequestBody SendWithdrawalRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request body is required");
        }
        return ethWithdrawalBiz.withdraw(
            request.uid(),
            request.fromWalletId(),
            request.toAddress(),
            request.amountEth()
        );
    }

    @GetMapping
    public List<EthWithdrawal> list(@RequestParam Long uid) {
        if (uid == null || uid <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "uid must be a positive number");
        }
        return ethWithdrawalBiz.listByUid(uid);
    }

    @ExceptionHandler(EthWithdrawalException.class)
    public ResponseEntity<ErrorResponse> handleWithdrawalError(EthWithdrawalException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
    }

    public record SendWithdrawalRequest(Long uid, Long fromWalletId, String toAddress, BigDecimal amountEth) {
    }

    public record ErrorResponse(String message) {
    }
}
