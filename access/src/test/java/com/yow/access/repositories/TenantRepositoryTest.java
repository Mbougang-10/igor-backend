package com.yow.access.repositories;

import com.yow.access.entities.Tenant;
import com.yow.access.repositories.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Version 1 : Test d'intégration complet (plus lent)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class TenantRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant savedTenant;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        // Nettoyer avant chaque test
        tenantRepository.deleteAll();

        // Créer un tenant de test
        Tenant tenant = new Tenant();
        tenant.setCode("TENANT_A");
        tenant.setName("Tenant Alpha");

        savedTenant = tenantRepository.save(tenant);
        tenantId = savedTenant.getId();
    }

    @Test
    void createTenant_shouldPersist() {
        // Given
        Tenant newTenant = new Tenant();
        newTenant.setCode("TENANT_B");
        newTenant.setName("Tenant Beta");

        // When
        Tenant persisted = tenantRepository.save(newTenant);

        // Then
        assertThat(persisted.getId()).isNotNull();
        assertThat(persisted.getCode()).isEqualTo("TENANT_B");
        assertThat(persisted.getName()).isEqualTo("Tenant Beta");
    }

    @Test
    void findById_shouldReturnTenant() {
        // When
        Optional<Tenant> found = tenantRepository.findById(tenantId);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(tenantId);
        assertThat(found.get().getCode()).isEqualTo("TENANT_A");
    }

    @Test
    void findById_nonExistent_shouldReturnEmpty() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        Optional<Tenant> found = tenantRepository.findById(nonExistentId);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByCode_shouldReturnTrueWhenCodeExists() {
        // When & Then
        assertThat(tenantRepository.existsByCode("TENANT_A")).isTrue();
        assertThat(tenantRepository.existsByCode("NON_EXISTENT")).isFalse();
    }

    @Test
    void findByCode_shouldReturnTenant() {
        // When
        Optional<Tenant> found = tenantRepository.findByCode("TENANT_A");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("TENANT_A");
    }

    @Test
    void deleteTenant_shouldOnlyDeleteTenant() {
        // Given - Vérifier que le tenant existe
        assertThat(tenantRepository.findById(tenantId)).isPresent();

        // When
        tenantRepository.deleteById(tenantId);

        // Then
        assertThat(tenantRepository.findById(tenantId)).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllTenants() {
        // Given - Ajouter un deuxième tenant
        Tenant tenant2 = new Tenant();
        tenant2.setCode("TENANT_B");
        tenant2.setName("Tenant Beta");
        tenantRepository.save(tenant2);

        // When
        List<Tenant> allTenants = tenantRepository.findAll();

        // Then
        assertThat(allTenants).hasSize(2);
        assertThat(allTenants).extracting(Tenant::getCode)
                .containsExactlyInAnyOrder("TENANT_A", "TENANT_B");
    }
}