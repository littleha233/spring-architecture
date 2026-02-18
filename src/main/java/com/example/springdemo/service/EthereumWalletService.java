package com.example.springdemo.service;

import com.example.springdemo.biz.EthWalletBiz;
import com.example.springdemo.config.EthWalletProperties;
import com.example.springdemo.domain.EthWallet;
import com.example.springdemo.repository.EthWalletRepository;
import com.example.springdemo.service.exception.EthAddressLimitExceededException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.List;

@Service
public class EthereumWalletService implements EthWalletBiz {
    private static final String BC_PROVIDER = "BC";

    private final EthWalletRepository ethWalletRepository;
    private final EthWalletProperties ethWalletProperties;

    public EthereumWalletService(EthWalletRepository ethWalletRepository, EthWalletProperties ethWalletProperties) {
        this.ethWalletRepository = ethWalletRepository;
        this.ethWalletProperties = ethWalletProperties;
        if (Security.getProvider(BC_PROVIDER) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Override
    public EthWallet generateAndSave(Long uid) {
        long currentCount = ethWalletRepository.countByUid(uid);
        int maxCountPerUser = ethWalletProperties.getMaxCountPerUser();
        if (currentCount >= maxCountPerUser) {
            throw new EthAddressLimitExceededException("生成地址数量已达上限（最多 " + maxCountPerUser + " 个）");
        }

        try {
            KeyPair keyPair = generateKeyPair();
            ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
            ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();

            String privateKeyHex = toHexWithPrefix(toFixedLength(privateKey.getS(), 32));
            byte[] publicKeyBytes = toUncompressedPublicKey(publicKey);
            String publicKeyHex = toHexWithPrefix(publicKeyBytes);
            String address = buildAddress(publicKeyBytes);

            EthWallet wallet = new EthWallet(uid, privateKeyHex, publicKeyHex, address);
            return ethWalletRepository.save(wallet);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to generate ethereum wallet", e);
        }
    }

    @Override
    public List<EthWallet> listAll() {
        return ethWalletRepository.findAllByOrderByCreateTimeDesc();
    }

    @Override
    public List<EthWallet> listByUid(Long uid) {
        return ethWalletRepository.findByUidOrderByCreateTimeDesc(uid);
    }

    private KeyPair generateKeyPair() throws GeneralSecurityException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", BC_PROVIDER);
        keyPairGenerator.initialize(new ECGenParameterSpec("secp256k1"));
        return keyPairGenerator.generateKeyPair();
    }

    private String buildAddress(byte[] uncompressedPublicKey) throws GeneralSecurityException {
        MessageDigest digest = MessageDigest.getInstance("KECCAK-256", BC_PROVIDER);
        byte[] hashed = digest.digest(Arrays.copyOfRange(uncompressedPublicKey, 1, uncompressedPublicKey.length));
        byte[] addressBytes = Arrays.copyOfRange(hashed, hashed.length - 20, hashed.length);
        return toHexWithPrefix(addressBytes);
    }

    private byte[] toUncompressedPublicKey(ECPublicKey publicKey) {
        byte[] x = toFixedLength(publicKey.getW().getAffineX(), 32);
        byte[] y = toFixedLength(publicKey.getW().getAffineY(), 32);
        byte[] result = new byte[65];
        result[0] = 0x04;
        System.arraycopy(x, 0, result, 1, 32);
        System.arraycopy(y, 0, result, 33, 32);
        return result;
    }

    private byte[] toFixedLength(BigInteger value, int length) {
        byte[] source = value.toByteArray();
        byte[] target = new byte[length];
        int copyLength = Math.min(source.length, length);
        System.arraycopy(source, source.length - copyLength, target, length - copyLength, copyLength);
        return target;
    }

    private String toHexWithPrefix(byte[] bytes) {
        StringBuilder builder = new StringBuilder(2 + bytes.length * 2);
        builder.append("0x");
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
