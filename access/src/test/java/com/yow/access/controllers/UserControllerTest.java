package com.yow.access.controllers;

import com.yow.access.config.security.context.AuthenticatedUserContext;
import com.yow.access.dto.AssignRoleRequest;
import com.yow.access.dto.CreateUserRequest;
import com.yow.access.entities.AppUser;
import com.yow.access.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserController
 * Adapté pour la nouvelle structure AppUser avec @Builder
 */
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    private UserController userController;
    private UserService userService;
    private AuthenticatedUserContext userContext;

    private final UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private final UUID actorUserId = UUID.fromString("223e4567-e89b-12d3-a456-426614174000");
    private final short roleId = 1;
    private final UUID resourceId = UUID.fromString("423e4567-e89b-12d3-a456-426614174000");
    private final Instant now = Instant.now();

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userContext = mock(AuthenticatedUserContext.class);
        userController = new UserController(userService, userContext);
    }

    @Nested
    @DisplayName("POST /api/users - Create User")
    class CreateUserTests {

        @Test
        @DisplayName("Devrait créer un utilisateur avec succès")
        void createUser_Success() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                    "testuser",
                    "test@example.com",
                    "hashedPassword123"
            );

            // Utilisation du builder pour créer l'utilisateur mocké
            AppUser expectedUser = AppUser.builder()
                    .id(userId)
                    .username("testuser")
                    .email("test@example.com")
                    .passwordHash("hashedPassword123")
                    .enabled(true)
                    .mustChangePassword(false)
                    .accountActivated(false)
                    .createdAt(now)
                    .build();

            when(userService.createUser("testuser", "test@example.com", "hashedPassword123"))
                    .thenReturn(expectedUser);

            // Act
            var response = userController.createUser(request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());

            AppUser createdUser = response.getBody();
            assertNotNull(createdUser);
            assertEquals(userId, createdUser.getId());
            assertEquals("testuser", createdUser.getUsername());
            assertEquals("test@example.com", createdUser.getEmail());
            assertEquals("hashedPassword123", createdUser.getPasswordHash());
            assertTrue(createdUser.isEnabled());
            assertFalse(createdUser.isMustChangePassword());
            assertFalse(createdUser.isAccountActivated());
            assertEquals(now, createdUser.getCreatedAt());

            verify(userService).createUser("testuser", "test@example.com", "hashedPassword123");
        }

        @Test
        @DisplayName("Devrait propager l'exception du service")
        void createUser_ServiceException() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                    "testuser",
                    "test@example.com",
                    "password123"
            );

            when(userService.createUser(any(), any(), any()))
                    .thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                userController.createUser(request);
            });
        }

        @Test
        @DisplayName("Devrait propager l'exception de conflit d'email")
        void createUser_EmailConflictException() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                    "existinguser",
                    "existing@example.com",
                    "password123"
            );

            when(userService.createUser(any(), any(), any()))
                    .thenThrow(new IllegalStateException("Email already exists"));

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                userController.createUser(request);
            });
            assertEquals("Email already exists", exception.getMessage());
        }

        @Test
        @DisplayName("Devrait propager l'exception de conflit de username")
        void createUser_UsernameConflictException() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                    "existinguser",
                    "new@example.com",
                    "password123"
            );

            when(userService.createUser(any(), any(), any()))
                    .thenThrow(new IllegalStateException("Username already exists"));

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                userController.createUser(request);
            });
            assertEquals("Username already exists", exception.getMessage());
        }

        @Test
        @DisplayName("Devrait créer un utilisateur avec createdBy si disponible dans le contexte")
        void createUser_WithCreatedByContext() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                    "newuser",
                    "new@example.com",
                    "hashedPassword123"
            );

            // Simuler un utilisateur créateur dans le contexte
            AppUser creator = AppUser.builder()
                    .id(actorUserId)
                    .username("admin")
                    .email("admin@example.com")
                    .build();

            AppUser expectedUser = AppUser.builder()
                    .id(userId)
                    .username("newuser")
                    .email("new@example.com")
                    .passwordHash("hashedPassword123")
                    .enabled(true)
                    .createdBy(creator)
                    .createdAt(now)
                    .build();

            when(userService.createUser("newuser", "new@example.com", "hashedPassword123"))
                    .thenReturn(expectedUser);

            // Act
            var response = userController.createUser(request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());

            AppUser createdUser = response.getBody();
            assertNotNull(createdUser);
            assertEquals(creator, createdUser.getCreatedBy());
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{userId}/enabled - Enable/Disable User")
    class SetUserEnabledTests {

        @Test
        @DisplayName("Devrait activer un utilisateur avec succès")
        void setUserEnabled_Enable_Success() {
            // Act
            var response = userController.setUserEnabled(userId, true);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(userService).setUserEnabled(userId, true);
        }

        @Test
        @DisplayName("Devrait désactiver un utilisateur avec succès")
        void setUserEnabled_Disable_Success() {
            // Act
            var response = userController.setUserEnabled(userId, false);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(userService).setUserEnabled(userId, false);
        }

        @Test
        @DisplayName("Devrait propager l'exception 'user not found'")
        void setUserEnabled_UserNotFound() {
            // Arrange
            doThrow(new IllegalArgumentException("User not found"))
                    .when(userService).setUserEnabled(userId, true);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userController.setUserEnabled(userId, true);
            });
            assertEquals("User not found", exception.getMessage());
        }

        @Test
        @DisplayName("Devrait propager l'exception de sécurité - pas autorisé à modifier")
        void setUserEnabled_Unauthorized() {
            // Arrange
            doThrow(new SecurityException("Not authorized to modify this user"))
                    .when(userService).setUserEnabled(userId, true);

            // Act & Assert
            SecurityException exception = assertThrows(SecurityException.class, () -> {
                userController.setUserEnabled(userId, true);
            });
            assertEquals("Not authorized to modify this user", exception.getMessage());
        }

        @Test
        @DisplayName("Devrait empêcher la désactivation de soi-même")
        void setUserEnabled_SelfDeactivation() {
            // Arrange - L'utilisateur essaie de se désactiver lui-même
            doThrow(new SecurityException("Cannot disable your own account"))
                    .when(userService).setUserEnabled(userId, false);

            // Act & Assert
            SecurityException exception = assertThrows(SecurityException.class, () -> {
                userController.setUserEnabled(userId, false);
            });
            assertEquals("Cannot disable your own account", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("POST /api/users/{userId}/roles - Assign Role")
    class AssignRoleTests {

        @Test
        @DisplayName("Devrait assigner un rôle avec succès")
        void assignRole_Success() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            // Act
            var response = userController.assignRole(userId, request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            verify(userService).assignRole(actorUserId, userId, roleId, resourceId);
        }

        @Test
        @DisplayName("Devrait utiliser le userId du contexte pour l'acteur")
        void assignRole_UsesContextUserId() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            UUID differentActorId = UUID.fromString("523e4567-e89b-12d3-a456-426614174000");
            when(userContext.getUserId()).thenReturn(differentActorId);

            // Act
            userController.assignRole(userId, request);

            // Assert
            verify(userService).assignRole(differentActorId, userId, roleId, resourceId);
        }

        @Test
        @DisplayName("Devrait propager l'exception de sécurité - pas autorisé à assigner le rôle")
        void assignRole_Unauthorized() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            doThrow(new SecurityException("Not authorized to assign this role"))
                    .when(userService).assignRole(actorUserId, userId, roleId, resourceId);

            // Act & Assert
            SecurityException exception = assertThrows(SecurityException.class, () -> {
                userController.assignRole(userId, request);
            });
            assertEquals("Not authorized to assign this role", exception.getMessage());
        }

        @Test
        @DisplayName("Devrait propager l'exception 'user not found'")
        void assignRole_UserNotFound() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            doThrow(new IllegalArgumentException("User not found"))
                    .when(userService).assignRole(actorUserId, userId, roleId, resourceId);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userController.assignRole(userId, request);
            });
            assertEquals("User not found", exception.getMessage());
        }

        @Test
        @DisplayName("Devrait propager l'exception 'resource not found'")
        void assignRole_ResourceNotFound() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            doThrow(new IllegalArgumentException("Resource not found"))
                    .when(userService).assignRole(actorUserId, userId, roleId, resourceId);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userController.assignRole(userId, request);
            });
            assertEquals("Resource not found", exception.getMessage());
        }

        @Test
        @DisplayName("Devrait propager l'exception de conflit - rôle déjà assigné")
        void assignRole_RoleAlreadyAssigned() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            doThrow(new IllegalStateException("Role already assigned to user on this resource"))
                    .when(userService).assignRole(actorUserId, userId, roleId, resourceId);

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                userController.assignRole(userId, request);
            });
            assertEquals("Role already assigned to user on this resource", exception.getMessage());
        }

        @Test
        @DisplayName("Devrait empêcher l'auto-assignation de rôles")
        void assignRole_SelfAssignmentPrevented() {
            // Arrange - L'utilisateur essaie de s'assigner un rôle à lui-même
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(userId); // Même ID que la cible

            doThrow(new SecurityException("Cannot assign roles to yourself"))
                    .when(userService).assignRole(userId, userId, roleId, resourceId);

            // Act & Assert
            SecurityException exception = assertThrows(SecurityException.class, () -> {
                userController.assignRole(userId, request);
            });
            assertEquals("Cannot assign roles to yourself", exception.getMessage());
        }

        @Test
        @DisplayName("Devrait empêcher l'assignation cross-tenant")
        void assignRole_CrossTenantPrevented() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            doThrow(new SecurityException("Cannot assign role across different tenants"))
                    .when(userService).assignRole(actorUserId, userId, roleId, resourceId);

            // Act & Assert
            SecurityException exception = assertThrows(SecurityException.class, () -> {
                userController.assignRole(userId, request);
            });
            assertEquals("Cannot assign role across different tenants", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Tests de cas limites et d'état")
    class EdgeCasesTests {

        @Test
        @DisplayName("Devrait gérer un utilisateur désactivé")
        void createUser_DisabledUser() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                    "disableduser",
                    "disabled@example.com",
                    "hashedPassword123"
            );

            AppUser expectedUser = AppUser.builder()
                    .id(userId)
                    .username("disableduser")
                    .email("disabled@example.com")
                    .passwordHash("hashedPassword123")
                    .enabled(false) // Utilisateur désactivé
                    .accountActivated(false)
                    .build();

            when(userService.createUser("disableduser", "disabled@example.com", "hashedPassword123"))
                    .thenReturn(expectedUser);

            // Act
            var response = userController.createUser(request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            AppUser createdUser = response.getBody();
            assertNotNull(createdUser);
            assertFalse(createdUser.isEnabled());
        }

        @Test
        @DisplayName("Devrait gérer un utilisateur avec mustChangePassword = true")
        void createUser_MustChangePassword() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                    "changepwduser",
                    "changepwd@example.com",
                    "temporaryPassword"
            );

            AppUser expectedUser = AppUser.builder()
                    .id(userId)
                    .username("changepwduser")
                    .email("changepwd@example.com")
                    .passwordHash("temporaryPassword")
                    .enabled(true)
                    .mustChangePassword(true) // Doit changer le mot de passe
                    .accountActivated(true)
                    .build();

            when(userService.createUser("changepwduser", "changepwd@example.com", "temporaryPassword"))
                    .thenReturn(expectedUser);

            // Act
            var response = userController.createUser(request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            AppUser createdUser = response.getBody();
            assertNotNull(createdUser);
            assertTrue(createdUser.isMustChangePassword());
        }

        @Test
        @DisplayName("Devrait gérer un utilisateur avec token d'activation")
        void createUser_WithActivationToken() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                    "pendinguser",
                    "pending@example.com",
                    "hashedPassword123"
            );

            Instant tokenExpiry = Instant.now().plusSeconds(3600); // Expire dans 1h
            AppUser expectedUser = AppUser.builder()
                    .id(userId)
                    .username("pendinguser")
                    .email("pending@example.com")
                    .passwordHash("hashedPassword123")
                    .enabled(false)
                    .activationToken("activation-token-123")
                    .activationTokenExpiry(tokenExpiry)
                    .accountActivated(false)
                    .build();

            when(userService.createUser("pendinguser", "pending@example.com", "hashedPassword123"))
                    .thenReturn(expectedUser);

            // Act
            var response = userController.createUser(request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            AppUser createdUser = response.getBody();
            assertNotNull(createdUser);
            assertEquals("activation-token-123", createdUser.getActivationToken());
            assertEquals(tokenExpiry, createdUser.getActivationTokenExpiry());
            assertFalse(createdUser.isAccountActivated());
        }
    }

    @Nested
    @DisplayName("Tests de null safety")
    class NullSafetyTests {

        @Test
        @DisplayName("Devrait rejeter une requête CreateUser nulle")
        void createUser_NullRequest() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> {
                userController.createUser(null);
            });
        }

        @Test
        @DisplayName("Devrait rejeter une requête AssignRole nulle")
        void assignRole_NullRequest() {
            // Arrange
            when(userContext.getUserId()).thenReturn(actorUserId);

            // Act & Assert
            assertThrows(NullPointerException.class, () -> {
                userController.assignRole(userId, null);
            });
        }

        @Test
        @DisplayName("Devrait gérer un contexte utilisateur null dans assignRole")
        void assignRole_NullUserContext() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(null);

            // Simule ce que fait VRAIMENT ton service
            doThrow(new IllegalArgumentException("Actor user ID is required"))
                    .when(userService).assignRole(null, userId, roleId, resourceId);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                userController.assignRole(userId, request);
            });
        }
    }

    @Nested
    @DisplayName("Tests de structure de réponse")
    class ResponseStructureTests {

        @Test
        @DisplayName("Devrait retourner un ResponseEntity avec le bon type")
        void createUser_ReturnsCorrectResponseEntity() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest(
                    "testuser",
                    "test@example.com",
                    "hashedPassword123"
            );

            AppUser expectedUser = AppUser.builder()
                    .id(userId)
                    .username("testuser")
                    .email("test@example.com")
                    .build();

            when(userService.createUser(any(), any(), any()))
                    .thenReturn(expectedUser);

            // Act
            var response = userController.createUser(request);

            // Assert
            assertNotNull(response);
            assertTrue(response instanceof ResponseEntity);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertTrue(response.hasBody());
        }

        @Test
        @DisplayName("Devrait retourner NO_CONTENT pour setUserEnabled")
        void setUserEnabled_ReturnsNoContent() {
            // Act
            var response = userController.setUserEnabled(userId, true);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertFalse(response.hasBody());
        }

        @Test
        @DisplayName("Devrait retourner CREATED pour assignRole")
        void assignRole_ReturnsCreated() {
            // Arrange
            AssignRoleRequest request = new AssignRoleRequest(roleId, resourceId);
            when(userContext.getUserId()).thenReturn(actorUserId);

            // Act
            var response = userController.assignRole(userId, request);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertFalse(response.hasBody());
        }
    }
}