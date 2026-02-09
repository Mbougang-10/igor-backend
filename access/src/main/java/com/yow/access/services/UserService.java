package com.yow.access.services;

import com.yow.access.entities.*;
import com.yow.access.exceptions.AccessDeniedException;
import com.yow.access.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ResourceRepository resourceRepository;
    private final UserRoleResourceRepository urrRepository;
    private final AuthorizationService authorizationService;
    private final AuditLogService auditLogService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            ResourceRepository resourceRepository,
            UserRoleResourceRepository urrRepository,
            AuthorizationService authorizationService,
            AuditLogService auditLogService,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.resourceRepository = resourceRepository;
        this.urrRepository = urrRepository;
        this.authorizationService = authorizationService;
        this.auditLogService = auditLogService;
        this.passwordEncoder = passwordEncoder;
    }

    /* ============================
       CREATE USER
       ============================ */
    /* ============================
       CREATE USER (DIRECT ACTIVE)
       ============================ */
    @Transactional
    public AppUser createUser(
            String username,
            String email,
            String rawPassword
    ) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        user.setAccountActivated(true); // Direct activation

        return userRepository.save(user);
    }

    /* ============================
       ENABLE / DISABLE USER
       ============================ */
    @Transactional
    public void setUserEnabled(UUID userId, boolean enabled) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        user.setEnabled(enabled);
    }

    /* ============================
       ASSIGN ROLE (RBAC + AUDIT)
       ============================ */
    @Transactional
    public void assignRole(
            UUID actorUserId,
            UUID targetUserId,
            Short roleId,
            UUID resourceId
    ) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalStateException("Resource not found"));

        AppUser actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new IllegalStateException("Actor not found"));

        try {
            authorizationService.checkPermission(
                    actorUserId,
                    resourceId,
                    "ASSIGN_ROLE"
            );

            AppUser targetUser = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new IllegalStateException("Target user not found"));

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalStateException("Role not found"));

            UserRoleResource urr =
                    UserRoleResourceFactory.create(
                            targetUser,
                            role,
                            resource
                    );

            urrRepository.save(urr);

            auditLogService.log(
                    resource.getTenant(),
                    actor,
                    resource,
                    "ASSIGN_ROLE",
                    "USER_ROLE_RESOURCE",
                    null,
                    "SUCCESS",
                    "Role assigned to user",
                    null,
                    null
            );

        } catch (AccessDeniedException ex) {

            auditLogService.log(
                    resource.getTenant(),
                    actor,
                    resource,
                    "ASSIGN_ROLE",
                    "USER_ROLE_RESOURCE",
                    null,
                    "FAILURE",
                    ex.getMessage(),
                    null,
                    null
            );

            throw ex;
        }
    }

    /* ============================
       REMOVE ROLE (RBAC + AUDIT)
       ============================ */
    @Transactional
    public void removeRole(
            UUID actorUserId,
            UUID targetUserId,
            Short roleId,
            UUID resourceId
    ) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalStateException("Resource not found"));

        AppUser actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new IllegalStateException("Actor not found"));

        try {
            authorizationService.checkPermission(
                    actorUserId,
                    resourceId,
                    "REMOVE_ROLE"
            );

            UserRoleResource urr =
                    urrRepository.findByUserIdAndRoleIdAndResourceId(
                                    targetUserId,
                                    roleId,
                                    resourceId
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException("Role assignment not found")
                            );

            urrRepository.delete(urr);

            auditLogService.log(
                    resource.getTenant(),
                    actor,
                    resource,
                    "REMOVE_ROLE",
                    "USER_ROLE_RESOURCE",
                    null,
                    "SUCCESS",
                    "Role removed from user",
                    null,
                    null
            );

        } catch (AccessDeniedException ex) {

            auditLogService.log(
                    resource.getTenant(),
                    actor,
                    resource,
                    "REMOVE_ROLE",
                    "USER_ROLE_RESOURCE",
                    null,
                    "FAILURE",
                    ex.getMessage(),
                    null,
                    null
            );

            throw ex;
        }
    }

    /* ============================
       GET USERS BY TENANT
       ============================ */
    @Transactional(readOnly = true)
    public java.util.List<AppUser> getUsersByTenant(UUID tenantId) {
        // Cette requête pourrait être optimisée dans le repository
        // Pour l'instant on réutilise urrRepository pour trouver les userIds
        // puis on fetch les users.
        
        // Ou mieux, on ajoute une méthode custom dans UserRepository/URRRepository
        return urrRepository.findUsersByTenantId(tenantId);
    }
    @Transactional(readOnly = true)
    public java.util.Optional<AppUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public java.util.List<com.yow.access.dto.UserRoleDTO> getUserRoles(UUID userId) {
        return urrRepository.findAllByUserIdWithResourceAndTenant(userId).stream()
                .map(urr -> new com.yow.access.dto.UserRoleDTO(
                        urr.getRole().getId(),
                        urr.getRole().getName(),
                        urr.getResource().getId(),
                        urr.getResource().getName(),
                        urr.getResource().getType()
                ))
                .collect(java.util.stream.Collectors.toList());
    }
}
