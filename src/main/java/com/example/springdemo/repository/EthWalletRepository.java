package com.example.springdemo.repository;

import com.example.springdemo.domain.EthWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EthWalletRepository extends JpaRepository<EthWallet, Long> {
    List<EthWallet> findAllByOrderByCreatedAtDesc();

    List<EthWallet> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);
}
