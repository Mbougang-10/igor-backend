package com.yow.access.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Entity
@Builder
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "must_change_password")
    @Builder.Default
    private boolean mustChangePassword = false;

    @Column(name = "activation_token", length = 255)
    private String activationToken;

    @Column(name = "activation_token_expiry")
    private Instant activationTokenExpiry;

    @Column(name = "account_activated")
    @Builder.Default
    private boolean accountActivated = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AppUser createdBy;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    // Constructeur par défaut pour JPA
    public AppUser() {
    }

    // Constructeur complet pour Lombok Builder
    public AppUser(UUID id, String username, String email, String passwordHash,
                   boolean enabled, boolean mustChangePassword, String activationToken,
                   Instant activationTokenExpiry, boolean accountActivated,
                   AppUser createdBy, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.mustChangePassword = mustChangePassword;
        this.activationToken = activationToken;
        this.activationTokenExpiry = activationTokenExpiry;
        this.accountActivated = accountActivated;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    // Getters & Setters (Lombok les génère déjà via @Getter/@Setter)
    // Vous pouvez les garder ou les supprimer si vous utilisez uniquement Lombok
}