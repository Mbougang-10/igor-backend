package com.yow.access.services;

import com.yow.access.config.email.EmailService;
import com.yow.access.config.security.jwt.JwtService;
import com.yow.access.dto.*;
import com.yow.access.entities.AppUser;
import com.yow.access.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    private static final String GENERIC_AUTH_ERROR = "Email ou mot de passe incorrect";
    private static final int ACTIVATION_TOKEN_EXPIRY_HOURS = 24;
    private static final int PASSWORD_RESET_TOKEN_EXPIRY_HOURS = 1;
    private static final String PENDING_ACTIVATION_PASSWORD = "PENDING_ACTIVATION";

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
     * Authentifie un utilisateur et retourne un token JWT
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.debug("Tentative de connexion pour l'email: {}", request.getEmail());

        AppUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Tentative de connexion avec email inexistant: {}", request.getEmail());
                    return new IllegalArgumentException(GENERIC_AUTH_ERROR);
                });

        validateUserForLogin(user, request.getPassword());

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        log.info("Connexion réussie pour l'utilisateur: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    /**
     * Valide l'utilisateur pour la connexion
     */
    private void validateUserForLogin(AppUser user, String providedPassword) {
        // Vérifier l'état du compte
        if (!user.isAccountActivated()) {
            log.warn("Tentative de connexion avec compte non activé: {}", user.getEmail());
            throw new IllegalStateException("Votre compte n'est pas encore activé. Veuillez vérifier vos emails.");
        }

        if (!user.isEnabled()) {
            log.warn("Tentative de connexion avec compte désactivé: {}", user.getEmail());
            throw new IllegalStateException("Votre compte a été désactivé. Contactez l'administrateur.");
        }

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(providedPassword, user.getPasswordHash())) {
            log.warn("Mot de passe incorrect pour l'utilisateur: {}", user.getEmail());
            throw new IllegalArgumentException(GENERIC_AUTH_ERROR);
        }
    }

    /**
     * Crée un nouvel utilisateur (réservé aux administrateurs)
     */
    @Transactional
    public AppUser createUser(CreateUserRequest request, AppUser createdBy) {
        log.debug("Création d'un nouvel utilisateur: {}", request.getEmail());

        validateNewUser(request);

        AppUser user = buildNewUser(request, createdBy);
        userRepository.save(user);

        sendActivationEmail(user);

        log.info("Utilisateur créé avec succès: {} (ID: {})", user.getEmail(), user.getId());
        return user;
    }

    /**
     * Valide les données d'un nouvel utilisateur
     */
    private void validateNewUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("L'adresse email " + request.getEmail() + " est déjà utilisée.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Le nom d'utilisateur " + request.getUsername() + " est déjà utilisé.");
        }
    }

    /**
     * Construit un nouvel utilisateur
     */
    private AppUser buildNewUser(CreateUserRequest request, AppUser createdBy) {
        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(PENDING_ACTIVATION_PASSWORD);
        user.setEnabled(true);
        user.setAccountActivated(false);
        user.setCreatedBy(createdBy);
        user.setCreatedAt(Instant.now());
        user.setMustChangePassword(false);

        // Générer le token d'activation
        String activationToken = generateActivationToken();
        user.setActivationToken(activationToken);
        user.setActivationTokenExpiry(Instant.now().plus(ACTIVATION_TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS));

        return user;
    }

    /**
     * Envoie l'email d'activation
     */
    private void sendActivationEmail(AppUser user) {
        try {
            emailService.sendActivationEmail(
                    user.getEmail(),
                    user.getUsername(),
                    user.getActivationToken()
            );
            log.debug("Email d'activation envoyé à: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email d'activation à {}: {}",
                    user.getEmail(), e.getMessage(), e);
            // On ne bloque pas la création de l'utilisateur, l'admin peut renvoyer l'email
        }
    }

    /**
     * Active un compte utilisateur avec un token d'activation
     */
    @Transactional
    public AuthResponse activateAccount(ActivateAccountRequest request) {
        log.debug("Tentative d'activation de compte avec token: {}", maskToken(request.getToken()));

        validatePasswordConfirmation(request);

        AppUser user = userRepository.findByActivationToken(request.getToken())
                .orElseThrow(() -> {
                    log.warn("Tentative d'activation avec token invalide: {}", maskToken(request.getToken()));
                    return new IllegalArgumentException("Le lien d'activation est invalide ou a expiré.");
                });

        validateActivationToken(user);

        activateUserAccount(user, request.getPassword());

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        log.info("Compte activé avec succès: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .mustChangePassword(false)
                .build();
    }

    /**
     * Valide la confirmation du mot de passe
     */
    private void validatePasswordConfirmation(ActivateAccountRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }
    }

    /**
     * Valide le token d'activation
     */
    private void validateActivationToken(AppUser user) {
        if (user.getActivationTokenExpiry().isBefore(Instant.now())) {
            log.warn("Token d'activation expiré pour l'utilisateur: {}", user.getEmail());
            throw new IllegalStateException("Le lien d'activation a expiré. Veuillez demander un nouveau lien.");
        }

        if (user.isAccountActivated()) {
            log.warn("Tentative d'activation d'un compte déjà activé: {}", user.getEmail());
            throw new IllegalStateException("Ce compte est déjà activé.");
        }
    }

    /**
     * Active le compte utilisateur et définit le mot de passe
     */
    private void activateUserAccount(AppUser user, String password) {
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setAccountActivated(true);
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);
        user.setMustChangePassword(false);

        userRepository.save(user);
    }

    /**
     * Demande la réinitialisation du mot de passe
     */
    @Transactional
    public void requestPasswordReset(String email) {
        log.debug("Demande de réinitialisation de mot de passe pour: {}", email);

        AppUser user = userRepository.findByEmail(email).orElse(null);

        // Pour des raisons de sécurité, on ne révèle pas si l'email existe
        if (user == null) {
            log.debug("Email non trouvé (comportement normal): {}", email);
            return;
        }

        if (!user.isAccountActivated()) {
            log.warn("Tentative de réinitialisation sur compte non activé: {}", email);
            return;
        }

        if (!user.isEnabled()) {
            log.warn("Tentative de réinitialisation sur compte désactivé: {}", email);
            return;
        }

        generatePasswordResetToken(user);
        sendPasswordResetEmail(user);

        log.info("Demande de réinitialisation traitée pour: {}", email);
    }

    /**
     * Génère un token de réinitialisation de mot de passe
     */
    private void generatePasswordResetToken(AppUser user) {
        String resetToken = generateActivationToken();
        user.setActivationToken(resetToken);
        user.setActivationTokenExpiry(Instant.now().plus(PASSWORD_RESET_TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS));

        userRepository.save(user);
    }

    /**
     * Envoie l'email de réinitialisation
     */
    private void sendPasswordResetEmail(AppUser user) {
        try {
            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getUsername(),
                    user.getActivationToken()
            );
            log.debug("Email de réinitialisation envoyé à: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de réinitialisation à {}: {}",
                    user.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Réinitialise le mot de passe avec un token
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.debug("Tentative de réinitialisation de mot de passe avec token: {}",
                maskToken(request.getToken()));

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }

        AppUser user = userRepository.findByActivationToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Le lien de réinitialisation est invalide ou a expiré."
                ));

        validateResetToken(user);
        updateUserPassword(user, request.getPassword());

        log.info("Mot de passe réinitialisé avec succès pour: {}", user.getEmail());
    }

    /**
     * Valide le token de réinitialisation
     */
    private void validateResetToken(AppUser user) {
        if (user.getActivationTokenExpiry().isBefore(Instant.now())) {
            log.warn("Token de réinitialisation expiré pour: {}", user.getEmail());
            throw new IllegalStateException("Le lien de réinitialisation a expiré.");
        }

        if (!user.isAccountActivated()) {
            log.warn("Tentative de réinitialisation sur compte non activé: {}", user.getEmail());
            throw new IllegalStateException("Ce compte n'est pas activé.");
        }
    }

    /**
     * Met à jour le mot de passe de l'utilisateur
     */
    private void updateUserPassword(AppUser user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);
        user.setMustChangePassword(false);

        userRepository.save(user);
    }

    /**
     * Génère un token d'activation/réinitialisation
     */
    private String generateActivationToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Masque partiellement un token pour les logs
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}