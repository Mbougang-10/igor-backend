package com.yow.access.repositories;

import com.yow.access.entities.Resource;
import com.yow.access.entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    List<Resource> findAllByTenant(Tenant tenant);

    List<Resource> findAllByParent(Resource parent);

    boolean existsByTenantAndPath(Tenant tenant, String path);

    List<Resource> findByParentId(UUID parentId);

}

