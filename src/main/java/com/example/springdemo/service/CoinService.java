package com.example.springdemo.service;

import com.example.springdemo.biz.CoinBiz;
import com.example.springdemo.common.error.BusinessException;
import com.example.springdemo.common.error.ErrorCode;
import com.example.springdemo.common.logging.LogContext;
import com.example.springdemo.domain.Coin;
import com.example.springdemo.repository.CoinRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoinService implements CoinBiz {
    private static final Logger log = LoggerFactory.getLogger(CoinService.class);

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
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "coinId already exists");
        }

        Coin coin = new Coin(
            normalizedCoinId,
            normalizedSymbol.toUpperCase(),
            normalizedFullName,
            normalizedPrecision,
            normalizedIconUrl,
            normalizedEnabled
        );
        Coin saved = coinRepository.save(coin);
        log.info(
            "business traceId={} userId={} operation=create_coin details=id:{},coinId:{},symbol:{} result=SUCCESS",
            LogContext.traceId(),
            LogContext.currentUserId(),
            saved.getId(),
            saved.getCoinId(),
            saved.getSymbol()
        );
        log.info(
            "audit traceId={} userId={} action=create_coin target=coinId:{}",
            LogContext.traceId(),
            LogContext.currentUserId(),
            saved.getCoinId()
        );
        return saved;
    }

    @Override
    public Coin update(Long id, Integer coinId, String symbol, String fullName, Integer coinPrecision, String iconUrl, Boolean enabled) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "id must be a positive number");
        }
        Coin coin = coinRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "coin not found"));

        int normalizedCoinId = validateCoinId(coinId);
        String normalizedSymbol = requireText(symbol, "symbol");
        String normalizedFullName = requireText(fullName, "fullName");
        int normalizedPrecision = validatePrecision(coinPrecision, "coinPrecision");
        String normalizedIconUrl = trimToNull(iconUrl);
        Boolean normalizedEnabled = enabled == null ? Boolean.TRUE : enabled;

        if (coinRepository.existsByCoinIdAndIdNot(normalizedCoinId, id)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "coinId already exists");
        }

        coin.setCoinId(normalizedCoinId);
        coin.setSymbol(normalizedSymbol.toUpperCase());
        coin.setFullName(normalizedFullName);
        coin.setCoinPrecision(normalizedPrecision);
        coin.setIconUrl(normalizedIconUrl);
        coin.setEnabled(normalizedEnabled);
        Coin saved = coinRepository.save(coin);
        log.info(
            "business traceId={} userId={} operation=update_coin details=id:{},coinId:{},symbol:{} result=SUCCESS",
            LogContext.traceId(),
            LogContext.currentUserId(),
            saved.getId(),
            saved.getCoinId(),
            saved.getSymbol()
        );
        log.info(
            "audit traceId={} userId={} action=update_coin target=coinId:{}",
            LogContext.traceId(),
            LogContext.currentUserId(),
            saved.getCoinId()
        );
        return saved;
    }

    private int validateCoinId(Integer coinId) {
        if (coinId == null || coinId < 0) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "coinId must be a non-negative integer");
        }
        return coinId;
    }

    private String requireText(String value, String field) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, field + " is required");
        }
        return normalized;
    }

    private int validatePrecision(Integer precision, String field) {
        if (precision == null || precision < 0) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, field + " must be a non-negative integer");
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
