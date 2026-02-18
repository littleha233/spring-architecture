package com.example.springdemo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "eth.tx")
@Getter
@Setter
public class EthTxProperties {
    private String rpcUrl = "http://localhost:8545";
    private List<String> rpcUrls = new ArrayList<>();
    private long chainId = 1L;
    private long defaultGasLimit = 21000L;
    private long defaultMaxPriorityFeeGwei = 2L;
    private long defaultMaxFeeGwei = 30L;
    private boolean estimateGasEnabled = true;
    private long connectTimeoutMs = 5000L;
    private long readTimeoutMs = 10000L;
}
