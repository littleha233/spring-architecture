package com.example.springdemo.service.eth;

import com.example.springdemo.config.EthTxProperties;
import com.example.springdemo.service.exception.EthWithdrawalException;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthMaxPriorityFeePerGas;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

@Component
public class EthereumChainService {
    private final EthTxProperties ethTxProperties;
    private final Web3j web3j;

    public EthereumChainService(EthTxProperties ethTxProperties) {
        this.ethTxProperties = ethTxProperties;
        if (ethTxProperties.getRpcUrl() == null || ethTxProperties.getRpcUrl().isBlank()) {
            throw new IllegalStateException("eth.tx.rpc-url is required");
        }
        this.web3j = Web3j.build(new HttpService(ethTxProperties.getRpcUrl()));
    }

    public BigInteger getPendingNonce(String address) {
        try {
            EthGetTransactionCount response = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send();
            return response.getTransactionCount();
        } catch (IOException e) {
            throw new EthWithdrawalException("Failed to fetch account nonce", e);
        }
    }

    public BigInteger estimateGasForEthTransfer(String fromAddress, String toAddress, BigInteger amountWei) {
        BigInteger fallback = BigInteger.valueOf(ethTxProperties.getDefaultGasLimit());
        if (!ethTxProperties.isEstimateGasEnabled()) {
            return fallback;
        }

        try {
            Transaction tx = Transaction.createEtherTransaction(fromAddress, null, null, null, toAddress, amountWei);
            EthEstimateGas response = web3j.ethEstimateGas(tx).send();
            if (response.hasError() || response.getAmountUsed() == null) {
                return fallback;
            }
            BigInteger estimated = response.getAmountUsed();
            BigInteger withBuffer = estimated.multiply(BigInteger.valueOf(12)).divide(BigInteger.TEN);
            return withBuffer.max(fallback);
        } catch (IOException e) {
            return fallback;
        }
    }

    public FeeSuggestion suggestEip1559Fees() {
        BigInteger fallbackPriority = toWeiFromGwei(ethTxProperties.getDefaultMaxPriorityFeeGwei());
        BigInteger fallbackMaxFee = toWeiFromGwei(ethTxProperties.getDefaultMaxFeeGwei());

        BigInteger maxPriorityFeePerGas = fallbackPriority;
        BigInteger maxFeePerGas = fallbackMaxFee;

        try {
            EthMaxPriorityFeePerGas priorityResponse = web3j.ethMaxPriorityFeePerGas().send();
            if (!priorityResponse.hasError() && priorityResponse.getMaxPriorityFeePerGas() != null) {
                maxPriorityFeePerGas = priorityResponse.getMaxPriorityFeePerGas();
            }
        } catch (IOException ignored) {
            // keep fallback
        }

        try {
            EthBlock blockResponse = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send();
            EthBlock.Block block = blockResponse.getBlock();
            BigInteger baseFee = block == null ? null : block.getBaseFeePerGas();
            if (baseFee != null) {
                maxFeePerGas = baseFee.multiply(BigInteger.TWO).add(maxPriorityFeePerGas);
            }
        } catch (IOException ignored) {
            // keep fallback
        }

        if (maxFeePerGas.compareTo(maxPriorityFeePerGas) < 0) {
            maxFeePerGas = maxPriorityFeePerGas;
        }

        return new FeeSuggestion(maxPriorityFeePerGas, maxFeePerGas);
    }

    public String sendRawTransaction(String signedRawTransactionHex) {
        try {
            EthSendTransaction response = web3j.ethSendRawTransaction(signedRawTransactionHex).send();
            if (response.hasError()) {
                throw new EthWithdrawalException("Failed to broadcast transaction: " + response.getError().getMessage());
            }
            if (response.getTransactionHash() == null || response.getTransactionHash().isBlank()) {
                throw new EthWithdrawalException("Broadcast response has empty transaction hash");
            }
            return response.getTransactionHash();
        } catch (IOException e) {
            throw new EthWithdrawalException("Failed to broadcast transaction", e);
        }
    }

    public long chainId() {
        return ethTxProperties.getChainId();
    }

    private BigInteger toWeiFromGwei(long gwei) {
        return Convert.toWei(BigDecimal.valueOf(gwei), Convert.Unit.GWEI).toBigInteger();
    }

    public record FeeSuggestion(BigInteger maxPriorityFeePerGas, BigInteger maxFeePerGas) {
    }
}
