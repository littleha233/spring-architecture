package com.example.springdemo.controller;

import com.example.springdemo.biz.CoinChainConfigBiz;
import com.example.springdemo.domain.CoinChainConfig;
import com.example.springdemo.domain.CoinChainConfigExtra;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/coin-chain-configs")
public class CoinChainConfigApiController {
    private final CoinChainConfigBiz coinChainConfigBiz;

    public CoinChainConfigApiController(CoinChainConfigBiz coinChainConfigBiz) {
        this.coinChainConfigBiz = coinChainConfigBiz;
    }

    @GetMapping
    public List<CoinChainConfig> list(@RequestParam(required = false) Long coinId) {
        return coinChainConfigBiz.list(coinId);
    }

    @PostMapping
    public CoinChainConfig create(@RequestBody SaveCoinChainConfigRequest request) {
        return coinChainConfigBiz.create(
            request.coinId(),
            request.chainCode(),
            request.rpcUrl(),
            request.collectionAddress(),
            request.withdrawAddress(),
            request.minWithdrawAmount(),
            request.withdrawPrecision(),
            request.minDepositAmount(),
            request.depositPrecision(),
            request.enabled()
        );
    }

    @PutMapping("/{id}")
    public CoinChainConfig update(@PathVariable Long id, @RequestBody SaveCoinChainConfigRequest request) {
        return coinChainConfigBiz.update(
            id,
            request.coinId(),
            request.chainCode(),
            request.rpcUrl(),
            request.collectionAddress(),
            request.withdrawAddress(),
            request.minWithdrawAmount(),
            request.withdrawPrecision(),
            request.minDepositAmount(),
            request.depositPrecision(),
            request.enabled()
        );
    }

    @GetMapping("/{id}/extras")
    public List<CoinChainConfigExtra> listExtras(@PathVariable Long id) {
        return coinChainConfigBiz.listExtras(id);
    }

    @PostMapping("/{id}/extras")
    public CoinChainConfigExtra upsertExtra(@PathVariable Long id, @RequestBody SaveCoinChainConfigExtraRequest request) {
        return coinChainConfigBiz.upsertExtra(id, request.paramKey(), request.paramValue());
    }

    @DeleteMapping("/{id}/extras/{extraId}")
    public ResponseEntity<Void> deleteExtra(@PathVariable Long id, @PathVariable Long extraId) {
        coinChainConfigBiz.deleteExtra(id, extraId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
    }

    public record SaveCoinChainConfigRequest(
        Long coinId,
        String chainCode,
        String rpcUrl,
        String collectionAddress,
        String withdrawAddress,
        BigDecimal minWithdrawAmount,
        Integer withdrawPrecision,
        BigDecimal minDepositAmount,
        Integer depositPrecision,
        Boolean enabled
    ) {
    }

    public record SaveCoinChainConfigExtraRequest(String paramKey, String paramValue) {
    }

    public record ErrorResponse(String message) {
    }
}
