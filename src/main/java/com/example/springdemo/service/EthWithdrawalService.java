package com.example.springdemo.service;

import com.example.springdemo.biz.EthWithdrawalBiz;
import com.example.springdemo.domain.EthWallet;
import com.example.springdemo.domain.EthWithdrawal;
import com.example.springdemo.repository.EthWalletRepository;
import com.example.springdemo.repository.EthWithdrawalRepository;
import com.example.springdemo.service.eth.EthereumChainService;
import com.example.springdemo.service.exception.EthWithdrawalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Service
public class EthWithdrawalService implements EthWithdrawalBiz {
    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    private final EthWalletRepository ethWalletRepository;
    private final EthWithdrawalRepository ethWithdrawalRepository;
    private final EthereumChainService ethereumChainService;

    public EthWithdrawalService(EthWalletRepository ethWalletRepository,
                                EthWithdrawalRepository ethWithdrawalRepository,
                                EthereumChainService ethereumChainService) {
        this.ethWalletRepository = ethWalletRepository;
        this.ethWithdrawalRepository = ethWithdrawalRepository;
        this.ethereumChainService = ethereumChainService;
    }

    @Override
    @Transactional
    public EthWithdrawal withdraw(Long uid, Long fromWalletId, String toAddress, BigDecimal amountEth) {
        if (uid == null || uid <= 0) {
            throw new EthWithdrawalException("uid must be a positive number");
        }
        if (fromWalletId == null || fromWalletId <= 0) {
            throw new EthWithdrawalException("fromWalletId must be a positive number");
        }
        if (toAddress == null || !WalletUtils.isValidAddress(toAddress)) {
            throw new EthWithdrawalException("toAddress is not a valid ethereum address");
        }
        if (amountEth == null || amountEth.compareTo(BigDecimal.ZERO) <= 0) {
            throw new EthWithdrawalException("amountEth must be a positive number");
        }

        EthWallet fromWallet = ethWalletRepository.findByIdAndUid(fromWalletId, uid)
            .orElseThrow(() -> new EthWithdrawalException("Selected from address does not exist for this uid"));
        if (!WalletUtils.isValidAddress(fromWallet.getAddress())) {
            throw new EthWithdrawalException("from address in database is invalid");
        }

        BigInteger amountWei = toWei(amountEth);
        BigInteger nonce = ethereumChainService.getPendingNonce(fromWallet.getAddress());
        BigInteger gasLimit = ethereumChainService.estimateGasForEthTransfer(
            fromWallet.getAddress(), toAddress, amountWei
        );
        EthereumChainService.FeeSuggestion feeSuggestion = ethereumChainService.suggestEip1559Fees();

        EthWithdrawal withdrawal = new EthWithdrawal(
            uid,
            fromWalletId,
            fromWallet.getAddress(),
            toAddress,
            amountWei.toString(),
            nonce.toString(),
            gasLimit.toString(),
            feeSuggestion.maxPriorityFeePerGas().toString(),
            feeSuggestion.maxFeePerGas().toString()
        );

        try {
            Credentials credentials = Credentials.create(Numeric.cleanHexPrefix(fromWallet.getPrivateKey()));
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                ethereumChainService.chainId(),
                nonce,
                gasLimit,
                toAddress,
                amountWei,
                feeSuggestion.maxPriorityFeePerGas(),
                feeSuggestion.maxFeePerGas()
            );
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String signedRawTxHex = Numeric.toHexString(signedMessage);
            String txHash = ethereumChainService.sendRawTransaction(signedRawTxHex);

            withdrawal.setStatus("SUBMITTED");
            withdrawal.setTxHash(txHash);
            return ethWithdrawalRepository.save(withdrawal);
        } catch (EthWithdrawalException e) {
            withdrawal.setStatus("FAILED");
            withdrawal.setErrorMessage(truncate(e.getMessage()));
            ethWithdrawalRepository.save(withdrawal);
            throw e;
        } catch (Exception e) {
            withdrawal.setStatus("FAILED");
            withdrawal.setErrorMessage(truncate(e.getMessage()));
            ethWithdrawalRepository.save(withdrawal);
            throw new EthWithdrawalException("Failed to sign and broadcast transaction", e);
        }
    }

    @Override
    public List<EthWithdrawal> listByUid(Long uid) {
        return ethWithdrawalRepository.findByUidOrderByCreateTimeDesc(uid);
    }

    private BigInteger toWei(BigDecimal amountEth) {
        try {
            return Convert.toWei(amountEth, Convert.Unit.ETHER).toBigIntegerExact();
        } catch (ArithmeticException e) {
            throw new EthWithdrawalException("amountEth has too many decimal places", e);
        }
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        if (message.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}
