package com.yow.access.controllers;

import com.yow.access.config.security.context.AuthenticatedUserContext;
import com.yow.access.dto.AssignRoleRequest;
import com.yow.access.dto.CreateUserRequest;
import com.yow.access.entities.AppUser;
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
    private final AuthenticatedUserContext userContext;

    public UserController(
            UserService userService,
            AuthenticatedUserContext userContext
    ) {
        this.userService = userService;
        this.userContext = userContext;
    }

    /* ============================
       CREATE USER
       ============================ */
    @PostMapping
    public ResponseEntity<AppUser> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        AppUser user =
                userService.createUser(
                        request.getUsername(),
                        request.getEmail(),
                        request.getPasswordHash()
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
}
