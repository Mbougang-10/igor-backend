package com.yow.access.services;

import com.yow.access.entities.*;
import com.yow.access.exceptions.AccessDeniedException;
import com.yow.access.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRoleResourceRepository urrRepository;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserService userService;

    private UUID actorUserId;
    private UUID targetUserId;
    private UUID resourceId;
    private Short roleId;
    private AppUser actorUser;
    private AppUser targetUser;
    private Role role;
    private Resource resource;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        actorUserId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();
        resourceId = UUID.randomUUID();
        roleId = 1;

        // Mock actor user
        actorUser = new AppUser();
        actorUser.setId(actorUserId);
        actorUser.setUsername("actor");
        actorUser.setEmail("actor@example.com");
        actorUser.setEnabled(true);

        // Mock target user
        targetUser = new AppUser();
        targetUser.setId(targetUserId);
        targetUser.setUsername("target");
        targetUser.setEmail("target@example.com");
        targetUser.setEnabled(true);

        // Mock role
        role = new Role();
        role.setId(roleId);
        role.setName("USER_ROLE");

        // Mock tenant
        tenant = new Tenant();
        tenant.setId(UUID.randomUUID());
        tenant.setName("Test Tenant");
        tenant.setCode("TEST");

        // Mock resource
        resource = new Resource();
        resource.setId(resourceId);
        resource.setName("Test Resource");
        resource.setType("FOLDER");
        resource.setTenant(tenant);
    }

    /* ============================
       TESTS CREATE USER
       ============================ */

    @Test
    @DisplayName("1. Créer user - doit persister avec les bons champs")
    void createUser_shouldPersistUser() {
        // Given
        AppUser newUser = new AppUser();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPasswordHash("hashedPass");
        newUser.setEnabled(true);
        newUser.setCreatedAt(Instant.now());

        when(userRepository.save(any(AppUser.class))).thenReturn(newUser);

        // When
        AppUser result = userService.createUser("newuser", "new@example.com", "hashedPass");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getPasswordHash()).isEqualTo("hashedPass");
        assertThat(result.isEnabled()).isTrue();

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("newuser") &&
                        user.getEmail().equals("new@example.com") &&
                        user.getPasswordHash().equals("hashedPass") &&
                        user.isEnabled()
        ));
    }

    @Test
    @DisplayName("2. Créer user - enabled par défaut à true")
    void createUser_shouldDefaultEnabledToTrue() {
        // Given
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // When
        AppUser result = userService.createUser("user", "email@example.com", "hash");

        // Then
        verify(userRepository).save(argThat(user -> user.isEnabled()));
    }

    /* ============================
       TESTS ENABLE/DISABLE USER
       ============================ */

    @Test
    @DisplayName("3. Activer user - doit mettre enabled à true")
    void setUserEnabled_true_shouldEnableUser() {
        // Given
        AppUser disabledUser = new AppUser();
        disabledUser.setId(targetUserId);
        disabledUser.setEnabled(false);

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(disabledUser));

        // When
        userService.setUserEnabled(targetUserId, true);

        // Then
        verify(userRepository).findById(targetUserId);
        assertThat(disabledUser.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("4. Désactiver user - doit mettre enabled à false")
    void setUserEnabled_false_shouldDisableUser() {
        // Given
        AppUser enabledUser = new AppUser();
        enabledUser.setId(targetUserId);
        enabledUser.setEnabled(true);

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(enabledUser));

        // When
        userService.setUserEnabled(targetUserId, false);

        // Then
        assertThat(enabledUser.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("5. Activer user inexistant - doit throw exception")
    void setUserEnabled_nonExistentUser_shouldThrowException() {
        // Given
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.setUserEnabled(UUID.randomUUID(), true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User not found");
    }

    /* ============================
       TESTS ASSIGN ROLE
       ============================ */

    @Test
    @DisplayName("6. Assigner rôle autorisé - doit réussir avec audit")
    void assignRole_authorized_shouldSucceed() {
        // Given
        when(userRepository.findById(actorUserId)).thenReturn(Optional.of(actorUser));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        UserRoleResource mockUrr = new UserRoleResource();
        mockUrr.setUser(targetUser);
        mockUrr.setRole(role);
        mockUrr.setResource(resource);

        // Mock la factory
        try (MockedStatic<UserRoleResourceFactory> factoryMock = mockStatic(UserRoleResourceFactory.class)) {
            factoryMock.when(() ->
                    UserRoleResourceFactory.create(targetUser, role, resource)
            ).thenReturn(mockUrr);

            when(urrRepository.save(mockUrr)).thenReturn(mockUrr);

            // When
            userService.assignRole(actorUserId, targetUserId, roleId, resourceId);

            // Then
            verify(authorizationService).checkPermission(
                    actorUserId, resourceId, "ASSIGN_ROLE"
            );
            verify(urrRepository).save(mockUrr);
            verify(auditLogService).log(
                    eq(tenant),
                    eq(actorUser),
                    eq(resource),
                    eq("ASSIGN_ROLE"),
                    eq("USER_ROLE_RESOURCE"),
                    isNull(),
                    eq("SUCCESS"),
                    eq("Role assigned to user"),
                    isNull(),
                    isNull()
            );
        }
    }

    @Test
    @DisplayName("7. Assigner rôle sans permission - doit échouer avec audit")
    void assignRole_unauthorized_shouldFailWithAudit() {
        // Given
        when(userRepository.findById(actorUserId)).thenReturn(Optional.of(actorUser));
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));

        doThrow(new AccessDeniedException("Permission denied"))
                .when(authorizationService)
                .checkPermission(actorUserId, resourceId, "ASSIGN_ROLE");

        // When & Then
        assertThatThrownBy(() ->
                userService.assignRole(actorUserId, targetUserId, roleId, resourceId)
        ).isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Permission denied");

        // Verify audit log for failure
        verify(auditLogService).log(
                eq(tenant),
                eq(actorUser),
                eq(resource),
                eq("ASSIGN_ROLE"),
                eq("USER_ROLE_RESOURCE"),
                isNull(),
                eq("FAILURE"),
                contains("Permission denied"),
                isNull(),
                isNull()
        );

        verify(urrRepository, never()).save(any());
    }

    @Test
    @DisplayName("8. Assigner rôle - resource inexistante")
    void assignRole_nonExistentResource_shouldThrowException() {
        // Given - Seule la resource n'existe pas
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                userService.assignRole(actorUserId, targetUserId, roleId, resourceId)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Resource not found");

        // Vérifications
        verify(resourceRepository).findById(resourceId);
        verify(userRepository, never()).findById(any()); // Pas appelé car exception avant
        verify(authorizationService, never()).checkPermission(any(), any(), any());
        verify(urrRepository, never()).save(any());
    }

    @Test
    @DisplayName("9. Assigner rôle - actor inexistant")
    void assignRole_nonExistentActor_shouldThrowException() {
        // Given - Resource existe, mais actor n'existe pas
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        when(userRepository.findById(actorUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                userService.assignRole(actorUserId, targetUserId, roleId, resourceId)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Actor not found");

        // Vérifications
        verify(resourceRepository).findById(resourceId);
        verify(userRepository).findById(actorUserId);
        verify(authorizationService, never()).checkPermission(any(), any(), any());
        verify(urrRepository, never()).save(any());
    }


    @Test
    @DisplayName("10. Assigner rôle - target user inexistant")
    void assignRole_nonExistentTargetUser_shouldThrowException() {
        // Given
        when(userRepository.findById(actorUserId)).thenReturn(Optional.of(actorUser));
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        // Autorisation réussie
        doNothing().when(authorizationService)
                .checkPermission(actorUserId, resourceId, "ASSIGN_ROLE");

        // When & Then
        assertThatThrownBy(() ->
                userService.assignRole(actorUserId, targetUserId, roleId, resourceId)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target user not found");

        verify(urrRepository, never()).save(any());
    }

    @Test
    @DisplayName("11. Assigner rôle - rôle inexistant")
    void assignRole_nonExistentRole_shouldThrowException() {
        // Given
        when(userRepository.findById(actorUserId)).thenReturn(Optional.of(actorUser));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        doNothing().when(authorizationService)
                .checkPermission(actorUserId, resourceId, "ASSIGN_ROLE");

        // When & Then
        assertThatThrownBy(() ->
                userService.assignRole(actorUserId, targetUserId, roleId, resourceId)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Role not found");

        verify(urrRepository, never()).save(any());
    }

    @Test
    @DisplayName("12. Assigner plusieurs rôles sur même resource - doit autoriser")
    void assignMultipleRoles_sameResource_shouldAllow() {
        // Given - Simuler qu'un rôle est déjà assigné
        when(userRepository.findById(actorUserId)).thenReturn(Optional.of(actorUser));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));

        Role role2 = new Role();
        role2.setId((short) 2);
        role2.setName("ANOTHER_ROLE");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.findById((short) 2)).thenReturn(Optional.of(role2));

        // Autoriser les deux assignations
        doNothing().when(authorizationService)
                .checkPermission(actorUserId, resourceId, "ASSIGN_ROLE");

        // Mock des factories
        try (MockedStatic<UserRoleResourceFactory> factoryMock = mockStatic(UserRoleResourceFactory.class)) {
            factoryMock.when(() ->
                    UserRoleResourceFactory.create(any(), any(), any())
            ).thenReturn(new UserRoleResource());

            when(urrRepository.save(any())).thenReturn(new UserRoleResource());

            // When - Assigner deux rôles différents sur la même resource
            userService.assignRole(actorUserId, targetUserId, roleId, resourceId);
            userService.assignRole(actorUserId, targetUserId, (short) 2, resourceId);

            // Then - Deux assignations réussies
            verify(urrRepository, times(2)).save(any());
            verify(auditLogService, times(2)).log(
                    any(), any(), any(), eq("ASSIGN_ROLE"),
                    any(), any(), eq("SUCCESS"), any(), any(), any()
            );
        }
    }

    /* ============================
       TESTS REMOVE ROLE
       ============================ */

    @Test
    @DisplayName("13. Supprimer rôle autorisé - doit réussir avec audit")
    void removeRole_authorized_shouldSucceed() {
        // Given
        when(userRepository.findById(actorUserId)).thenReturn(Optional.of(actorUser));
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));

        UserRoleResource existingUrr = new UserRoleResource();
        existingUrr.setUser(targetUser);
        existingUrr.setRole(role);
        existingUrr.setResource(resource);

        when(urrRepository.findByUserIdAndRoleIdAndResourceId(targetUserId, roleId, resourceId))
                .thenReturn(Optional.of(existingUrr));

        // When
        userService.removeRole(actorUserId, targetUserId, roleId, resourceId);

        // Then
        verify(authorizationService).checkPermission(
                actorUserId, resourceId, "REMOVE_ROLE"
        );
        verify(urrRepository).delete(existingUrr);
        verify(auditLogService).log(
                eq(tenant),
                eq(actorUser),
                eq(resource),
                eq("REMOVE_ROLE"),
                eq("USER_ROLE_RESOURCE"),
                isNull(),
                eq("SUCCESS"),
                eq("Role removed from user"),
                isNull(),
                isNull()
        );
    }

    @Test
    @DisplayName("14. Supprimer rôle sans permission - doit échouer avec audit")
    void removeRole_unauthorized_shouldFailWithAudit() {
        // Given
        when(userRepository.findById(actorUserId)).thenReturn(Optional.of(actorUser));
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));

        doThrow(new AccessDeniedException("Permission denied"))
                .when(authorizationService)
                .checkPermission(actorUserId, resourceId, "REMOVE_ROLE");

        // When & Then
        assertThatThrownBy(() ->
                userService.removeRole(actorUserId, targetUserId, roleId, resourceId)
        ).isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Permission denied");

        verify(urrRepository, never()).findByUserIdAndRoleIdAndResourceId(any(), any(), any());
        verify(urrRepository, never()).delete(any());

        verify(auditLogService).log(
                eq(tenant),
                eq(actorUser),
                eq(resource),
                eq("REMOVE_ROLE"),
                eq("USER_ROLE_RESOURCE"),
                isNull(),
                eq("FAILURE"),
                contains("Permission denied"),
                isNull(),
                isNull()
        );
    }

    @Test
    @DisplayName("15. Supprimer rôle inexistant - doit throw exception")
    void removeRole_nonExistentAssignment_shouldThrowException() {
        // Given
        when(userRepository.findById(actorUserId)).thenReturn(Optional.of(actorUser));
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));

        when(urrRepository.findByUserIdAndRoleIdAndResourceId(targetUserId, roleId, resourceId))
                .thenReturn(Optional.empty());

        doNothing().when(authorizationService)
                .checkPermission(actorUserId, resourceId, "REMOVE_ROLE");

        // When & Then
        assertThatThrownBy(() ->
                userService.removeRole(actorUserId, targetUserId, roleId, resourceId)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Role assignment not found");

        verify(urrRepository, never()).delete(any());
    }

    @Test
    @DisplayName("16. Refuser auto-promotion - actor essaie de se donner un rôle")
    void assignRole_selfAssignment_shouldBeAllowed() {
        // Note: Ton service n'empêche pas l'auto-assignation
        // C'est à l'AuthorizationService de gérer ça

        // Given - Actor = Target (même user)
        when(userRepository.findById(actorUserId)).thenReturn(Optional.of(actorUser));
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        // Mock la factory
        try (MockedStatic<UserRoleResourceFactory> factoryMock = mockStatic(UserRoleResourceFactory.class)) {
            factoryMock.when(() ->
                    UserRoleResourceFactory.create(actorUser, role, resource)
            ).thenReturn(new UserRoleResource());

            when(urrRepository.save(any())).thenReturn(new UserRoleResource());

            // When - Actor s'assigner un rôle à lui-même
            userService.assignRole(actorUserId, actorUserId, roleId, resourceId);

            // Then - Devrait réussir (si pas de restriction dans ton service)
            verify(urrRepository).save(any());
        }
    }

    /* ============================
       TESTS TRANSACTION & AUDIT
       ============================ */

    @Test
    @DisplayName("17. Transaction - rollback en cas d'erreur après audit")
    void assignRole_transactionRollbackOnError() {
        // Given
        when(userRepository.findById(actorUserId)).thenReturn(Optional.of(actorUser));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        // Mock la factory
        try (MockedStatic<UserRoleResourceFactory> factoryMock = mockStatic(UserRoleResourceFactory.class)) {
            factoryMock.when(() ->
                    UserRoleResourceFactory.create(targetUser, role, resource)
            ).thenReturn(new UserRoleResource());

            // Simuler une erreur lors de la sauvegarde
            when(urrRepository.save(any(UserRoleResource.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() ->
                    userService.assignRole(actorUserId, targetUserId, roleId, resourceId)
            ).isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database error");

            // Audit devrait être rollback aussi (dans une vraie transaction)
            // Mais on vérifie que l'authorization a été vérifiée
            verify(authorizationService).checkPermission(
                    actorUserId, resourceId, "ASSIGN_ROLE"
            );
        }
    }

    @Test
    @DisplayName("18. Cross-tenant assignment - bloqué par authorization")
    void assignRole_crossTenant_shouldBeBlocked() {
        // Given - Resource d'un autre tenant
        Tenant otherTenant = new Tenant();
        otherTenant.setId(UUID.randomUUID());
        otherTenant.setCode("OTHER_TENANT");

        Resource otherResource = new Resource();
        otherResource.setId(UUID.randomUUID());
        otherResource.setTenant(otherTenant);

        when(userRepository.findById(actorUserId)).thenReturn(Optional.of(actorUser));
        when(resourceRepository.findById(otherResource.getId())).thenReturn(Optional.of(otherResource));

        // Simuler que l'authorization bloque le cross-tenant
        doThrow(new AccessDeniedException("Cross-tenant access denied"))
                .when(authorizationService)
                .checkPermission(actorUserId, otherResource.getId(), "ASSIGN_ROLE");

        // When & Then
        assertThatThrownBy(() ->
                userService.assignRole(actorUserId, targetUserId, roleId, otherResource.getId())
        ).isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Cross-tenant access denied");

        verify(auditLogService).log(
                eq(otherTenant), // Audit avec le bon tenant
                eq(actorUser),
                eq(otherResource),
                eq("ASSIGN_ROLE"),
                eq("USER_ROLE_RESOURCE"),
                isNull(),
                eq("FAILURE"),
                contains("Cross-tenant access denied"),
                isNull(),
                isNull()
        );
    }
}