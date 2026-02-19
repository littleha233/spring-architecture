package com.example.springdemo.controller;

import com.example.springdemo.biz.ConfigFacadeBiz;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/facade/config")
public class ConfigFacadeApiController {
    private final ConfigFacadeBiz configFacadeBiz;

    public ConfigFacadeApiController(ConfigFacadeBiz configFacadeBiz) {
        this.configFacadeBiz = configFacadeBiz;
    }

    @GetMapping("/coin-chain")
    public ResponseEntity<?> queryCoinChainConfig(@RequestParam Integer coinId, @RequestParam Integer blockchainId) {
        Optional<ConfigFacadeBiz.CoinChainConfigView> result = configFacadeBiz.queryCoinChainConfig(coinId, blockchainId);
        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("coin chain config not found for coinId and blockchainId"));
        }
        return ResponseEntity.ok(result.get());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
    }

    public record ErrorResponse(String message) {
    }
}
