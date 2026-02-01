package com.yow.access.services;

import com.yow.access.entities.*;
import com.yow.access.exceptions.TenantAlreadyExistsException;
import com.yow.access.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleResourceRepository urrRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private TenantService tenantService;

    private UUID creatorUserId;
    private AppUser creatorUser;
    private Tenant tenant;
    private UUID tenantId;
    private Resource rootResource;
    private Role tenantAdminRole;


    @BeforeEach
    void setUp() {
        creatorUserId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        tenantAdminRole = new Role();

        // Générer un short aléatoire (entre 1 et 1000 par exemple)
        SecureRandom random = new SecureRandom();
        short randomShort = (short) (random.nextInt(1000) + 1); // 1-1000


        // Mock creator user
        creatorUser = new AppUser();
        creatorUser.setId(creatorUserId);
        creatorUser.setEmail("creator@example.com");
        creatorUser.setUsername("creator");

        // Mock tenant
        tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setName("Test Tenant");
        tenant.setCode("TENANT_TEST");
        tenant.setStatus("ACTIVE");
        tenant.setCreatedAt(Instant.now());

        // Mock root resource
        rootResource = new Resource();
        rootResource.setId(UUID.randomUUID());
        rootResource.setName("Test Tenant Root");
        rootResource.setType("ROOT");
        rootResource.setTenant(tenant);

        // Mock tenant admin role
        tenantAdminRole = new Role();
        tenantAdminRole.setId(randomShort);
        tenantAdminRole.setName("TENANT_ADMIN");
    }

    /* ============================
       TESTS CREATE TENANT
       ============================ */

    @Test
    @DisplayName("1. Créer tenant → crée resource racine")
    void createTenant_shouldCreateTenantAndRootResource() {
        // Given
        when(userRepository.findById(creatorUserId)).thenReturn(Optional.of(creatorUser));
        when(tenantRepository.existsByCode("NEW_TENANT")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant t = invocation.getArgument(0);
            t.setId(tenantId); // Simule l'ID généré
            t.setCode("NEW_TENANT"); // Important: set le code
            t.setName("New Tenant");
            t.setStatus("ACTIVE");
            return t;
        });

        // Créer un mock spécifique pour la resource
        Resource mockResource = new Resource();
        mockResource.setId(UUID.randomUUID());
        mockResource.setType("ROOT");
        mockResource.setName("New Tenant Root");
        mockResource.setTenant(tenant);

        when(resourceRepository.save(any(Resource.class))).thenReturn(mockResource);
        when(roleRepository.findByName("TENANT_ADMIN")).thenReturn(Optional.of(tenantAdminRole));

        UserRoleResource mockUrr = new UserRoleResource();
        mockUrr.setUser(creatorUser);
        mockUrr.setRole(tenantAdminRole);
        mockUrr.setResource(mockResource);

        // Utilise les factories directement ou mock-les
        try (MockedStatic<ResourceFactory> resourceFactoryMock = mockStatic(ResourceFactory.class);
             MockedStatic<UserRoleResourceFactory> urrFactoryMock = mockStatic(UserRoleResourceFactory.class)) {

            resourceFactoryMock.when(() ->
                    ResourceFactory.createRootResource(any(Tenant.class), eq("New Tenant"))
            ).thenReturn(mockResource);

            urrFactoryMock.when(() ->
                    UserRoleResourceFactory.create(creatorUser, tenantAdminRole, mockResource)
            ).thenReturn(mockUrr);

            when(urrRepository.save(any(UserRoleResource.class))).thenReturn(mockUrr);

            // When
            tenantService.createTenant("New Tenant", "NEW_TENANT", creatorUserId);

            // Then
            // 1. Vérifie que le tenant est sauvegardé
            verify(tenantRepository).save(argThat(tenant ->
                    tenant != null &&
                            tenant.getName().equals("New Tenant") &&
                            tenant.getCode().equals("NEW_TENANT") &&
                            "ACTIVE".equals(tenant.getStatus())
            ));

            // 2. Vérifie que la resource racine est créée - utiliser any() au lieu de argThat()
            verify(resourceRepository).save(any(Resource.class));

            // 3. Vérifie que l'assignation de rôle est faite
            verify(urrRepository).save(argThat(urr ->
                    urr != null &&
                            urr.getUser() != null &&
                            urr.getUser().getId().equals(creatorUserId) &&
                            urr.getRole() != null &&
                            urr.getRole().getName().equals("TENANT_ADMIN")
            ));

            // 4. Vérifie que l'audit log est généré
            verify(auditLogService).log(
                    argThat(tenant -> tenant != null && tenant.getCode().equals("NEW_TENANT")),
                    eq(creatorUser),
                    any(Resource.class),
                    eq("CREATE_TENANT"),
                    eq("TENANT"),
                    eq(tenantId),
                    eq("SUCCESS"),
                    eq("Tenant created"),
                    isNull(),
                    isNull()
            );
        }
    }
    @Test
    @DisplayName("2. Créer tenant → assignation automatique ADMIN_TENANT")
    void createTenant_shouldAutoAssignTenantAdminRole() {
        // Given
        when(userRepository.findById(creatorUserId)).thenReturn(Optional.of(creatorUser));
        when(tenantRepository.existsByCode("NEW_TENANT")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(resourceRepository.save(any(Resource.class))).thenReturn(rootResource);
        when(roleRepository.findByName("TENANT_ADMIN")).thenReturn(Optional.of(tenantAdminRole));

        // Mock des factories
        try (MockedStatic<ResourceFactory> resourceFactoryMock = mockStatic(ResourceFactory.class);
             MockedStatic<UserRoleResourceFactory> urrFactoryMock = mockStatic(UserRoleResourceFactory.class)) {

            resourceFactoryMock.when(() ->
                    ResourceFactory.createRootResource(any(Tenant.class), eq("New Tenant"))
            ).thenReturn(rootResource);

            UserRoleResource mockUrr = new UserRoleResource();
            mockUrr.setUser(creatorUser);
            mockUrr.setRole(tenantAdminRole);
            mockUrr.setResource(rootResource);

            urrFactoryMock.when(() ->
                    UserRoleResourceFactory.create(creatorUser, tenantAdminRole, rootResource)
            ).thenReturn(mockUrr);

            when(urrRepository.save(mockUrr)).thenReturn(mockUrr);

            // When
            tenantService.createTenant("New Tenant", "NEW_TENANT", creatorUserId);

            // Then
            verify(roleRepository).findByName("TENANT_ADMIN");
            verify(urrRepository).save(argThat(urr ->
                    urr.getUser().getId().equals(creatorUserId) &&
                            urr.getRole().getName().equals("TENANT_ADMIN") &&
                            urr.getResource().getType().equals("ROOT")
            ));
        }
    }

    @Test
    @DisplayName("3. Créer tenant avec code existant → échec + audit log")
    void createTenant_withExistingCode_shouldThrowException() {
        // Given
        when(userRepository.findById(creatorUserId)).thenReturn(Optional.of(creatorUser));
        when(tenantRepository.existsByCode("EXISTING_CODE")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() ->
                tenantService.createTenant("Existing Tenant", "EXISTING_CODE", creatorUserId)
        )
                .isInstanceOf(TenantAlreadyExistsException.class)
                .hasMessageContaining("EXISTING_CODE");

        // Vérifie que l'audit log d'échec est généré
        verify(auditLogService).log(
                isNull(),
                eq(creatorUser),
                isNull(),
                eq("CREATE_TENANT"),
                eq("TENANT"),
                isNull(),
                eq("FAILURE"),
                contains("Tenant code already exists: EXISTING_CODE"),
                isNull(),
                isNull()
        );

        // Vérifie que le tenant n'est pas sauvegardé
        verify(tenantRepository, never()).save(any());
        verify(resourceRepository, never()).save(any());
        verify(urrRepository, never()).save(any());
    }

    @Test
    @DisplayName("4. Créer tenant avec utilisateur inexistant → échec")
    void createTenant_withNonExistentUser_shouldThrowException() {
        // Given
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                tenantService.createTenant("New Tenant", "NEW_TENANT", UUID.randomUUID())
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User not found");

        verify(tenantRepository, never()).save(any());
    }

    @Test
    @DisplayName("5. Créer tenant → rôle TENANT_ADMIN manquant → échec")
    void createTenant_missingTenantAdminRole_shouldThrowException() {
        // Given
        when(userRepository.findById(creatorUserId)).thenReturn(Optional.of(creatorUser));
        when(tenantRepository.existsByCode("NEW_TENANT")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(resourceRepository.save(any(Resource.class))).thenReturn(rootResource);
        when(roleRepository.findByName("TENANT_ADMIN")).thenReturn(Optional.empty());

        // Mock de la factory de resource seulement
        try (MockedStatic<ResourceFactory> resourceFactoryMock = mockStatic(ResourceFactory.class)) {
            resourceFactoryMock.when(() ->
                    ResourceFactory.createRootResource(any(Tenant.class), eq("New Tenant"))
            ).thenReturn(rootResource);

            // When & Then
            assertThatThrownBy(() ->
                    tenantService.createTenant("New Tenant", "NEW_TENANT", creatorUserId)
            )
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("TENANT_ADMIN missing");

            verify(urrRepository, never()).save(any());
        }
    }

    /* ============================
       TESTS READ OPERATIONS
       ============================ */

    @Test
    @DisplayName("6. Récupérer tenant par ID → succès")
    void getTenantById_shouldReturnTenant() {
        // Given
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        // When
        Tenant result = tenantService.getTenantById(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(tenantId);
        assertThat(result.getCode()).isEqualTo("TENANT_TEST");
        verify(tenantRepository).findById(tenantId);
    }

    @Test
    @DisplayName("7. Récupérer tenant inexistant → échec")
    void getTenantById_nonExistent_shouldThrowException() {
        // Given
        when(tenantRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                tenantService.getTenantById(UUID.randomUUID())
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tenant not found");
    }

    @Test
    @DisplayName("8. Récupérer tenants accessibles par utilisateur → liste filtrée")
    void getTenantsAccessibleByUser_shouldReturnFilteredList() {
        // Given
        UUID userId = UUID.randomUUID();

        // Mock UserRoleResource avec différentes ressources
        UserRoleResource urr1 = mock(UserRoleResource.class);
        UserRoleResource urr2 = mock(UserRoleResource.class);
        UserRoleResource urr3 = mock(UserRoleResource.class); // Ressource avec parent (à filtrer)

        Resource rootResource1 = new Resource();
        rootResource1.setId(UUID.randomUUID());
        rootResource1.setParent(null);
        Tenant tenant1 = new Tenant("Tenant 1", "T1", "ACTIVE");
        rootResource1.setTenant(tenant1);

        Resource rootResource2 = new Resource();
        rootResource2.setId(UUID.randomUUID());
        rootResource2.setParent(null);
        Tenant tenant2 = new Tenant("Tenant 2", "T2", "ACTIVE");
        rootResource2.setTenant(tenant2);

        Resource childResource = new Resource();
        childResource.setId(UUID.randomUUID());
        childResource.setParent(rootResource1); // Ressource enfant

        when(urr1.getResource()).thenReturn(rootResource1);
        when(urr2.getResource()).thenReturn(rootResource2);
        when(urr3.getResource()).thenReturn(childResource);

        when(urrRepository.findAllByUserIdWithResourceAndTenant(userId))
                .thenReturn(Arrays.asList(urr1, urr2, urr3));

        // When
        List<Tenant> result = tenantService.getTenantsAccessibleByUser(userId);

        // Then
        assertThat(result)
                .hasSize(2)
                .extracting(Tenant::getCode)
                .containsExactlyInAnyOrder("T1", "T2");

        // Vérifie que les ressources avec parent sont filtrées
        assertThat(result).noneMatch(t -> t.getCode().equals("CHILD_TENANT"));
    }

    @Test
    @DisplayName("9. Récupérer tenants accessibles → liste vide si aucun accès")
    void getTenantsAccessibleByUser_noAccess_shouldReturnEmptyList() {
        // Given
        UUID userId = UUID.randomUUID();
        when(urrRepository.findAllByUserIdWithResourceAndTenant(userId))
                .thenReturn(Collections.emptyList());

        // When
        List<Tenant> result = tenantService.getTenantsAccessibleByUser(userId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("10. Récupérer tenants accessibles → doublons éliminés")
    void getTenantsAccessibleByUser_shouldRemoveDuplicates() {
        // Given
        UUID userId = UUID.randomUUID();

        Tenant sameTenant = new Tenant("Same Tenant", "SAME", "ACTIVE");

        Resource root1 = new Resource();
        root1.setId(UUID.randomUUID());
        root1.setParent(null);
        root1.setTenant(sameTenant);

        Resource root2 = new Resource();
        root2.setId(UUID.randomUUID());
        root2.setParent(null);
        root2.setTenant(sameTenant); // Même tenant

        UserRoleResource urr1 = mock(UserRoleResource.class);
        UserRoleResource urr2 = mock(UserRoleResource.class);

        when(urr1.getResource()).thenReturn(root1);
        when(urr2.getResource()).thenReturn(root2);

        when(urrRepository.findAllByUserIdWithResourceAndTenant(userId))
                .thenReturn(Arrays.asList(urr1, urr2));

        // When
        List<Tenant> result = tenantService.getTenantsAccessibleByUser(userId);

        // Then
        assertThat(result).hasSize(1); // Doublon éliminé
        assertThat(result.get(0).getCode()).isEqualTo("SAME");
    }

    /* ============================
       TESTS TRANSACTION & CONSISTENCY
       ============================ */

    @Test
    @DisplayName("11. Transaction → rollback en cas d'erreur après création partielle")
    void createTenant_transactionRollbackOnError() {
        // Given
        when(userRepository.findById(creatorUserId)).thenReturn(Optional.of(creatorUser));
        when(tenantRepository.existsByCode("NEW_TENANT")).thenReturn(false);

        Tenant savedTenant = new Tenant();
        savedTenant.setId(tenantId);
        savedTenant.setName("New Tenant");
        savedTenant.setCode("NEW_TENANT");
        savedTenant.setStatus("ACTIVE");

        when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);

        Resource mockResource = new Resource();
        mockResource.setId(UUID.randomUUID());
        mockResource.setType("ROOT");
        mockResource.setName("New Tenant Root");
        mockResource.setTenant(savedTenant);

        when(resourceRepository.save(any(Resource.class))).thenReturn(mockResource);
        when(roleRepository.findByName("TENANT_ADMIN")).thenReturn(Optional.of(tenantAdminRole));

        // Mock des factories
        try (MockedStatic<ResourceFactory> resourceFactoryMock = mockStatic(ResourceFactory.class)) {
            resourceFactoryMock.when(() ->
                    ResourceFactory.createRootResource(any(Tenant.class), eq("New Tenant"))
            ).thenReturn(mockResource);

            // Simuler l'erreur lors de la sauvegarde de l'assignation
            when(urrRepository.save(any(UserRoleResource.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() ->
                    tenantService.createTenant("New Tenant", "NEW_TENANT", creatorUserId)
            ).isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database error");

            // Vérifie que les étapes ont été tentées
            verify(tenantRepository).save(any());
            verify(resourceRepository).save(any());
            verify(roleRepository).findByName("TENANT_ADMIN");
            verify(urrRepository).save(any());

            // L'audit log ne doit PAS être appelé (car exception avant)
            verify(auditLogService, never()).log(any(), any(), any(), anyString(),
                    anyString(), any(), anyString(), anyString(), any(), any());
        }
    }
    @Test
    @DisplayName("12. Audit log généré pour chaque opération")
    void allOperations_shouldGenerateAuditLog() {
        // Test create
        when(userRepository.findById(creatorUserId)).thenReturn(Optional.of(creatorUser));
        when(tenantRepository.existsByCode("NEW_TENANT")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(resourceRepository.save(any(Resource.class))).thenReturn(rootResource);
        when(roleRepository.findByName("TENANT_ADMIN")).thenReturn(Optional.of(tenantAdminRole));

        // Mock des factories
        try (MockedStatic<ResourceFactory> resourceFactoryMock = mockStatic(ResourceFactory.class);
             MockedStatic<UserRoleResourceFactory> urrFactoryMock = mockStatic(UserRoleResourceFactory.class)) {

            resourceFactoryMock.when(() ->
                    ResourceFactory.createRootResource(any(Tenant.class), eq("New Tenant"))
            ).thenReturn(rootResource);

            UserRoleResource mockUrr = new UserRoleResource();
            urrFactoryMock.when(() ->
                    UserRoleResourceFactory.create(creatorUser, tenantAdminRole, rootResource)
            ).thenReturn(mockUrr);

            when(urrRepository.save(mockUrr)).thenReturn(mockUrr);

            tenantService.createTenant("New Tenant", "NEW_TENANT", creatorUserId);

            verify(auditLogService, atLeastOnce()).log(any(), any(), any(), anyString(),
                    anyString(), any(), anyString(), anyString(), any(), any());
        }
    }

    @Test
    @DisplayName("13. Création tenant avec status par défaut ACTIVE")
    void createTenant_shouldSetDefaultStatusActive() {
        // Given
        when(userRepository.findById(creatorUserId)).thenReturn(Optional.of(creatorUser));
        when(tenantRepository.existsByCode("NEW_TENANT")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant t = invocation.getArgument(0);
            t.setId(tenantId);
            return t;
        });
        when(resourceRepository.save(any(Resource.class))).thenReturn(rootResource);
        when(roleRepository.findByName("TENANT_ADMIN")).thenReturn(Optional.of(tenantAdminRole));

        // Mock des factories
        try (MockedStatic<ResourceFactory> resourceFactoryMock = mockStatic(ResourceFactory.class);
             MockedStatic<UserRoleResourceFactory> urrFactoryMock = mockStatic(UserRoleResourceFactory.class)) {

            resourceFactoryMock.when(() ->
                    ResourceFactory.createRootResource(any(Tenant.class), eq("New Tenant"))
            ).thenReturn(rootResource);

            UserRoleResource mockUrr = new UserRoleResource();
            urrFactoryMock.when(() ->
                    UserRoleResourceFactory.create(creatorUser, tenantAdminRole, rootResource)
            ).thenReturn(mockUrr);

            when(urrRepository.save(mockUrr)).thenReturn(mockUrr);

            // When
            tenantService.createTenant("New Tenant", "NEW_TENANT", creatorUserId);

            // Then - Vérifie que le status est ACTIVE par défaut
            verify(tenantRepository).save(argThat(t ->
                    "ACTIVE".equals(t.getStatus())
            ));
        }
    }

    @Test
    @DisplayName("14. Erreur dans la création de resource → rollback complet")
    void createTenant_errorCreatingResource_shouldRollback() {
        // Given
        when(userRepository.findById(creatorUserId)).thenReturn(Optional.of(creatorUser));
        when(tenantRepository.existsByCode("NEW_TENANT")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        // Mock la factory de resource
        try (MockedStatic<ResourceFactory> resourceFactoryMock = mockStatic(ResourceFactory.class)) {
            resourceFactoryMock.when(() ->
                    ResourceFactory.createRootResource(any(Tenant.class), eq("New Tenant"))
            ).thenReturn(rootResource);

            // Simuler une erreur lors de la création de la resource
            when(resourceRepository.save(any(Resource.class)))
                    .thenThrow(new RuntimeException("Cannot create resource"));

            // When & Then
            assertThatThrownBy(() ->
                    tenantService.createTenant("New Tenant", "NEW_TENANT", creatorUserId)
            ).isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Cannot create resource");

            // Le tenant a été sauvegardé, mais transaction devrait rollback
            verify(tenantRepository).save(any());
            verify(urrRepository, never()).save(any());
        }
    }
}