package com.example.springdemo.biz;

import com.example.springdemo.domain.EthWithdrawal;

import java.math.BigDecimal;
import java.util.List;

public interface EthWithdrawalBiz {
    EthWithdrawal withdraw(Long uid, Long fromWalletId, String toAddress, BigDecimal amountEth);

    List<EthWithdrawal> listByUid(Long uid);
}
