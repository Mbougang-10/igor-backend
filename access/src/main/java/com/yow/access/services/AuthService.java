package com.yow.access.services;

import com.yow.access.config.email.EmailService;
import com.yow.access.config.security.jwt.JwtService;
import com.yow.access.dto.*;
import com.yow.access.entities.AppUser;
import com.yow.access.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final ResourceRepository resourceRepository;
    private final RoleRepository roleRepository;
    private final UserRoleResourceRepository userRoleResourceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthService(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            ResourceRepository resourceRepository,
            RoleRepository roleRepository,
            UserRoleResourceRepository userRoleResourceRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.resourceRepository = resourceRepository;
        this.roleRepository = roleRepository;
        this.userRoleResourceRepository = userRoleResourceRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    private static final String GENERIC_AUTH_ERROR = "Email ou mot de passe incorrect";
    private static final int ACTIVATION_TOKEN_EXPIRY_HOURS = 24;
    private static final int PASSWORD_RESET_TOKEN_EXPIRY_HOURS = 1;
    private static final String PENDING_ACTIVATION_PASSWORD = "PENDING_ACTIVATION";

    /**
     * Enregistre un nouveau tenant et son administrateur
     */
    @Transactional
    public AuthResponse registerTenant(RegisterTenantRequest request) {
        log.debug("Enregistrement d'un nouveau tenant: {}", request.getOrganizationName());

        // 1. Vérifications préalables
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà.");
        }

        // On génère un code unique pour le tenant basé sur le nom
        String tenantCode = request.getOrganizationName().toUpperCase().replaceAll("[^A-Z0-9]", "_");
        if (tenantCode.length() > 20) tenantCode = tenantCode.substring(0, 20);
        
        int suffix = 1;
        String originalCode = tenantCode;
        while (tenantRepository.existsByCode(tenantCode)) {
            tenantCode = originalCode + "_" + suffix++;
        }

        // 2. Création de l'utilisateur Admin
        AppUser adminUser = new AppUser();
        adminUser.setUsername(request.getEmail());
        adminUser.setEmail(request.getEmail());
        adminUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        adminUser.setEnabled(true);
        adminUser.setAccountActivated(true);
        adminUser.setMustChangePassword(false);
        adminUser.setCreatedAt(Instant.now());
        
        userRepository.save(adminUser);

        // 3. Création du Tenant
        com.yow.access.entities.Tenant tenant = new com.yow.access.entities.Tenant();
        tenant.setName(request.getOrganizationName());
        tenant.setCode(tenantCode);
        tenant.setStatus("ACTIVE");
        
        tenantRepository.save(tenant);

        // 4. Création de la Ressource Racine (Département Principal)
        com.yow.access.entities.Resource rootResource = com.yow.access.entities.ResourceFactory.createRootResource(tenant, request.getOrganizationName());
        resourceRepository.save(rootResource);

        // 5. Assignation du Rôle TENANT_ADMIN
        com.yow.access.entities.Role adminRole = roleRepository.findByName("TENANT_ADMIN")
                .orElseThrow(() -> new IllegalStateException("Le rôle TENANT_ADMIN est introuvable en base."));

        com.yow.access.entities.UserRoleResource urr = com.yow.access.entities.UserRoleResourceFactory.create(adminUser, adminRole, rootResource);
        userRoleResourceRepository.save(urr);

        // 6. Génération Token et Réponse
        List<String> roles = Collections.singletonList("TENANT_ADMIN");
        String token = jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), roles);

        log.info("Tenant '{}' créé avec succès par {}", tenant.getName(), adminUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(adminUser.getId())
                .email(adminUser.getEmail())
                .username(adminUser.getUsername())
                .roles(roles)
                .mustChangePassword(false)
                .build();
    }

    /**
     * Enregistre un nouvel utilisateur simple (sans création de tenant)
     * L'utilisateur pourra ensuite être invité à rejoindre des organisations
     */
    @Transactional
    public AuthResponse registerUser(RegisterUserRequest request) {
        log.debug("Enregistrement d'un nouvel utilisateur: {}", request.getEmail());

        // Vérifications préalables
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris.");
        }

        // Création de l'utilisateur
        AppUser newUser = new AppUser();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setEnabled(true);
        newUser.setAccountActivated(true);
        newUser.setMustChangePassword(false);
        newUser.setCreatedAt(Instant.now());

        userRepository.save(newUser);

        log.info("Utilisateur créé avec succès: {}", newUser.getEmail());

        // Générer un token JWT pour connexion automatique
        // L'utilisateur n'a aucun rôle pour le moment, il devra être invité à une organisation
        String token = jwtService.generateToken(newUser.getId(), newUser.getEmail(), Collections.emptyList());

        return new AuthResponse(
                token,
                newUser.getId(),
                newUser.getEmail(),
                newUser.getUsername(),
                Collections.emptyList() // Pas de rôles initialement
        );
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

        // Récupérer les rôles de l'utilisateur
        List<String> roles = userRoleResourceRepository.findAllByUserId(user.getId())
                .stream()
                .map(urr -> urr.getRole().getName())
                .collect(Collectors.toList());

        // Hack temporaire pour l'admin par défaut s'il n'a pas de rôle en base
        if (roles.isEmpty() && (user.getUsername().equals("admin") || user.getEmail().equals("admin@example.com"))) {
            roles.add("ADMIN");
        }
        
        if (roles.isEmpty() && (user.getUsername().equals("testuser") || user.getEmail().equals("test@yow.com"))) {
            roles.add("USER");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), roles);
        log.info("Connexion réussie pour l'utilisateur: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .roles(roles)
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    /**
     * Valide l'utilisateur pour la connexion
     */
    private void validateUserForLogin(AppUser user, String providedPassword) {
        if (!user.isAccountActivated()) {
            log.warn("Tentative de connexion avec compte non activé: {}", user.getEmail());
            throw new IllegalStateException("Votre compte n'est pas encore activé. Veuillez vérifier vos emails.");
        }

        if (!user.isEnabled()) {
            log.warn("Tentative de connexion avec compte désactivé: {}", user.getEmail());
            throw new IllegalStateException("Votre compte a été désactivé. Contactez l'administrateur.");
        }

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

    private void validateNewUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("L'adresse email " + request.getEmail() + " est déjà utilisée.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Le nom d'utilisateur " + request.getUsername() + " est déjà utilisé.");
        }
    }

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
        String activationToken = generateActivationToken();
        user.setActivationToken(activationToken);
        user.setActivationTokenExpiry(Instant.now().plus(ACTIVATION_TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS));
        return user;
    }

    private void sendActivationEmail(AppUser user) {
        try {
            emailService.sendActivationEmail(user.getEmail(), user.getUsername(), user.getActivationToken());
            log.debug("Email d'activation envoyé à: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email d'activation à {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Transactional
    public AuthResponse activateAccount(ActivateAccountRequest request) {
        log.debug("Tentative d'activation de compte avec token: {}", maskToken(request.getToken()));
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }
        AppUser user = userRepository.findByActivationToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Le lien d'activation est invalide ou a expiré."));
        if (user.getActivationTokenExpiry().isBefore(Instant.now())) {
            throw new IllegalStateException("Le lien d'activation a expiré.");
        }
        if (user.isAccountActivated()) {
            throw new IllegalStateException("Ce compte est déjà activé.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setAccountActivated(true);
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);
        userRepository.save(user);
        
        String token = jwtService.generateToken(user.getId(), user.getEmail(), Collections.emptyList());
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .roles(Collections.emptyList())
                .mustChangePassword(false)
                .build();
    }

    @Transactional
    public void requestPasswordReset(String email) {
        AppUser user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !user.isAccountActivated() || !user.isEnabled()) return;
        user.setActivationToken(generateActivationToken());
        user.setActivationTokenExpiry(Instant.now().plus(PASSWORD_RESET_TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS));
        userRepository.save(user);
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), user.getActivationToken());
        } catch (Exception e) {
            log.error("Erreur envoi email reset: {}", e.getMessage());
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }
        AppUser user = userRepository.findByActivationToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Lien invalide ou expiré."));
        if (user.getActivationTokenExpiry().isBefore(Instant.now())) {
            throw new IllegalStateException("Lien expiré.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    private String generateActivationToken() {
        return UUID.randomUUID().toString();
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) return "***";
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}