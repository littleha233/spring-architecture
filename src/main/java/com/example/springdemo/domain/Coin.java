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
    name = "coin",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_coin_coin_id", columnNames = "coin_id")
    }
)
@Data
public class Coin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coin_id", nullable = false, length = 64)
    private String coinId;

    @Column(name = "symbol", nullable = false, length = 32)
    private String symbol;

    @Column(name = "full_name", nullable = false, length = 128)
    private String fullName;

    @Column(name = "coin_precision", nullable = false)
    private Integer coinPrecision;

    @Column(name = "icon_url", length = 512)
    private String iconUrl;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "create_time")
    private Instant createTime;

    @Column(name = "update_time")
    private Instant updateTime;

    protected Coin() {
        // for JPA
    }

    public Coin(String coinId, String symbol, String fullName, Integer coinPrecision, String iconUrl, Boolean enabled) {
        this.coinId = coinId;
        this.symbol = symbol;
        this.fullName = fullName;
        this.coinPrecision = coinPrecision;
        this.iconUrl = iconUrl;
        this.enabled = enabled;
    }

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        if (createTime == null) {
            createTime = now;
        }
        updateTime = now;
        if (enabled == null) {
            enabled = Boolean.TRUE;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updateTime = Instant.now();
    }
}
