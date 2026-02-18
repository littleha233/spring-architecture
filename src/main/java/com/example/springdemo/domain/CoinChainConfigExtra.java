package com.example.springdemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(
    name = "coin_chain_config_extra",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_coin_chain_config_extra_key", columnNames = {"chain_config_id", "param_key"})
    }
)
@Data
public class CoinChainConfigExtra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chain_config_id", nullable = false)
    private Long chainConfigId;

    @Column(name = "param_key", nullable = false, length = 128)
    private String paramKey;

    @Column(name = "param_value", nullable = false, length = 2000)
    private String paramValue;

    @Column(name = "create_time")
    private Instant createTime;

    @Column(name = "update_time")
    private Instant updateTime;

    protected CoinChainConfigExtra() {
        // for JPA
    }

    public CoinChainConfigExtra(Long chainConfigId, String paramKey, String paramValue) {
        this.chainConfigId = chainConfigId;
        this.paramKey = paramKey;
        this.paramValue = paramValue;
    }

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        if (createTime == null) {
            createTime = now;
        }
        updateTime = now;
    }

    @PreUpdate
    public void onUpdate() {
        updateTime = Instant.now();
    }
}
