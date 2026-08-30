package com.javalive.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Connect-wallet (mnemonic) feature. {@code phraseEncrypted} replaces the legacy plaintext
 * `phrase` column per the security flag in docs/parity-checklist.md — the application layer
 * must encrypt the mnemonic phrase before insert.
 */
@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "wallet_name")
    private String walletName;

    @Column(name = "phrase_encrypted", columnDefinition = "TEXT")
    private String phraseEncrypted;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "last_validated")
    private LocalDateTime lastValidated;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Wallet other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
