package com.example.springdemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import java.time.Instant;
import lombok.Data;

@Entity
@Data
public class EthWallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false, length = 66)
    private String privateKey;

    @Column(nullable = false, length = 132)
    private String publicKey;

    @Column(nullable = false, length = 42)
    private String address;

    private Instant createdAt;

    protected EthWallet() {
        // for JPA
    }

    public EthWallet(Long userId, String privateKey, String publicKey, String address) {
        this.userId = userId;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.address = address;
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
