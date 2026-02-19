package com.example.springdemo.biz;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface ConfigFacadeBiz {
    Optional<CoinChainConfigView> queryCoinChainConfig(Integer coinId, Integer blockchainId);

    record CoinChainConfigView(
        Integer coinId,
        String symbol,
        String fullName,
        Integer coinPrecision,
        String iconUrl,
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
        Boolean enabled,
        Instant createTime,
        Instant updateTime
    ) {
    }
}
