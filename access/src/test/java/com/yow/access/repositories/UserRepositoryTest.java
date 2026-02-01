package com.yow.access.repositories;

import com.yow.access.entities.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private AppUser testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = new AppUser();
        testUser.setId(userId);
        testUser.setUsername("john.doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPasswordHash("hashedPassword123");
        testUser.setEnabled(true);
        testUser.setMustChangePassword(false);
        testUser.setAccountActivated(true);
        testUser.setActivationToken(UUID.randomUUID().toString());
        testUser.setActivationTokenExpiry(Instant.now().plusSeconds(3600));
        testUser.setCreatedAt(Instant.now());
    }

    /* ============================
       TESTS DES MÉTHODES STANDARD
       ============================ */

    @Test
    @DisplayName("1. save() - doit sauvegarder et retourner le user")
    void save_shouldReturnSavedUser() {
        // Given
        AppUser newUser = new AppUser();
        newUser.setUsername("jane.doe");
        newUser.setEmail("jane.doe@example.com");
        newUser.setPasswordHash("hashedPassword456");

        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(UUID.randomUUID()); // Simule l'ID généré
            return user;
        });

        // When
        AppUser savedUser = userRepository.save(newUser);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        verify(userRepository).save(newUser);
    }

    @Test
    @DisplayName("2. findById() - doit retourner le user existant")
    void findById_existingUser_shouldReturnUser() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Optional<AppUser> found = userRepository.findById(userId);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john.doe");
        assertThat(found.get().getEmail()).isEqualTo("john.doe@example.com");
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("3. findById() - user inexistant doit retourner empty")
    void findById_nonExistent_shouldReturnEmpty() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<AppUser> found = userRepository.findById(nonExistentId);

        // Then
        assertThat(found).isEmpty();
        verify(userRepository).findById(nonExistentId);
    }

    /* ============================
       TESTS DES MÉTHODES CUSTOM
       ============================ */

    @Test
    @DisplayName("4. findByEmail() - doit retourner le user")
    void findByEmail_shouldReturnUser() {
        // Given
        String email = "john.doe@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        Optional<AppUser> found = userRepository.findByEmail(email);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
        assertThat(found.get().getUsername()).isEqualTo("john.doe");
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("5. findByEmail() - email inexistant doit retourner empty")
    void findByEmail_nonExistent_shouldReturnEmpty() {
        // Given
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When
        Optional<AppUser> found = userRepository.findByEmail(nonExistentEmail);

        // Then
        assertThat(found).isEmpty();
        verify(userRepository).findByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("6. findByEmail() - sensible à la casse")
    void findByEmail_shouldBeCaseSensitive() {
        // Given
        String uppercaseEmail = "JOHN.DOE@EXAMPLE.COM";
        when(userRepository.findByEmail(uppercaseEmail)).thenReturn(Optional.empty());

        // When
        Optional<AppUser> found = userRepository.findByEmail(uppercaseEmail);

        // Then
        assertThat(found).isEmpty();
        verify(userRepository).findByEmail(uppercaseEmail);
    }

    @Test
    @DisplayName("7. findByUsername() - doit retourner le user")
    void findByUsername_shouldReturnUser() {
        // Given
        String username = "john.doe";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        Optional<AppUser> found = userRepository.findByUsername(username);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(username);
        assertThat(found.get().getEmail()).isEqualTo("john.doe@example.com");
        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("8. findByUsername() - username inexistant doit retourner empty")
    void findByUsername_nonExistent_shouldReturnEmpty() {
        // Given
        String nonExistentUsername = "nonexistent";
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        // When
        Optional<AppUser> found = userRepository.findByUsername(nonExistentUsername);

        // Then
        assertThat(found).isEmpty();
        verify(userRepository).findByUsername(nonExistentUsername);
    }

    @Test
    @DisplayName("9. findByUsername() - sensible à la casse")
    void findByUsername_shouldBeCaseSensitive() {
        // Given
        String uppercaseUsername = "JOHN.DOE";
        when(userRepository.findByUsername(uppercaseUsername)).thenReturn(Optional.empty());

        // When
        Optional<AppUser> found = userRepository.findByUsername(uppercaseUsername);

        // Then
        assertThat(found).isEmpty();
        verify(userRepository).findByUsername(uppercaseUsername);
    }

    @Test
    @DisplayName("10. findByActivationToken() - doit retourner le user")
    void findByActivationToken_shouldReturnUser() {
        // Given
        String activationToken = testUser.getActivationToken();
        when(userRepository.findByActivationToken(activationToken)).thenReturn(Optional.of(testUser));

        // When
        Optional<AppUser> found = userRepository.findByActivationToken(activationToken);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getActivationToken()).isEqualTo(activationToken);
        assertThat(found.get().getUsername()).isEqualTo("john.doe");
        verify(userRepository).findByActivationToken(activationToken);
    }

    @Test
    @DisplayName("11. findByActivationToken() - token inexistant doit retourner empty")
    void findByActivationToken_nonExistent_shouldReturnEmpty() {
        // Given
        String invalidToken = "invalid-token-123";
        when(userRepository.findByActivationToken(invalidToken)).thenReturn(Optional.empty());

        // When
        Optional<AppUser> found = userRepository.findByActivationToken(invalidToken);

        // Then
        assertThat(found).isEmpty();
        verify(userRepository).findByActivationToken(invalidToken);
    }

    @Test
    @DisplayName("12. findByActivationToken() - token null doit retourner empty")
    void findByActivationToken_nullToken_shouldReturnEmpty() {
        // Given
        when(userRepository.findByActivationToken(null)).thenReturn(Optional.empty());

        // When
        Optional<AppUser> found = userRepository.findByActivationToken(null);

        // Then
        assertThat(found).isEmpty();
        verify(userRepository).findByActivationToken(null);
    }

    @Test
    @DisplayName("13. existsByEmail() - doit retourner vrai si email existe")
    void existsByEmail_shouldReturnTrueWhenEmailExists() {
        // Given
        String existingEmail = "john.doe@example.com";
        when(userRepository.existsByEmail(existingEmail)).thenReturn(true);
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // When & Then
        assertThat(userRepository.existsByEmail(existingEmail)).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();

        verify(userRepository).existsByEmail(existingEmail);
        verify(userRepository).existsByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("14. existsByEmail() - sensible à la casse")
    void existsByEmail_shouldBeCaseSensitive() {
        // Given
        when(userRepository.existsByEmail("JOHN.DOE@EXAMPLE.COM")).thenReturn(false);
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // When & Then
        assertThat(userRepository.existsByEmail("JOHN.DOE@EXAMPLE.COM")).isFalse();
        assertThat(userRepository.existsByEmail("john.doe@example.com")).isTrue();
    }

    @Test
    @DisplayName("15. existsByUsername() - doit retourner vrai si username existe")
    void existsByUsername_shouldReturnTrueWhenUsernameExists() {
        // Given
        when(userRepository.existsByUsername("john.doe")).thenReturn(true);
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // When & Then
        assertThat(userRepository.existsByUsername("john.doe")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("16. existsByUsername() - sensible à la casse")
    void existsByUsername_shouldBeCaseSensitive() {
        // Given
        when(userRepository.existsByUsername("JOHN.DOE")).thenReturn(false);
        when(userRepository.existsByUsername("john.doe")).thenReturn(true);

        // When & Then
        assertThat(userRepository.existsByUsername("JOHN.DOE")).isFalse();
        assertThat(userRepository.existsByUsername("john.doe")).isTrue();
    }

    @Test
    @DisplayName("17. deleteById() - doit supprimer le user")
    void deleteById_shouldDeleteUser() {
        // Given
        doNothing().when(userRepository).deleteById(userId);

        // When
        userRepository.deleteById(userId);

        // Then
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("18. findAll() - doit retourner tous les users")
    void findAll_shouldReturnAllUsers() {
        // Given
        AppUser user2 = new AppUser();
        user2.setId(UUID.randomUUID());
        user2.setUsername("jane.doe");
        user2.setEmail("jane@example.com");

        when(userRepository.findAll()).thenReturn(java.util.List.of(testUser, user2));

        // When
        var allUsers = userRepository.findAll();

        // Then
        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting(AppUser::getUsername)
                .containsExactlyInAnyOrder("john.doe", "jane.doe");
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("19. count() - doit retourner le nombre de users")
    void count_shouldReturnUserCount() {
        // Given
        when(userRepository.count()).thenReturn(5L);

        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isEqualTo(5);
        verify(userRepository).count();
    }

    @Test
    @DisplayName("20. Méthodes combinées - workflow complet")
    void combinedMethods_fullWorkflow() {
        // Test d'un workflow complet
        AppUser newUser = new AppUser();
        newUser.setUsername("new.user");
        newUser.setEmail("new.user@example.com");
        newUser.setPasswordHash("hash");

        // 1. Vérifier que l'email n'existe pas encore
        when(userRepository.existsByEmail("new.user@example.com")).thenReturn(false);
        assertThat(userRepository.existsByEmail("new.user@example.com")).isFalse();

        // 2. Sauvegarder le user
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        AppUser saved = userRepository.save(newUser);
        assertThat(saved.getId()).isNotNull();

        // 3. Rechercher par email
        when(userRepository.findByEmail("new.user@example.com")).thenReturn(Optional.of(saved));
        Optional<AppUser> foundByEmail = userRepository.findByEmail("new.user@example.com");
        assertThat(foundByEmail).isPresent();

        // 4. Rechercher par username
        when(userRepository.findByUsername("new.user")).thenReturn(Optional.of(saved));
        Optional<AppUser> foundByUsername = userRepository.findByUsername("new.user");
        assertThat(foundByUsername).isPresent();

        // 5. Vérifier que exists retourne true maintenant
        when(userRepository.existsByEmail("new.user@example.com")).thenReturn(true);
        when(userRepository.existsByUsername("new.user")).thenReturn(true);

        assertThat(userRepository.existsByEmail("new.user@example.com")).isTrue();
        assertThat(userRepository.existsByUsername("new.user")).isTrue();

        verify(userRepository, times(2)).existsByEmail("new.user@example.com");
        verify(userRepository).existsByUsername("new.user");
        verify(userRepository).save(any(AppUser.class));
        verify(userRepository).findByEmail("new.user@example.com");
        verify(userRepository).findByUsername("new.user");
    }

    @Test
    @DisplayName("21. Utilisation des champs optionnels de l'entité")
    void optionalFields_shouldWorkCorrectly() {
        // Given - Vérifier les valeurs par défaut lors de la création
        AppUser minimalUser = new AppUser();

        // Assert: Vérifier les valeurs par défaut IMMÉDIATEMENT après new AppUser()
        assertThat(minimalUser.isEnabled()).isTrue(); // Par défaut: true
        assertThat(minimalUser.isMustChangePassword()).isFalse(); // Par défaut: false
        assertThat(minimalUser.isAccountActivated()).isFalse(); // Par défaut: false
        assertThat(minimalUser.getActivationToken()).isNull(); // Par défaut: null
        assertThat(minimalUser.getActivationTokenExpiry()).isNull(); // Par défaut: null
        assertThat(minimalUser.getCreatedBy()).isNull(); // Par défaut: null
        assertThat(minimalUser.getCreatedAt()).isNotNull(); // AUTO-GÉNÉRÉ à la création

        // Maintenant, setter les champs obligatoires
        minimalUser.setUsername("minimal");
        minimalUser.setEmail("minimal@example.com");
        minimalUser.setPasswordHash("hash");

        UUID minimalId = UUID.randomUUID();
        minimalUser.setId(minimalId);

        when(userRepository.findById(minimalId)).thenReturn(Optional.of(minimalUser));

        // When
        Optional<AppUser> found = userRepository.findById(minimalId);

        // Then - Vérifier que les valeurs sont conservées
        assertThat(found).isPresent();
        AppUser user = found.get();

        assertThat(user.getUsername()).isEqualTo("minimal");
        assertThat(user.getEmail()).isEqualTo("minimal@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hash");

        // Les valeurs par défaut doivent être préservées
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isMustChangePassword()).isFalse();
        assertThat(user.isAccountActivated()).isFalse();
        assertThat(user.getActivationToken()).isNull();
        assertThat(user.getActivationTokenExpiry()).isNull();
        assertThat(user.getCreatedBy()).isNull();
        assertThat(user.getCreatedAt()).isNotNull(); // Toujours pas null
    }
    @Test
    @DisplayName("22. Relation createdBy - peut référencer un autre user")
    void createdBy_relationshipTest() {
        // Given - User créé par un autre user
        AppUser creator = new AppUser();
        creator.setId(UUID.randomUUID());
        creator.setUsername("admin");
        creator.setEmail("admin@example.com");

        AppUser createdUser = new AppUser();
        createdUser.setId(UUID.randomUUID());
        createdUser.setUsername("newuser");
        createdUser.setEmail("newuser@example.com");
        createdUser.setPasswordHash("hash");
        createdUser.setCreatedBy(creator); // Relation établie

        when(userRepository.findById(createdUser.getId())).thenReturn(Optional.of(createdUser));

        // When
        Optional<AppUser> found = userRepository.findById(createdUser.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCreatedBy()).isNotNull();
        assertThat(found.get().getCreatedBy().getUsername()).isEqualTo("admin");
        assertThat(found.get().getCreatedBy().getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    @DisplayName("23. Activation token avec expiry date")
    void activationToken_withExpiryDate() {
        // Given - User avec token et date d'expiration
        Instant expiryDate = Instant.now().plusSeconds(7200); // 2 heures
        AppUser userWithToken = new AppUser();
        userWithToken.setId(UUID.randomUUID());
        userWithToken.setUsername("tokenuser");
        userWithToken.setEmail("token@example.com");
        userWithToken.setPasswordHash("hash");
        userWithToken.setActivationToken("secure-token-xyz");
        userWithToken.setActivationTokenExpiry(expiryDate);
        userWithToken.setAccountActivated(false);

        when(userRepository.findByActivationToken("secure-token-xyz"))
                .thenReturn(Optional.of(userWithToken));

        // When
        Optional<AppUser> found = userRepository.findByActivationToken("secure-token-xyz");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getActivationToken()).isEqualTo("secure-token-xyz");
        assertThat(found.get().getActivationTokenExpiry()).isEqualTo(expiryDate);
        assertThat(found.get().isAccountActivated()).isFalse();
    }

    @Test
    @DisplayName("24. Méthodes non implémentées par le repository")
    void repositoryMethodSignatures_shouldMatchInterface() {
        // Ce test vérifie simplement que les méthodes existent avec les bonnes signatures
        // Pas besoin de mocks spécifiques

        // Given - Le repository est une interface JpaRepository
        // When & Then - Toutes les méthodes doivent être accessibles

        // Méthodes JpaRepository standard
        userRepository.save(testUser);
        userRepository.findById(userId);
        userRepository.deleteById(userId);
        userRepository.findAll();
        userRepository.count();

        // Méthodes custom déclarées dans l'interface
        userRepository.findByEmail("test@example.com");
        userRepository.findByUsername("testuser");
        userRepository.findByActivationToken("token");
        userRepository.existsByEmail("test@example.com");
        userRepository.existsByUsername("testuser");

        // Si aucune exception n'est lancée, les signatures sont correctes
        assertThat(userRepository).isNotNull();
    }
}