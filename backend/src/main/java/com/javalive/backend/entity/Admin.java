package com.javalive.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone")
    private String phone;

    @Column(name = "dashboard_style", nullable = false)
    private String dashboardStyle;

    @Column(name = "remember_token")
    private String rememberToken;

    @Column(name = "status", nullable = false)
    private String status;

    // Values: Super Admin / Admin / Rentention Agent / Conversion Agent
    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "enable_2fa", nullable = false)
    private Boolean enable2fa;

    @Column(name = "token_2fa")
    private String token2fa;

    @Column(name = "token_2fa_expiry")
    private LocalDateTime token2faExpiry;

    @Column(name = "pass_2fa", nullable = false)
    private Boolean pass2fa;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
