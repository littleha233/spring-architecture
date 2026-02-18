package com.example.springdemo.biz;

import com.example.springdemo.domain.Coin;

import java.util.List;

public interface CoinBiz {
    List<Coin> list();

    Coin create(Integer coinId, String symbol, String fullName, Integer coinPrecision, String iconUrl, Boolean enabled);

    Coin update(Long id, Integer coinId, String symbol, String fullName, Integer coinPrecision, String iconUrl, Boolean enabled);
}
