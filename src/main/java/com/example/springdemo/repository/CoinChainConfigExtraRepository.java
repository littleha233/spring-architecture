package com.example.springdemo.repository;

import com.example.springdemo.domain.CoinChainConfigExtra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoinChainConfigExtraRepository extends JpaRepository<CoinChainConfigExtra, Long> {
    List<CoinChainConfigExtra> findByChainConfigIdOrderByIdAsc(Long chainConfigId);

    Optional<CoinChainConfigExtra> findByChainConfigIdAndParamKeyIgnoreCase(Long chainConfigId, String paramKey);
}
