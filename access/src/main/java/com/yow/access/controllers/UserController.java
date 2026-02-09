package com.yow.access.controllers;

import com.yow.access.config.security.context.AuthenticatedUserContext;
import com.yow.access.dto.AssignRoleRequest;
import com.yow.access.dto.CreateUserRequest;
import com.yow.access.entities.AppUser;
import com.yow.access.repositories.UserRepository;
import com.yow.access.services.AuthService;
import com.yow.access.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final AuthenticatedUserContext userContext;

    public UserController(
            UserService userService,
            AuthService authService,
            UserRepository userRepository,
            AuthenticatedUserContext userContext
    ) {
        this.userService = userService;
        this.authService = authService;
        this.userRepository = userRepository;
        this.userContext = userContext;
    }

    /* ============================
       CREATE USER (DIRECT ACTIVE)
       ============================ */
    @PostMapping
    public ResponseEntity<AppUser> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        // Validation: Check if requestor exists (optional security check)
        // UUID creatorId = userContext.getUserId();

        // Use UserService directly to create Active user with provided password
        AppUser user =
                userService.createUser(
                        request.getUsername(),
                        request.getEmail(),
                        request.getPasswordHash() // This is the raw password from request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(user);
    }

    /* ============================
       ENABLE / DISABLE USER
       ============================ */
    @PatchMapping("/{userId}/enabled")
    public ResponseEntity<Void> setUserEnabled(
            @PathVariable UUID userId,
            @RequestParam boolean enabled
    ) {
        userService.setUserEnabled(userId, enabled);
        return ResponseEntity.noContent().build();
    }

    /* ============================
       ASSIGN ROLE (RBAC PROTECTED)
       ============================ */
    @PostMapping("/{userId}/roles")
    public ResponseEntity<Void> assignRole(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRoleRequest request
    ) {
        UUID actorUserId = userContext.getUserId();

        userService.assignRole(
                actorUserId,
                userId,
                request.getRoleId(),
                request.getResourceId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /* ============================
       GET USERS BY TENANT
       ============================ */
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<java.util.List<AppUser>> getUsersByTenant(
            @PathVariable UUID tenantId
    ) {
        // En vrai, il faudrait ajouter une pagination
        return ResponseEntity.ok(userService.getUsersByTenant(tenantId));
    }

    /* ============================
       SEARCH USER BY EMAIL
       ============================ */
    @GetMapping("/search")
    public ResponseEntity<AppUser> getUserByEmail(@RequestParam String email) {
        // TODO: Restreindre qui peut chercher ? Pour l'instant on laisse ouvert aux utilisateurs authentifiés
        // pour faciliter l'invitation.
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /* ============================
       GET USER ROLES
       ============================ */
    @GetMapping("/{userId}/roles")
    public ResponseEntity<java.util.List<com.yow.access.dto.UserRoleDTO>> getUserRoles(@PathVariable UUID userId) {
        // TODO: Check if requesting user has right to view roles of target user
        return ResponseEntity.ok(userService.getUserRoles(userId));
    }

    /* ============================
       REMOVE ROLE
       ============================ */
    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<Void> removeRole(
            @PathVariable UUID userId,
            @PathVariable Short roleId,
            @RequestParam UUID resourceId
    ) {
        UUID actorUserId = userContext.getUserId();

        userService.removeRole(
                actorUserId,
                userId,
                roleId,
                resourceId
        );

        return ResponseEntity.noContent().build();
    }
}
