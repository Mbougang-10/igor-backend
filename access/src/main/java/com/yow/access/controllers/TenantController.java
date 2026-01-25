package com.yow.access.controllers;

import com.yow.access.config.security.Permissions;
import com.yow.access.dto.CreateTenantRequest;
import com.yow.access.dto.TenantResponse;
import com.yow.access.dto.TenantStatsResponse;
import com.yow.access.entities.Tenant;
import com.yow.access.repositories.ResourceRepository;
import com.yow.access.repositories.UserRoleResourceRepository;
import com.yow.access.services.AuthorizationService;
import com.yow.access.services.TenantService;
import com.yow.access.config.security.context.AuthenticatedUserContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;
    private final AuthorizationService authorizationService;
    private final AuthenticatedUserContext userContext;
    private final ResourceRepository resourceRepository;
    private final UserRoleResourceRepository urrRepository;

    public TenantController(
            TenantService tenantService,
            AuthorizationService authorizationService,
            AuthenticatedUserContext userContext,
            ResourceRepository resourceRepository,
            UserRoleResourceRepository urrRepository
    ) {
        this.tenantService = tenantService;
        this.authorizationService = authorizationService;
        this.userContext = userContext;
        this.resourceRepository = resourceRepository;
        this.urrRepository = urrRepository;
    }

    /* ============================
       CREATE TENANT (GLOBAL)
       ============================ */
    @PostMapping
    public ResponseEntity<Void> createTenant(
            @Valid @RequestBody CreateTenantRequest request
    ) {
        UUID userId = userContext.getUserId();

        authorizationService.checkGlobalPermission(
                userId,
                Permissions.TENANT_CREATE
        );

        tenantService.createTenant(
                request.getName(),
                request.getCode(),
                userId
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /* ============================
       GET ONE TENANT (RBAC)
       ============================ */
    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantResponse> getTenant(
            @PathVariable UUID tenantId
    ) {
        UUID userId = userContext.getUserId();

        authorizationService.checkPermission(
                userId,
                tenantId,
                Permissions.TENANT_READ
        );

        Tenant tenant = tenantService.getTenantById(tenantId);

        return ResponseEntity.ok(
                TenantResponse.fromEntity(tenant)
        );
    }

    /* ============================
       LIST TENANTS (RBAC FILTERED)
       ============================ */
    @GetMapping
    public ResponseEntity<List<TenantResponse>> getTenants() {

        UUID userId = userContext.getUserId();

        List<TenantResponse> tenants =
                tenantService.getTenantsAccessibleByUser(userId)
                        .stream()
                        .map(TenantResponse::fromEntity)
                        .toList();

        return ResponseEntity.ok(tenants);
    }

    /* ============================
       GET TENANT STATS
       ============================ */
    @GetMapping("/{tenantId}/stats")
    public ResponseEntity<TenantStatsResponse> getTenantStats(
            @PathVariable UUID tenantId
    ) {
        long userCount = urrRepository.countDistinctUsersByTenantId(tenantId);
        long resourceCount = resourceRepository.countByTenantId(tenantId);

        return ResponseEntity.ok(new TenantStatsResponse(userCount, resourceCount));
    }
}
