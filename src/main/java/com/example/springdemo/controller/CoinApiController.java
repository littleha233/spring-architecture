package com.example.springdemo.controller;

import com.example.springdemo.biz.CoinBiz;
import com.example.springdemo.domain.Coin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/coins")
public class CoinApiController {
    private final CoinBiz coinBiz;

    public CoinApiController(CoinBiz coinBiz) {
        this.coinBiz = coinBiz;
    }

    @GetMapping
    public List<Coin> list() {
        return coinBiz.list();
    }

    @PostMapping
    public Coin create(@RequestBody SaveCoinRequest request) {
        return coinBiz.create(
            request.coinId(),
            request.symbol(),
            request.fullName(),
            request.coinPrecision(),
            request.iconUrl(),
            request.enabled()
        );
    }

    @PutMapping("/{id}")
    public Coin update(@PathVariable Long id, @RequestBody SaveCoinRequest request) {
        return coinBiz.update(
            id,
            request.coinId(),
            request.symbol(),
            request.fullName(),
            request.coinPrecision(),
            request.iconUrl(),
            request.enabled()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
    }

    public record SaveCoinRequest(
        String coinId,
        String symbol,
        String fullName,
        Integer coinPrecision,
        String iconUrl,
        Boolean enabled
    ) {
    }

    public record ErrorResponse(String message) {
    }
}
