package com.javalive.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mt4_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mt4Detail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "mt4_id")
    private String mt4Id;

    @Column(name = "mt4_password_encrypted")
    private String mt4PasswordEncrypted;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "currency")
    private String currency;

    @Column(name = "leverage")
    private String leverage;

    @Column(name = "server")
    private String server;

    @Column(name = "options_note")
    private String optionsNote;

    @Column(name = "duration")
    private String duration;

    @Column(name = "status")
    private String status;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "reminded_at")
    private LocalDateTime remindedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mt4Detail other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
