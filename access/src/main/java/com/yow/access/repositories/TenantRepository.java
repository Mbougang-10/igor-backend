package com.yow.access.repositories;

import com.yow.access.entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findByCode(String code);

    boolean existsByCode(String code);

    @Query(value = """
        SELECT DISTINCT t.*
        FROM user_role_resource urr
        JOIN resource r ON r.id = urr.resource_id
        JOIN tenant t ON t.id = r.tenant_id
        WHERE urr.user_id = CAST(:userId AS UUID)
          AND r.parent_id IS NULL
        """, nativeQuery = true)
    List<Tenant> findTenantsAccessibleByUser(@Param("userId") UUID userId);

    @Query(value = """
        SELECT 
            t.id as tenantId, 
            t.name as tenantName, 
            t.code as tenantCode, 
            t.status as tenantStatus, 
            t.created_at as createdAt,
            u.username as ownerName,
            u.email as ownerEmail
        FROM tenant t
        LEFT JOIN resource r ON r.tenant_id = t.id AND r.parent_id IS NULL AND r.type = 'ROOT'
        LEFT JOIN user_role_resource urr ON urr.resource_id = r.id
        LEFT JOIN role rol ON rol.id = urr.role_id AND rol.name = 'TENANT_ADMIN'
        LEFT JOIN app_user u ON u.id = urr.user_id
        -- Pour éviter les doublons si plusieurs admins, on prend le plus ancien par exemple, 
        -- mais ici on va just laisser les doublons ou gérer coté service.
        -- DISTINCT pour éviter trop de bruit si configs bizarres
        ORDER BY t.created_at DESC
        """, nativeQuery = true)
    List<Object[]> findAllTenantsWithOwnersRaw();
}
