package com.example.springdemo.service;

import com.example.springdemo.biz.CoinChainConfigBiz;
import com.example.springdemo.common.error.BusinessException;
import com.example.springdemo.common.error.ErrorCode;
import com.example.springdemo.common.logging.LogContext;
import com.example.springdemo.domain.BlockchainConfig;
import com.example.springdemo.domain.Coin;
import com.example.springdemo.domain.CoinChainConfig;
import com.example.springdemo.repository.BlockchainConfigRepository;
import com.example.springdemo.repository.CoinChainConfigRepository;
import com.example.springdemo.repository.CoinRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CoinChainConfigService implements CoinChainConfigBiz {
    private static final Logger log = LoggerFactory.getLogger(CoinChainConfigService.class);
    private static final int EXTRA_JSON_MAX_LENGTH = 4000;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CoinChainConfigRepository coinChainConfigRepository;
    private final CoinRepository coinRepository;
    private final BlockchainConfigRepository blockchainConfigRepository;

    public CoinChainConfigService(CoinChainConfigRepository coinChainConfigRepository,
                                  CoinRepository coinRepository,
                                  BlockchainConfigRepository blockchainConfigRepository) {
        this.coinChainConfigRepository = coinChainConfigRepository;
        this.coinRepository = coinRepository;
        this.blockchainConfigRepository = blockchainConfigRepository;
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
    public CoinChainConfig create(Long coinId, Integer blockchainId, String chainCode, String chainName, String rpcUrl,
                                  String collectionAddress, String withdrawAddress,
                                  BigDecimal minWithdrawAmount, Integer withdrawPrecision,
                                  BigDecimal minDepositAmount, Integer depositPrecision, String extraJson, Boolean enabled) {
        validateCoinExists(coinId);
        BlockchainConfig blockchainConfig = validateAndResolveBlockchain(blockchainId, chainCode, chainName);
        Integer normalizedBlockchainId = blockchainConfig.getBlockchainId();

        if (coinChainConfigRepository.existsByCoinIdAndBlockchainId(coinId, normalizedBlockchainId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "chain config already exists for this coin and blockchainId");
        }

        CoinChainConfig config = new CoinChainConfig(
            coinId,
            normalizedBlockchainId,
            blockchainConfig.getChainCode(),
            blockchainConfig.getChainName(),
            requireText(rpcUrl, "rpcUrl"),
            requireText(collectionAddress, "collectionAddress"),
            requireText(withdrawAddress, "withdrawAddress"),
            requireAmount(minWithdrawAmount, "minWithdrawAmount"),
            validatePrecision(withdrawPrecision, "withdrawPrecision"),
            requireAmount(minDepositAmount, "minDepositAmount"),
            validatePrecision(depositPrecision, "depositPrecision"),
            normalizeExtraJson(extraJson),
            enabled == null ? Boolean.TRUE : enabled
        );
        CoinChainConfig saved = coinChainConfigRepository.save(config);
        log.info(
            "business traceId={} userId={} operation=create_coin_chain_config details=id:{},coinId:{},blockchainId:{} result=SUCCESS",
            LogContext.traceId(),
            LogContext.currentUserId(),
            saved.getId(),
            saved.getCoinId(),
            saved.getBlockchainId()
        );
        log.info(
            "audit traceId={} userId={} action=create_coin_chain_config target=coinId:{},blockchainId:{}",
            LogContext.traceId(),
            LogContext.currentUserId(),
            saved.getCoinId(),
            saved.getBlockchainId()
        );
        return saved;
    }

    @Override
    public CoinChainConfig update(Long id, Long coinId, Integer blockchainId, String chainCode, String chainName, String rpcUrl,
                                  String collectionAddress, String withdrawAddress,
                                  BigDecimal minWithdrawAmount, Integer withdrawPrecision,
                                  BigDecimal minDepositAmount, Integer depositPrecision, String extraJson, Boolean enabled) {
        validatePositiveId(id, "id");
        validateCoinExists(coinId);
        CoinChainConfig config = coinChainConfigRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "coin chain config not found"));

        BlockchainConfig blockchainConfig = validateAndResolveBlockchain(blockchainId, chainCode, chainName);
        Integer normalizedBlockchainId = blockchainConfig.getBlockchainId();
        if (coinChainConfigRepository.existsByCoinIdAndBlockchainIdAndIdNot(coinId, normalizedBlockchainId, id)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "chain config already exists for this coin and blockchainId");
        }

        config.setCoinId(coinId);
        config.setBlockchainId(normalizedBlockchainId);
        config.setChainCode(blockchainConfig.getChainCode());
        config.setChainName(blockchainConfig.getChainName());
        config.setRpcUrl(requireText(rpcUrl, "rpcUrl"));
        config.setCollectionAddress(requireText(collectionAddress, "collectionAddress"));
        config.setWithdrawAddress(requireText(withdrawAddress, "withdrawAddress"));
        config.setMinWithdrawAmount(requireAmount(minWithdrawAmount, "minWithdrawAmount"));
        config.setWithdrawPrecision(validatePrecision(withdrawPrecision, "withdrawPrecision"));
        config.setMinDepositAmount(requireAmount(minDepositAmount, "minDepositAmount"));
        config.setDepositPrecision(validatePrecision(depositPrecision, "depositPrecision"));
        config.setExtraJson(normalizeExtraJson(extraJson));
        config.setEnabled(enabled == null ? Boolean.TRUE : enabled);

        CoinChainConfig saved = coinChainConfigRepository.save(config);
        log.info(
            "business traceId={} userId={} operation=update_coin_chain_config details=id:{},coinId:{},blockchainId:{} result=SUCCESS",
            LogContext.traceId(),
            LogContext.currentUserId(),
            saved.getId(),
            saved.getCoinId(),
            saved.getBlockchainId()
        );
        log.info(
            "audit traceId={} userId={} action=update_coin_chain_config target=coinId:{},blockchainId:{}",
            LogContext.traceId(),
            LogContext.currentUserId(),
            saved.getCoinId(),
            saved.getBlockchainId()
        );
        return saved;
    }

    private BlockchainConfig validateAndResolveBlockchain(Integer blockchainId, String chainCode, String chainName) {
        int normalizedBlockchainId = validateBlockchainId(blockchainId);
        String normalizedChainCode = requireText(chainCode, "chainCode").toUpperCase();
        String normalizedChainName = requireText(chainName, "chainName");

        BlockchainConfig blockchainConfig = blockchainConfigRepository.findByBlockchainId(normalizedBlockchainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "blockchain config not found for blockchainId"));

        if (Boolean.FALSE.equals(blockchainConfig.getEnabled())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "selected blockchain is disabled");
        }

        if (!blockchainConfig.getChainCode().equalsIgnoreCase(normalizedChainCode)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "chainCode does not match selected blockchainId");
        }
        if (!blockchainConfig.getChainName().equals(normalizedChainName)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "chainName does not match selected blockchainId");
        }
        return blockchainConfig;
    }

    private void validateCoinExists(Long coinId) {
        validatePositiveId(coinId, "coinId");
        Coin coin = coinRepository.findById(coinId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "coin not found"));
        if (Boolean.FALSE.equals(coin.getEnabled())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "selected coin is disabled");
        }
    }

    private void validatePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, fieldName + " must be a positive number");
        }
    }

    private String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, field + " is required");
        }
        return value.trim();
    }

    private BigDecimal requireAmount(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, field + " must be greater than or equal to 0");
        }
        return value;
    }

    private int validatePrecision(Integer precision, String field) {
        if (precision == null || precision < 0) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, field + " must be a non-negative integer");
        }
        return precision;
    }

    private int validateBlockchainId(Integer blockchainId) {
        if (blockchainId == null || blockchainId < 0) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "blockchainId must be a non-negative integer");
        }
        return blockchainId;
    }

    private String normalizeExtraJson(String extraJson) {
        String normalized = extraJson == null ? "{}" : extraJson.trim();
        if (normalized.isEmpty()) {
            normalized = "{}";
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(normalized);
            if (node == null || !node.isObject()) {
                throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "extraJson must be a valid JSON object");
            }
            String compactJson = OBJECT_MAPPER.writeValueAsString(node);
            if (compactJson.length() > EXTRA_JSON_MAX_LENGTH) {
                throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "extraJson exceeds length limit " + EXTRA_JSON_MAX_LENGTH);
            }
            return compactJson;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "extraJson must be a valid JSON object");
        }
    }
}
