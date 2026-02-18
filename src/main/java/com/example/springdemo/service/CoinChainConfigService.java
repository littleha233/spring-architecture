package com.example.springdemo.service;

import com.example.springdemo.biz.CoinChainConfigBiz;
import com.example.springdemo.domain.Coin;
import com.example.springdemo.domain.CoinChainConfig;
import com.example.springdemo.domain.CoinChainConfigExtra;
import com.example.springdemo.repository.CoinChainConfigExtraRepository;
import com.example.springdemo.repository.CoinChainConfigRepository;
import com.example.springdemo.repository.CoinRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CoinChainConfigService implements CoinChainConfigBiz {
    private final CoinChainConfigRepository coinChainConfigRepository;
    private final CoinChainConfigExtraRepository coinChainConfigExtraRepository;
    private final CoinRepository coinRepository;

    public CoinChainConfigService(CoinChainConfigRepository coinChainConfigRepository,
                                  CoinChainConfigExtraRepository coinChainConfigExtraRepository,
                                  CoinRepository coinRepository) {
        this.coinChainConfigRepository = coinChainConfigRepository;
        this.coinChainConfigExtraRepository = coinChainConfigExtraRepository;
        this.coinRepository = coinRepository;
    }

    @Override
    public List<CoinChainConfig> list(Long coinId) {
        if (coinId == null) {
            return coinChainConfigRepository.findAllByOrderByIdDesc();
        }
        validatePositiveId(coinId, "coinId");
        return coinChainConfigRepository.findByCoinIdOrderByIdDesc(coinId);
    }

    @Override
    public CoinChainConfig create(Long coinId, String chainCode, String rpcUrl, String collectionAddress, String withdrawAddress,
                                  BigDecimal minWithdrawAmount, Integer withdrawPrecision,
                                  BigDecimal minDepositAmount, Integer depositPrecision, Boolean enabled) {
        validateCoinExists(coinId);
        String normalizedChainCode = requireText(chainCode, "chainCode").toUpperCase();

        if (coinChainConfigRepository.existsByCoinIdAndChainCodeIgnoreCase(coinId, normalizedChainCode)) {
            throw new IllegalArgumentException("chain config already exists for this coin and chainCode");
        }

        CoinChainConfig config = new CoinChainConfig(
            coinId,
            normalizedChainCode,
            requireText(rpcUrl, "rpcUrl"),
            requireText(collectionAddress, "collectionAddress"),
            requireText(withdrawAddress, "withdrawAddress"),
            requireAmount(minWithdrawAmount, "minWithdrawAmount"),
            validatePrecision(withdrawPrecision, "withdrawPrecision"),
            requireAmount(minDepositAmount, "minDepositAmount"),
            validatePrecision(depositPrecision, "depositPrecision"),
            enabled == null ? Boolean.TRUE : enabled
        );
        return coinChainConfigRepository.save(config);
    }

    @Override
    public CoinChainConfig update(Long id, Long coinId, String chainCode, String rpcUrl, String collectionAddress, String withdrawAddress,
                                  BigDecimal minWithdrawAmount, Integer withdrawPrecision,
                                  BigDecimal minDepositAmount, Integer depositPrecision, Boolean enabled) {
        validatePositiveId(id, "id");
        validateCoinExists(coinId);
        CoinChainConfig config = coinChainConfigRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("coin chain config not found"));

        String normalizedChainCode = requireText(chainCode, "chainCode").toUpperCase();
        if (coinChainConfigRepository.existsByCoinIdAndChainCodeIgnoreCaseAndIdNot(coinId, normalizedChainCode, id)) {
            throw new IllegalArgumentException("chain config already exists for this coin and chainCode");
        }

        config.setCoinId(coinId);
        config.setChainCode(normalizedChainCode);
        config.setRpcUrl(requireText(rpcUrl, "rpcUrl"));
        config.setCollectionAddress(requireText(collectionAddress, "collectionAddress"));
        config.setWithdrawAddress(requireText(withdrawAddress, "withdrawAddress"));
        config.setMinWithdrawAmount(requireAmount(minWithdrawAmount, "minWithdrawAmount"));
        config.setWithdrawPrecision(validatePrecision(withdrawPrecision, "withdrawPrecision"));
        config.setMinDepositAmount(requireAmount(minDepositAmount, "minDepositAmount"));
        config.setDepositPrecision(validatePrecision(depositPrecision, "depositPrecision"));
        config.setEnabled(enabled == null ? Boolean.TRUE : enabled);

        return coinChainConfigRepository.save(config);
    }

    @Override
    public List<CoinChainConfigExtra> listExtras(Long chainConfigId) {
        validateChainConfigExists(chainConfigId);
        return coinChainConfigExtraRepository.findByChainConfigIdOrderByIdAsc(chainConfigId);
    }

    @Override
    public CoinChainConfigExtra upsertExtra(Long chainConfigId, String paramKey, String paramValue) {
        validateChainConfigExists(chainConfigId);
        String normalizedKey = requireText(paramKey, "paramKey");
        String normalizedValue = requireText(paramValue, "paramValue");

        CoinChainConfigExtra existing = coinChainConfigExtraRepository
            .findByChainConfigIdAndParamKeyIgnoreCase(chainConfigId, normalizedKey)
            .orElse(null);
        if (existing != null) {
            existing.setParamValue(normalizedValue);
            return coinChainConfigExtraRepository.save(existing);
        }

        CoinChainConfigExtra extra = new CoinChainConfigExtra(chainConfigId, normalizedKey, normalizedValue);
        return coinChainConfigExtraRepository.save(extra);
    }

    @Override
    public void deleteExtra(Long chainConfigId, Long extraId) {
        validateChainConfigExists(chainConfigId);
        validatePositiveId(extraId, "extraId");

        CoinChainConfigExtra extra = coinChainConfigExtraRepository.findById(extraId)
            .orElseThrow(() -> new IllegalArgumentException("extra config not found"));
        if (!chainConfigId.equals(extra.getChainConfigId())) {
            throw new IllegalArgumentException("extra config does not belong to this chain config");
        }
        coinChainConfigExtraRepository.deleteById(extraId);
    }

    private void validateCoinExists(Long coinId) {
        validatePositiveId(coinId, "coinId");
        Coin coin = coinRepository.findById(coinId).orElseThrow(() -> new IllegalArgumentException("coin not found"));
        if (Boolean.FALSE.equals(coin.getEnabled())) {
            throw new IllegalArgumentException("selected coin is disabled");
        }
    }

    private void validateChainConfigExists(Long chainConfigId) {
        validatePositiveId(chainConfigId, "chainConfigId");
        coinChainConfigRepository.findById(chainConfigId)
            .orElseThrow(() -> new IllegalArgumentException("coin chain config not found"));
    }

    private void validatePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive number");
        }
    }

    private String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private BigDecimal requireAmount(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(field + " must be greater than or equal to 0");
        }
        return value;
    }

    private int validatePrecision(Integer precision, String field) {
        if (precision == null || precision < 0) {
            throw new IllegalArgumentException(field + " must be a non-negative integer");
        }
        return precision;
    }
}
