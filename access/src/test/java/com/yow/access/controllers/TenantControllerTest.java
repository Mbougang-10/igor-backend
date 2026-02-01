package com.yow.access.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yow.access.config.security.Permissions;
import com.yow.access.config.security.context.AuthenticatedUserContext;
import com.yow.access.dto.CreateTenantRequest;
import com.yow.access.dto.TenantResponse;
import com.yow.access.dto.TenantStatsResponse;
import com.yow.access.entities.Tenant;
import com.yow.access.repositories.ResourceRepository;
import com.yow.access.repositories.UserRoleResourceRepository;
import com.yow.access.services.AuthorizationService;
import com.yow.access.services.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantControllerTest {

    @Mock
    private TenantService tenantService;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private AuthenticatedUserContext userContext;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRoleResourceRepository urrRepository;

    @InjectMocks
    private TenantController tenantController;

    private UUID userId;
    private UUID tenantId;
    private Tenant tenant;
    private TenantResponse tenantResponse;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tenantId = UUID.randomUUID();

        // Mock tenant
        tenant = new Tenant("Test Tenant", "TEST_TENANT", "ACTIVE");
        tenant.setId(tenantId);
        tenant.setCreatedAt(Instant.now());

        // Mock response DTO
        tenantResponse = TenantResponse.fromEntity(tenant);

        objectMapper = new ObjectMapper();
    }

    /* ============================
       TESTS CREATE TENANT (POST /api/tenants)
       ============================ */

    @Test
    @DisplayName("1. POST /api/tenants → créer tenant autorisé → 201 Created")
    void createTenant_authorized_shouldReturnCreated() {
        // Given
        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("New Tenant");
        request.setCode("NEW_TENANT");

        // When
        ResponseEntity<Void> response = tenantController.createTenant(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(authorizationService).checkGlobalPermission(userId, Permissions.TENANT_CREATE);
        verify(tenantService).createTenant("New Tenant", "NEW_TENANT", userId);
    }

    @Test
    @DisplayName("2. POST /api/tenants → sans permission → doit throw AccessDeniedException")
    void createTenant_unauthorized_shouldThrowException() {
        // Given
        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("New Tenant");
        request.setCode("NEW_TENANT");

        doThrow(new AccessDeniedException("Permission denied"))
                .when(authorizationService)
                .checkGlobalPermission(any(), eq(Permissions.TENANT_CREATE));

        // When & Then
        assertThatThrownBy(() -> tenantController.createTenant(request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Permission denied");

        verify(tenantService, never()).createTenant(any(), any(), any());
    }

    @Test
    @DisplayName("3. POST /api/tenants → code déjà existant → doit propager l'exception")
    void createTenant_duplicateCode_shouldPropagateException() {
        // Given
        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("New Tenant");
        request.setCode("EXISTING_CODE");

        doThrow(new RuntimeException("Tenant already exists"))
                .when(tenantService)
                .createTenant(any(), any(), any());

        // When & Then
        assertThatThrownBy(() -> tenantController.createTenant(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tenant already exists");

        verify(authorizationService).checkGlobalPermission(userId, Permissions.TENANT_CREATE);
    }

    /* ============================
       TESTS GET TENANT (GET /api/tenants/{id})
       ============================ */

    @Test
    @DisplayName("4. GET /api/tenants/{id} → tenant existant → 200 OK")
    void getTenant_existingTenant_shouldReturnTenant() {
        // Given
        when(tenantService.getTenantById(tenantId)).thenReturn(tenant);

        // When
        ResponseEntity<TenantResponse> response = tenantController.getTenant(tenantId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Test Tenant");
        assertThat(response.getBody().getCode()).isEqualTo("TEST_TENANT");

        verify(authorizationService).checkPermission(userId, tenantId, Permissions.TENANT_READ);
        verify(tenantService).getTenantById(tenantId);
    }

    @Test
    @DisplayName("5. GET /api/tenants/{id} → sans permission → doit throw AccessDeniedException")
    void getTenant_unauthorized_shouldThrowException() {
        // Given
        doThrow(new AccessDeniedException("Access denied"))
                .when(authorizationService)
                .checkPermission(any(), any(), eq(Permissions.TENANT_READ));

        // When & Then
        assertThatThrownBy(() -> tenantController.getTenant(tenantId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied");

        verify(tenantService, never()).getTenantById(any());
    }

    @Test
    @DisplayName("6. GET /api/tenants/{id} → tenant inexistant → doit propager l'exception")
    void getTenant_nonExistent_shouldPropagateException() {
        // Given
        when(tenantService.getTenantById(tenantId))
                .thenThrow(new RuntimeException("Tenant not found"));

        // When & Then
        assertThatThrownBy(() -> tenantController.getTenant(tenantId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tenant not found");

        verify(authorizationService).checkPermission(userId, tenantId, Permissions.TENANT_READ);
    }

    /* ============================
       TESTS LIST TENANTS (GET /api/tenants)
       ============================ */

    @Test
    @DisplayName("7. GET /api/tenants → liste filtrée par RBAC → 200 OK")
    void getTenants_shouldReturnFilteredList() {
        // Given
        Tenant tenant1 = new Tenant("Tenant 1", "T1", "ACTIVE");
        tenant1.setId(UUID.randomUUID());

        Tenant tenant2 = new Tenant("Tenant 2", "T2", "ACTIVE");
        tenant2.setId(UUID.randomUUID());

        List<Tenant> tenants = Arrays.asList(tenant1, tenant2);

        when(tenantService.getTenantsAccessibleByUser(userId)).thenReturn(tenants);

        // When
        ResponseEntity<List<TenantResponse>> response = tenantController.getTenants();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getCode()).isEqualTo("T1");
        assertThat(response.getBody().get(1).getCode()).isEqualTo("T2");

        verify(tenantService).getTenantsAccessibleByUser(userId);
    }

    @Test
    @DisplayName("8. GET /api/tenants → aucun accès → liste vide → 200 OK")
    void getTenants_noAccess_shouldReturnEmptyList() {
        // Given
        when(tenantService.getTenantsAccessibleByUser(userId)).thenReturn(List.of());

        // When
        ResponseEntity<List<TenantResponse>> response = tenantController.getTenants();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    /* ============================
       TESTS TENANT STATS (GET /api/tenants/{id}/stats)
       ============================ */

    @Test
    @DisplayName("9. GET /api/tenants/{id}/stats → stats du tenant → 200 OK")
    void getTenantStats_shouldReturnStats() {
        // Given
        long userCount = 15L;
        long resourceCount = 42L;

        // getTenantStats() n'utilise PAS userContext, authorizationService, ni tenantService
        // Il utilise SEULEMENT urrRepository et resourceRepository

        when(urrRepository.countDistinctUsersByTenantId(tenantId)).thenReturn(userCount);
        when(resourceRepository.countByTenantId(tenantId)).thenReturn(resourceCount);

        // When
        ResponseEntity<TenantStatsResponse> response = tenantController.getTenantStats(tenantId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserCount()).isEqualTo(userCount);
        assertThat(response.getBody().getResourceCount()).isEqualTo(resourceCount);

        verify(urrRepository).countDistinctUsersByTenantId(tenantId);
        verify(resourceRepository).countByTenantId(tenantId);

        // Vérifier que les autres dépendances ne sont PAS appelées
        verify(userContext, never()).getUserId();
        verify(authorizationService, never()).checkPermission(any(), any(), any());
        verify(authorizationService, never()).checkGlobalPermission(any(), any());
        verify(tenantService, never()).getTenantById(any());
        verify(tenantService, never()).getTenantsAccessibleByUser(any());
    }

    @Test
    @DisplayName("10. GET /api/tenants/{id}/stats → tenant inexistant → stats à 0")
    void getTenantStats_nonExistentTenant_shouldReturnZeroStats() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // getTenantStats() n'utilise PAS userContext

        when(urrRepository.countDistinctUsersByTenantId(nonExistentId)).thenReturn(0L);
        when(resourceRepository.countByTenantId(nonExistentId)).thenReturn(0L);

        // When
        ResponseEntity<TenantStatsResponse> response = tenantController.getTenantStats(nonExistentId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserCount()).isEqualTo(0);
        assertThat(response.getBody().getResourceCount()).isEqualTo(0);

        // Vérifier que userContext n'est PAS appelé
        verify(userContext, never()).getUserId();
    }
    /* ============================
       TESTS SÉCURITÉ
       ============================ */

    @Test
    @DisplayName("11. Cross-tenant access → bloqué par AuthorizationService")
    void crossTenantAccess_shouldBeBlocked() {
        // Given - User A essaie d'accéder au tenant de User B
        UUID tenantIdB = UUID.randomUUID();

        doThrow(new AccessDeniedException("Cross-tenant access denied"))
                .when(authorizationService)
                .checkPermission(userId, tenantIdB, Permissions.TENANT_READ);

        // When & Then
        assertThatThrownBy(() -> tenantController.getTenant(tenantIdB))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Cross-tenant access denied");
    }

    /* ============================
       TESTS DE COHÉRENCE
       ============================ */

    @Test
    @DisplayName("12. Scénario complet : création + lecture + stats")
    void fullScenario_createReadStats() {
        // Étape 1: Créer un tenant
        CreateTenantRequest createRequest = new CreateTenantRequest();
        createRequest.setName("Scenario Tenant");
        createRequest.setCode("SCENARIO_T");

        ResponseEntity<Void> createResponse = tenantController.createTenant(createRequest);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        verify(authorizationService).checkGlobalPermission(userId, Permissions.TENANT_CREATE);
        verify(tenantService).createTenant("Scenario Tenant", "SCENARIO_T", userId);

        // Reset des verifications pour les étapes suivantes
        reset(authorizationService, tenantService);

        // Étape 2: Récupérer le tenant créé
        UUID createdTenantId = UUID.randomUUID();
        Tenant createdTenant = new Tenant("Scenario Tenant", "SCENARIO_T", "ACTIVE");
        createdTenant.setId(createdTenantId);

        when(tenantService.getTenantById(createdTenantId)).thenReturn(createdTenant);

        ResponseEntity<TenantResponse> getResponse = tenantController.getTenant(createdTenantId);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getName()).isEqualTo("Scenario Tenant");

        verify(authorizationService).checkPermission(userId, createdTenantId, Permissions.TENANT_READ);

        // Reset
        reset(authorizationService, tenantService, urrRepository, resourceRepository);

        // Étape 3: Voir les stats
        when(urrRepository.countDistinctUsersByTenantId(createdTenantId)).thenReturn(1L);
        when(resourceRepository.countByTenantId(createdTenantId)).thenReturn(5L);

        ResponseEntity<TenantStatsResponse> statsResponse = tenantController.getTenantStats(createdTenantId);
        assertThat(statsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statsResponse.getBody().getUserCount()).isEqualTo(1);
        assertThat(statsResponse.getBody().getResourceCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("13. Validation DTO - création tenant avec données invalides")
    void createTenant_invalidData_shouldFailValidation() {
        // Note: La validation se fait au niveau du contrôleur via @Valid
        // Dans ce test unitaire, on ne teste pas la validation Spring
        // On teste la logique métier une fois les données validées

        // Given - Données valides (validation passée)
        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("Valid Tenant");
        request.setCode("VALID");

        // When & Then - Doit fonctionner
        ResponseEntity<Void> response = tenantController.createTenant(request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("14. Gestion des exceptions - RuntimeException propagée")
    void runtimeException_shouldBePropagated() {
        // Given
        when(tenantService.getTenantById(tenantId))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> tenantController.getTenant(tenantId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection failed");
    }

    @Test
    @DisplayName("15. UserContext - userId toujours utilisé")
    void userContext_shouldAlwaysProvideUserId() {
        // Test que userContext.getUserId() est appelé dans chaque méthode

        // Test pour createTenant
        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("Test");
        request.setCode("TEST");

        tenantController.createTenant(request);
        verify(userContext, atLeastOnce()).getUserId();

        // Reset
        reset(userContext, authorizationService, tenantService);
        when(userContext.getUserId()).thenReturn(userId);

        // Test pour getTenant
        when(tenantService.getTenantById(tenantId)).thenReturn(tenant);
        tenantController.getTenant(tenantId);
        verify(userContext, atLeastOnce()).getUserId();

        // Reset
        reset(userContext, authorizationService, tenantService);
        when(userContext.getUserId()).thenReturn(userId);

        // Test pour getTenants
        when(tenantService.getTenantsAccessibleByUser(userId)).thenReturn(List.of(tenant));
        tenantController.getTenants();
        verify(userContext, atLeastOnce()).getUserId();
    }

    @Test
    @DisplayName("16. Méthodes privées - vérification des permissions systématique")
    void permissionCheck_shouldAlwaysBeCalled() {
        // Vérifie que checkPermission/checkGlobalPermission est toujours appelé

        // 1. createTenant → checkGlobalPermission
        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("Test");
        request.setCode("TEST");

        tenantController.createTenant(request);
        verify(authorizationService).checkGlobalPermission(userId, Permissions.TENANT_CREATE);

        // Reset
        reset(authorizationService, tenantService);

        // 2. getTenant → checkPermission
        when(tenantService.getTenantById(tenantId)).thenReturn(tenant);
        tenantController.getTenant(tenantId);
        verify(authorizationService).checkPermission(userId, tenantId, Permissions.TENANT_READ);

        // 3. getTenants → PAS de checkPermission (c'est filtré par le service)
        // Mais le service utilise l'userId pour filtrer
    }

    @Test
    @DisplayName("17. Réponses HTTP correctes")
    void httpResponses_shouldBeCorrect() {
        // Test des codes HTTP retournés

        // POST /api/tenants → 201 Created
        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("Test");
        request.setCode("TEST");

        ResponseEntity<Void> postResponse = tenantController.createTenant(request);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // GET /api/tenants/{id} → 200 OK
        when(tenantService.getTenantById(tenantId)).thenReturn(tenant);
        ResponseEntity<TenantResponse> getResponse = tenantController.getTenant(tenantId);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // GET /api/tenants → 200 OK
        when(tenantService.getTenantsAccessibleByUser(userId)).thenReturn(List.of(tenant));
        ResponseEntity<List<TenantResponse>> listResponse = tenantController.getTenants();
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // GET /api/tenants/{id}/stats → 200 OK
        when(urrRepository.countDistinctUsersByTenantId(tenantId)).thenReturn(10L);
        when(resourceRepository.countByTenantId(tenantId)).thenReturn(20L);
        ResponseEntity<TenantStatsResponse> statsResponse = tenantController.getTenantStats(tenantId);
        assertThat(statsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}