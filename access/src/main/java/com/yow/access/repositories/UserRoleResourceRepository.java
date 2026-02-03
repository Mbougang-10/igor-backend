package com.yow.access.repositories;

import com.yow.access.entities.UserRoleResource;
import com.yow.access.entities.UserRoleResourceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleResourceRepository
        extends JpaRepository<UserRoleResource, UserRoleResourceId> {

    List<UserRoleResource> findAllByIdUserId(UUID userId);

    // Query with JOIN FETCH to eagerly load Resource, Parent, and Tenant
    @Query("SELECT urr FROM UserRoleResource urr " +
           "JOIN FETCH urr.resource r " +
           "JOIN FETCH r.tenant " +
           "LEFT JOIN FETCH r.parent " +
           "WHERE urr.id.userId = :userId")
    List<UserRoleResource> findAllByUserIdWithResourceAndTenant(@Param("userId") UUID userId);

    boolean existsByIdUserIdAndIdRoleIdAndIdResourceId(
            UUID userId,
            Short roleId,
            Long resourceId
    );

    List<UserRoleResource> findAllByUserId(UUID userId);

    // Compter les utilisateurs distincts ayant accès à un tenant (via ses ressources)
    @Query("SELECT COUNT(DISTINCT urr.user.id) FROM UserRoleResource urr WHERE urr.resource.tenant.id = :tenantId")
    long countDistinctUsersByTenantId(@Param("tenantId") UUID tenantId);

    Optional<UserRoleResource> findByUserIdAndRoleIdAndResourceId(
            UUID userId,
            Short roleId,
            UUID resourceId
    );

    @Query("SELECT DISTINCT urr.user FROM UserRoleResource urr WHERE urr.resource.tenant.id = :tenantId")
    List<com.yow.access.entities.AppUser> findUsersByTenantId(@Param("tenantId") UUID tenantId);
}

