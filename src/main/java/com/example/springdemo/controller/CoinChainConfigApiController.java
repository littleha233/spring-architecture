package com.example.springdemo.controller;

import com.example.springdemo.biz.CoinChainConfigBiz;
import com.example.springdemo.domain.CoinChainConfig;
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
            request.blockchainId(),
            request.chainCode(),
            request.chainName(),
            request.rpcUrl(),
            request.collectionAddress(),
            request.withdrawAddress(),
            request.minWithdrawAmount(),
            request.withdrawPrecision(),
            request.minDepositAmount(),
            request.depositPrecision(),
            request.extraJson(),
            request.enabled()
        );
    }

    @PutMapping("/{id}")
    public CoinChainConfig update(@PathVariable Long id, @RequestBody SaveCoinChainConfigRequest request) {
        return coinChainConfigBiz.update(
            id,
            request.coinId(),
            request.blockchainId(),
            request.chainCode(),
            request.chainName(),
            request.rpcUrl(),
            request.collectionAddress(),
            request.withdrawAddress(),
            request.minWithdrawAmount(),
            request.withdrawPrecision(),
            request.minDepositAmount(),
            request.depositPrecision(),
            request.extraJson(),
            request.enabled()
        );
    }

    public record SaveCoinChainConfigRequest(
        Long coinId,
        Integer blockchainId,
        String chainCode,
        String chainName,
        String rpcUrl,
        String collectionAddress,
        String withdrawAddress,
        BigDecimal minWithdrawAmount,
        Integer withdrawPrecision,
        BigDecimal minDepositAmount,
        Integer depositPrecision,
        String extraJson,
        Boolean enabled
    ) {
    }

}
