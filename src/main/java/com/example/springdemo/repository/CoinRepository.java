package com.example.springdemo.repository;

import com.example.springdemo.domain.Coin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoinRepository extends JpaRepository<Coin, Long> {
    boolean existsByCoinId(String coinId);

    boolean existsByCoinIdAndIdNot(String coinId, Long id);

    List<Coin> findAllByOrderByIdDesc();
}
