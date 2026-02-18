package com.example.springdemo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "eth.wallet")
@Getter
@Setter
public class EthWalletProperties {
    private int maxCountPerUser = 20;
}
