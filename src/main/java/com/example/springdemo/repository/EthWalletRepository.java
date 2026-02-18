package com.example.springdemo.repository;

import com.example.springdemo.domain.EthWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EthWalletRepository extends JpaRepository<EthWallet, Long> {
    List<EthWallet> findAllByOrderByCreateTimeDesc();

    List<EthWallet> findByUidOrderByCreateTimeDesc(Long uid);

    long countByUid(Long uid);

    Optional<EthWallet> findByIdAndUid(Long id, Long uid);
}
