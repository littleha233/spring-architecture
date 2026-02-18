package com.example.springdemo.repository;

import com.example.springdemo.domain.Coin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoinRepository extends JpaRepository<Coin, Long> {
    boolean existsByCoinId(Integer coinId);

    boolean existsByCoinIdAndIdNot(Integer coinId, Long id);

    List<Coin> findAllByOrderByIdDesc();
}
