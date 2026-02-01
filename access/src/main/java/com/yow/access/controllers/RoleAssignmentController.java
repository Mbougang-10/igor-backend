package com.yow.access.controllers;

import com.yow.access.config.security.context.AuthenticatedUserContext;
import com.yow.access.dto.RoleAssignmentRequest;
import com.yow.access.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
public class RoleAssignmentController {

    private final UserService userService;
    private final AuthenticatedUserContext userContext;

    public RoleAssignmentController(
            UserService userService,
            AuthenticatedUserContext userContext
    ) {
        this.userService = userService;
        this.userContext = userContext;
    }

    /* ============================
       ASSIGN ROLE (RBAC PROTECTED)
       ============================ */
    @PostMapping("/assign")
    public ResponseEntity<Void> assignRole(
            @Valid @RequestBody RoleAssignmentRequest request
    ) {
        UUID actorUserId = userContext.getUserId();

        userService.assignRole(
                actorUserId,
                request.getTargetUserId(),
                request.getRoleId(),
                request.getResourceId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /* ============================
       REMOVE ROLE (RBAC PROTECTED)
       ============================ */
    @PostMapping("/remove")
    public ResponseEntity<Void> removeRole(
            @Valid @RequestBody RoleAssignmentRequest request
    ) {
        UUID actorUserId = userContext.getUserId();

        userService.removeRole(
                actorUserId,
                request.getTargetUserId(),
                request.getRoleId(),
                request.getResourceId()
        );

        return ResponseEntity.noContent().build();
    }
}
