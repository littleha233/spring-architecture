package com.example.springdemo.service;

import com.example.springdemo.biz.ConfigFacadeBiz;
import com.example.springdemo.domain.Coin;
import com.example.springdemo.domain.CoinChainConfig;
import com.example.springdemo.repository.CoinChainConfigRepository;
import com.example.springdemo.repository.CoinRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConfigFacadeService implements ConfigFacadeBiz {
    private final CoinRepository coinRepository;
    private final CoinChainConfigRepository coinChainConfigRepository;

    public ConfigFacadeService(CoinRepository coinRepository, CoinChainConfigRepository coinChainConfigRepository) {
        this.coinRepository = coinRepository;
        this.coinChainConfigRepository = coinChainConfigRepository;
    }

    @Override
    public Optional<CoinChainConfigView> queryCoinChainConfig(Integer coinId, Integer blockchainId) {
        validateNonNegative(coinId, "coinId");
        validateNonNegative(blockchainId, "blockchainId");

        Optional<Coin> coinOptional = coinRepository.findByCoinId(coinId);
        if (coinOptional.isEmpty()) {
            return Optional.empty();
        }
        Coin coin = coinOptional.get();

        Optional<CoinChainConfig> configOptional = coinChainConfigRepository.findByCoinIdAndBlockchainId(coin.getId(), blockchainId);
        if (configOptional.isEmpty()) {
            return Optional.empty();
        }
        CoinChainConfig config = configOptional.get();

        return Optional.of(new CoinChainConfigView(
            coin.getCoinId(),
            coin.getSymbol(),
            coin.getFullName(),
            coin.getCoinPrecision(),
            coin.getIconUrl(),
            config.getBlockchainId(),
            config.getChainCode(),
            config.getChainName(),
            config.getRpcUrl(),
            config.getCollectionAddress(),
            config.getWithdrawAddress(),
            config.getMinWithdrawAmount(),
            config.getWithdrawPrecision(),
            config.getMinDepositAmount(),
            config.getDepositPrecision(),
            config.getExtraJson(),
            config.getEnabled(),
            config.getCreateTime(),
            config.getUpdateTime()
        ));
    }

    private void validateNonNegative(Integer value, String field) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException(field + " must be a non-negative integer");
        }
    }
}
