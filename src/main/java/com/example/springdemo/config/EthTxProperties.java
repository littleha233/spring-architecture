package com.example.springdemo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "eth.tx")
@Getter
@Setter
public class EthTxProperties {
    private String rpcUrl = "http://localhost:8545";
    private long chainId = 1L;
    private long defaultGasLimit = 21000L;
    private long defaultMaxPriorityFeeGwei = 2L;
    private long defaultMaxFeeGwei = 30L;
    private boolean estimateGasEnabled = true;
}
