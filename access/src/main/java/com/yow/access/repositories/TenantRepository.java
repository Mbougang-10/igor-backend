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
}
