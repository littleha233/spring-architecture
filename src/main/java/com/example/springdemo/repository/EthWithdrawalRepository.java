package com.example.springdemo.repository;

import com.example.springdemo.domain.EthWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EthWithdrawalRepository extends JpaRepository<EthWithdrawal, Long> {
    List<EthWithdrawal> findByUidOrderByCreateTimeDesc(Long uid);
}
