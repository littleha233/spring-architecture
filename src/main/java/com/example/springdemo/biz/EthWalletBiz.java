package com.example.springdemo.biz;

import com.example.springdemo.domain.EthWallet;

import java.util.List;

public interface EthWalletBiz {
    EthWallet generateAndSave(Long uid);

    List<EthWallet> listAll();

    List<EthWallet> listByUid(Long uid);
}
