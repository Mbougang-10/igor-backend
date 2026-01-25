package com.yow.access.controllers;

import com.yow.access.dto.*;
import com.yow.access.entities.AppUser;
import com.yow.access.repositories.UserRepository;
import com.yow.access.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    /**
     * POST /api/auth/login
     * Connexion d'un utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/activate
     * Activer un compte et definir le mot de passe
     */
    @PostMapping("/activate")
    public ResponseEntity<AuthResponse> activateAccount(@Valid @RequestBody ActivateAccountRequest request) {
        AuthResponse response = authService.activateAccount(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/forgot-password
     * Demander la reinitialisation du mot de passe
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.requestPasswordReset(email);
        return ResponseEntity.ok(Map.of("message", "Si cet email existe, un lien de reinitialisation a ete envoye."));
    }

    /**
     * POST /api/auth/reset-password
     * Reinitialiser le mot de passe
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ActivateAccountRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Mot de passe reinitialise avec succes."));
    }

    /**
     * POST /api/auth/users
     * Creer un nouvel utilisateur (admin seulement)
     */
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        AppUser newUser = authService.createUser(request, currentUser);
        return ResponseEntity.ok(Map.of(
                "message", "Utilisateur cree avec succes. Un email d'activation a ete envoye.",
                "userId", newUser.getId(),
                "email", newUser.getEmail()
        ));
    }

    /**
     * GET /api/auth/me
     * Obtenir les informations de l'utilisateur connecte
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal AppUser currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Non authentifie"));
        }

        return ResponseEntity.ok(Map.of(
                "userId", currentUser.getId(),
                "username", currentUser.getUsername(),
                "email", currentUser.getEmail(),
                "enabled", currentUser.isEnabled(),
                "accountActivated", currentUser.isAccountActivated()
        ));
    }

    /**
     * GET /api/auth/users
     * Lister tous les utilisateurs
     */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> listUsers() {
        List<AppUser> users = userRepository.findAll();
        List<Map<String, Object>> response = users.stream()
                .map(user -> Map.<String, Object>of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "enabled", user.isEnabled(),
                        "accountActivated", user.isAccountActivated(),
                        "createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""
                ))
                .toList();
        return ResponseEntity.ok(response);
    }
}
