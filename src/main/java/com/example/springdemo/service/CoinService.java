package com.example.springdemo.service;

import com.example.springdemo.biz.CoinBiz;
import com.example.springdemo.domain.Coin;
import com.example.springdemo.repository.CoinRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoinService implements CoinBiz {
    private final CoinRepository coinRepository;

    public CoinService(CoinRepository coinRepository) {
        this.coinRepository = coinRepository;
    }

    @Override
    public List<Coin> list() {
        return coinRepository.findAllByOrderByIdDesc();
    }

    @Override
    public Coin create(Integer coinId, String symbol, String fullName, Integer coinPrecision, String iconUrl, Boolean enabled) {
        int normalizedCoinId = validateCoinId(coinId);
        String normalizedSymbol = requireText(symbol, "symbol");
        String normalizedFullName = requireText(fullName, "fullName");
        int normalizedPrecision = validatePrecision(coinPrecision, "coinPrecision");
        String normalizedIconUrl = trimToNull(iconUrl);
        Boolean normalizedEnabled = enabled == null ? Boolean.TRUE : enabled;

        if (coinRepository.existsByCoinId(normalizedCoinId)) {
            throw new IllegalArgumentException("coinId already exists");
        }

        Coin coin = new Coin(
            normalizedCoinId,
            normalizedSymbol.toUpperCase(),
            normalizedFullName,
            normalizedPrecision,
            normalizedIconUrl,
            normalizedEnabled
        );
        return coinRepository.save(coin);
    }

    @Override
    public Coin update(Long id, Integer coinId, String symbol, String fullName, Integer coinPrecision, String iconUrl, Boolean enabled) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be a positive number");
        }
        Coin coin = coinRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("coin not found"));

        int normalizedCoinId = validateCoinId(coinId);
        String normalizedSymbol = requireText(symbol, "symbol");
        String normalizedFullName = requireText(fullName, "fullName");
        int normalizedPrecision = validatePrecision(coinPrecision, "coinPrecision");
        String normalizedIconUrl = trimToNull(iconUrl);
        Boolean normalizedEnabled = enabled == null ? Boolean.TRUE : enabled;

        if (coinRepository.existsByCoinIdAndIdNot(normalizedCoinId, id)) {
            throw new IllegalArgumentException("coinId already exists");
        }

        coin.setCoinId(normalizedCoinId);
        coin.setSymbol(normalizedSymbol.toUpperCase());
        coin.setFullName(normalizedFullName);
        coin.setCoinPrecision(normalizedPrecision);
        coin.setIconUrl(normalizedIconUrl);
        coin.setEnabled(normalizedEnabled);
        return coinRepository.save(coin);
    }

    private int validateCoinId(Integer coinId) {
        if (coinId == null || coinId < 0) {
            throw new IllegalArgumentException("coinId must be a non-negative integer");
        }
        return coinId;
    }

    private String requireText(String value, String field) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return normalized;
    }

    private int validatePrecision(Integer precision, String field) {
        if (precision == null || precision < 0) {
            throw new IllegalArgumentException(field + " must be a non-negative integer");
        }
        return precision;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
