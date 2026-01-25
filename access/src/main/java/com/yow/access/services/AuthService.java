package com.yow.access.services;

import com.yow.access.config.email.EmailService;
import com.yow.access.config.security.jwt.JwtService;
import com.yow.access.dto.*;
import com.yow.access.entities.AppUser;
import com.yow.access.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    /**
     * Login - Authentification d'un utilisateur
     */
    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect"));

        if (!user.isAccountActivated()) {
            throw new IllegalStateException("Compte non active. Verifiez votre email.");
        }

        if (!user.isEnabled()) {
            throw new IllegalStateException("Compte desactive. Contactez l'administrateur.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Email ou mot de passe incorrect");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.isMustChangePassword()
        );
    }

    /**
     * Creer un utilisateur (par un admin)
     */
    @Transactional
    public AppUser createUser(CreateUserRequest request, AppUser createdBy) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Cet email est deja utilise");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est deja utilise");
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash("PENDING_ACTIVATION");
        user.setEnabled(true);
        user.setAccountActivated(false);
        user.setCreatedBy(createdBy);

        String activationToken = UUID.randomUUID().toString();
        user.setActivationToken(activationToken);
        user.setActivationTokenExpiry(Instant.now().plus(24, ChronoUnit.HOURS));

        userRepository.save(user);

        // Envoyer l'email d'activation
        try {
            emailService.sendActivationEmail(
                    user.getEmail(),
                    user.getUsername(),
                    activationToken
            );
        } catch (Exception e) {
            // Log l'erreur mais ne pas bloquer la creation
            System.err.println("Erreur envoi email: " + e.getMessage());
        }

        return user;
    }

    /**
     * Activer un compte et definir le mot de passe
     */
    @Transactional
    public AuthResponse activateAccount(ActivateAccountRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }

        AppUser user = userRepository.findByActivationToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token d'activation invalide"));

        if (user.getActivationTokenExpiry().isBefore(Instant.now())) {
            throw new IllegalStateException("Le token d'activation a expire");
        }

        if (user.isAccountActivated()) {
            throw new IllegalStateException("Ce compte est deja active");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setAccountActivated(true);
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);
        user.setMustChangePassword(false);

        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                false
        );
    }

    /**
     * Demander la reinitialisation du mot de passe
     */
    @Transactional
    public void requestPasswordReset(String email) {
        AppUser user = userRepository.findByEmail(email)
                .orElse(null);

        // Ne pas reveler si l'email existe ou non
        if (user == null) {
            return;
        }

        String resetToken = UUID.randomUUID().toString();
        user.setActivationToken(resetToken);
        user.setActivationTokenExpiry(Instant.now().plus(1, ChronoUnit.HOURS));

        userRepository.save(user);

        try {
            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getUsername(),
                    resetToken
            );
        } catch (Exception e) {
            System.err.println("Erreur envoi email: " + e.getMessage());
        }
    }

    /**
     * Reinitialiser le mot de passe
     */
    @Transactional
    public void resetPassword(ActivateAccountRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }

        AppUser user = userRepository.findByActivationToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (user.getActivationTokenExpiry().isBefore(Instant.now())) {
            throw new IllegalStateException("Le token a expire");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);
        user.setMustChangePassword(false);

        userRepository.save(user);
    }
}
