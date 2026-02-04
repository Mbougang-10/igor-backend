package com.yow.access.services;

import com.yow.access.entities.*;
import com.yow.access.entities.Resource;
import com.yow.access.exceptions.TenantAlreadyExistsException;
import com.yow.access.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleResourceRepository urrRepository;
    private final AuditLogService auditLogService;

    public TenantService(
            TenantRepository tenantRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleResourceRepository urrRepository,
            AuditLogService auditLogService
    ) {
        this.tenantRepository = tenantRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.urrRepository = urrRepository;
        this.auditLogService = auditLogService;
    }

    /* ============================
       CREATE TENANT
       ============================ */
    @Transactional
    public void createTenant(
            String name,
            String code,
            UUID creatorUserId
    ) {
        AppUser creator =
                userRepository.findById(creatorUserId)
                        .orElseThrow(() -> new IllegalStateException("User not found"));

        if (tenantRepository.existsByCode(code)) {
            auditLogService.log(
                    null,
                    creator,
                    null,
                    "CREATE_TENANT",
                    "TENANT",
                    null,
                    "FAILURE",
                    "Tenant code already exists: " + code,
                    null,
                    null
            );
            throw new TenantAlreadyExistsException(code);
        }

        Tenant tenant = new Tenant();
        tenant.setName(name);
        tenant.setCode(code);
        tenant.setStatus("ACTIVE");

        tenantRepository.save(tenant);

        Resource root =
                ResourceFactory.createRootResource(tenant, name);
        resourceRepository.save(root);

        Role adminRole =
                roleRepository.findByName("TENANT_ADMIN")
                        .orElseThrow(() -> new IllegalStateException("TENANT_ADMIN missing"));

        UserRoleResource urr =
                UserRoleResourceFactory.create(creator, adminRole, root);
        urrRepository.save(urr);

        auditLogService.log(
                tenant,
                creator,
                root,
                "CREATE_TENANT",
                "TENANT",
                tenant.getId(),
                "SUCCESS",
                "Tenant created",
                null,
                null
        );
    }

    /* ============================
       READ
       ============================ */
    public Tenant getTenantById(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant not found"));
    }

    public List<Tenant> getTenantsAccessibleByUser(UUID userId) {
        // Récupérer toutes les ressources auxquelles l'utilisateur a accès
        // Utiliser la méthode avec JOIN FETCH pour éviter les problèmes de lazy loading
        List<UserRoleResource> userRoles = urrRepository.findAllByUserIdWithResourceAndTenant(userId);

        // Filtrer pour ne garder que les ressources ROOT (sans parent)
        // et extraire les tenants uniques
        return userRoles.stream()
                .map(UserRoleResource::getResource)
                .filter(resource -> resource.getParent() == null)
                .map(Resource::getTenant)
                .distinct()
                .toList();
    }
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    /* ============================
       SUPER ADMIN
       ============================ */
    @Transactional(readOnly = true)
    public List<com.yow.access.dto.TenantSummaryDTO> getAllTenantsWithOwners() {
        List<Object[]> results = tenantRepository.findAllTenantsWithOwnersRaw();

        // On utilise une map pour dédupliquer les tenants (au cas où il y ait plusieurs admins)
        // On garde le premier admin trouvé
        java.util.Map<UUID, com.yow.access.dto.TenantSummaryDTO> tenantMap = new java.util.LinkedHashMap<>();

        for (Object[] row : results) {
            UUID id = (UUID) row[0];
            if (!tenantMap.containsKey(id)) {
                String name = (String) row[1];
                String code = (String) row[2];
                String status = (String) row[3];
                // Conversion Timestamp -> Instant
                java.sql.Timestamp ts = (java.sql.Timestamp) row[4];
                java.time.Instant createdAt = ts != null ? ts.toInstant() : null;
                String ownerName = (String) row[5];
                String ownerEmail = (String) row[6];

                tenantMap.put(id, new com.yow.access.dto.TenantSummaryDTO(
                        id, name, code, status, createdAt, ownerName, ownerEmail
                ));
            }
        }

        return new java.util.ArrayList<>(tenantMap.values());
    }
}
