package com.example.springdemo.service.eth;

import com.example.springdemo.config.EthTxProperties;
import com.example.springdemo.service.exception.EthWithdrawalException;
import okhttp3.OkHttpClient;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class EthereumChainService {
    private final EthTxProperties ethTxProperties;
    private final Map<String, Web3j> web3jClients;

    public EthereumChainService(EthTxProperties ethTxProperties) {
        this.ethTxProperties = ethTxProperties;
        List<String> urls = resolveRpcUrls(ethTxProperties);
        this.web3jClients = buildClients(urls, ethTxProperties);
    }

    public BigInteger getPendingNonce(String address) {
        return callRequired("fetch account nonce", (web3j) -> {
            try {
                EthGetTransactionCount response = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send();
                return response.getTransactionCount();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public BigInteger estimateGasForEthTransfer(String fromAddress, String toAddress, BigInteger amountWei) {
        BigInteger fallback = BigInteger.valueOf(ethTxProperties.getDefaultGasLimit());
        if (!ethTxProperties.isEstimateGasEnabled()) {
            return fallback;
        }

        BigInteger estimated = callOptional("estimate gas", (web3j) -> {
            try {
                Transaction tx = Transaction.createEtherTransaction(fromAddress, null, null, null, toAddress, amountWei);
                EthEstimateGas response = web3j.ethEstimateGas(tx).send();
                if (response.hasError() || response.getAmountUsed() == null) {
                    return null;
                }
                return response.getAmountUsed();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        if (estimated == null) {
            return fallback;
        }
        BigInteger withBuffer = estimated.multiply(BigInteger.valueOf(12)).divide(BigInteger.TEN);
        return withBuffer.max(fallback);
    }

    public FeeSuggestion suggestEip1559Fees() {
        BigInteger fallbackPriority = toWeiFromGwei(ethTxProperties.getDefaultMaxPriorityFeeGwei());
        BigInteger fallbackMaxFee = toWeiFromGwei(ethTxProperties.getDefaultMaxFeeGwei());

        BigInteger maxPriorityFeePerGas = callOptional("fetch max priority fee", (web3j) -> {
            try {
                EthMaxPriorityFeePerGas response = web3j.ethMaxPriorityFeePerGas().send();
                if (response.hasError()) {
                    return null;
                }
                return response.getMaxPriorityFeePerGas();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        if (maxPriorityFeePerGas == null) {
            maxPriorityFeePerGas = fallbackPriority;
        }

        BigInteger latestBaseFee = callOptional("fetch latest base fee", (web3j) -> {
            try {
                EthBlock response = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send();
                EthBlock.Block block = response.getBlock();
                return block == null ? null : block.getBaseFeePerGas();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        BigInteger maxFeePerGas = fallbackMaxFee;
        if (latestBaseFee != null) {
            maxFeePerGas = latestBaseFee.multiply(BigInteger.TWO).add(maxPriorityFeePerGas);
        }
        if (maxFeePerGas.compareTo(maxPriorityFeePerGas) < 0) {
            maxFeePerGas = maxPriorityFeePerGas;
        }

        return new FeeSuggestion(maxPriorityFeePerGas, maxFeePerGas);
    }

    public String sendRawTransaction(String signedRawTransactionHex) {
        return callRequired("broadcast transaction", (web3j) -> {
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
                throw new RuntimeException(e);
            }
        });
    }

    public long chainId() {
        return ethTxProperties.getChainId();
    }

    private <T> T callRequired(String action, Function<Web3j, T> request) {
        List<String> failures = new ArrayList<>();
        for (Map.Entry<String, Web3j> entry : web3jClients.entrySet()) {
            try {
                return request.apply(entry.getValue());
            } catch (EthWithdrawalException e) {
                throw e;
            } catch (Exception e) {
                failures.add(entry.getKey() + " -> " + messageOf(e));
            }
        }
        throw new EthWithdrawalException("Failed to " + action + ", all RPC nodes failed: " + String.join(" | ", failures));
    }

    private <T> T callOptional(String action, Function<Web3j, T> request) {
        for (Map.Entry<String, Web3j> entry : web3jClients.entrySet()) {
            try {
                T value = request.apply(entry.getValue());
                if (value != null) {
                    return value;
                }
            } catch (Exception ignored) {
                // try next rpc
            }
        }
        return null;
    }

    private String messageOf(Exception e) {
        Throwable cause = e.getCause();
        if (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank()) {
            return cause.getClass().getSimpleName() + ": " + cause.getMessage();
        }
        if (e.getMessage() != null && !e.getMessage().isBlank()) {
            return e.getMessage();
        }
        return e.getClass().getSimpleName();
    }

    private BigInteger toWeiFromGwei(long gwei) {
        return Convert.toWei(BigDecimal.valueOf(gwei), Convert.Unit.GWEI).toBigInteger();
    }

    private List<String> resolveRpcUrls(EthTxProperties properties) {
        List<String> urls = new ArrayList<>();
        if (properties.getRpcUrls() != null) {
            for (String url : properties.getRpcUrls()) {
                if (url != null && !url.isBlank()) {
                    urls.add(url.trim());
                }
            }
        }
        if (urls.isEmpty() && properties.getRpcUrl() != null && !properties.getRpcUrl().isBlank()) {
            urls.add(properties.getRpcUrl().trim());
        }
        if (urls.isEmpty()) {
            throw new IllegalStateException("eth.tx.rpc-url or eth.tx.rpc-urls is required");
        }
        return urls;
    }

    private Map<String, Web3j> buildClients(List<String> urls, EthTxProperties properties) {
        Map<String, Web3j> clients = new LinkedHashMap<>();
        OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(properties.getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
            .readTimeout(properties.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
            .build();

        for (String url : urls) {
            clients.put(url, Web3j.build(new HttpService(url, httpClient)));
        }
        return clients;
    }

    public record FeeSuggestion(BigInteger maxPriorityFeePerGas, BigInteger maxFeePerGas) {
    }
}
