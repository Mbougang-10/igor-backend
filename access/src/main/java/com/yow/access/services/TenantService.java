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
        return tenantRepository.findTenantsAccessibleByUser(userId);
    }
}
