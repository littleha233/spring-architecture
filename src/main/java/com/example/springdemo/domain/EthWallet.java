package com.example.springdemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Data;

@Entity
@Table(name = "eth_wallet")
@Data
public class EthWallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid")
    private Long uid;

    @Column(nullable = false, length = 42)
    private String address;

    @Column(nullable = false, length = 66)
    private String privateKey;

    @Column(nullable = false, length = 132)
    private String publicKey;

    @Column(name = "create_time")
    private Instant createTime;

    protected EthWallet() {
        // for JPA
    }

    public EthWallet(Long uid, String privateKey, String publicKey, String address) {
        this.uid = uid;
        this.address = address;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    @PrePersist
    public void onCreate() {
        if (createTime == null) {
            createTime = Instant.now();
        }
    }
}
