package com.example.springdemo.repository;

import com.example.springdemo.domain.CoinChainConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoinChainConfigRepository extends JpaRepository<CoinChainConfig, Long> {
    List<CoinChainConfig> findByCoinIdOrderByIdDesc(Long coinId);

    List<CoinChainConfig> findAllByOrderByIdDesc();

    boolean existsByCoinIdAndBlockchainId(Long coinId, Integer blockchainId);

    boolean existsByCoinIdAndBlockchainIdAndIdNot(Long coinId, Integer blockchainId, Long id);
}
