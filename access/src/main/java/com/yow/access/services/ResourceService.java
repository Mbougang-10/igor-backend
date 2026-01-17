package com.yow.access.services;

import com.yow.access.dto.ResourceTreeResponse;
import com.yow.access.entities.Resource;
import com.yow.access.entities.ResourceFactory;
import com.yow.access.exceptions.AccessDeniedException;
import com.yow.access.repositories.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final AuthorizationService authorizationService;
    private final AuditLogService auditLogService;

    public ResourceService(
            ResourceRepository resourceRepository,
            AuthorizationService authorizationService,
            AuditLogService auditLogService
    ) {
        this.resourceRepository = resourceRepository;
        this.authorizationService = authorizationService;
        this.auditLogService = auditLogService;
    }

    /* =========================================================
       CREATE RESOURCE
       ========================================================= */
    @Transactional
    public void createChildResource(
            UUID actorUserId,
            UUID parentResourceId,
            String name,
            String type
    ) {
        Resource parent = loadResource(parentResourceId);

        authorizationService.checkPermission(
                actorUserId,
                parent.getId(),
                "RESOURCE_CREATE"
        );

        Resource child =
                ResourceFactory.createChildResource(parent, name, type);

        resourceRepository.save(child);

        auditLogService.log(
                parent.getTenant(),
                null,
                child,
                "CREATE_RESOURCE",
                "RESOURCE",
                child.getId(),
                "SUCCESS",
                "Child resource created",
                null,
                null
        );
    }

    /* =========================================================
       READ TREE
       ========================================================= */
    @Transactional(readOnly = true)
    public ResourceTreeResponse getResourceTree(
            UUID userId,
            UUID rootResourceId
    ) {
        authorizationService.checkPermission(
                userId,
                rootResourceId,
                "RESOURCE_READ"
        );

        Resource root = loadResource(rootResourceId);
        return buildTree(root);
    }

    private ResourceTreeResponse buildTree(Resource resource) {
        ResourceTreeResponse node =
                ResourceTreeResponse.fromEntity(resource);

        List<Resource> children =
                resourceRepository.findByParentId(resource.getId());

        for (Resource child : children) {
            node.addChild(buildTree(child));
        }

        return node;
    }

    /* =========================================================
       DELETE RESOURCE
       ========================================================= */
    @Transactional
    public void deleteResource(
            UUID userId,
            UUID resourceId
    ) {
        Resource resource = loadResource(resourceId);

        authorizationService.checkPermission(
                userId,
                resourceId,
                "RESOURCE_DELETE"
        );

        resourceRepository.delete(resource);

        auditLogService.log(
                resource.getTenant(),
                null,
                resource,
                "DELETE_RESOURCE",
                "RESOURCE",
                resourceId,
                "SUCCESS",
                "Resource deleted",
                null,
                null
        );
    }

    /* =========================================================
       MOVE RESOURCE
       ========================================================= */
    @Transactional
    public void moveResource(
            UUID userId,
            UUID resourceId,
            UUID newParentId
    ) {
        Resource resource = loadResource(resourceId);
        Resource newParent = loadResource(newParentId);

        authorizationService.checkPermission(
                userId,
                resourceId,
                "RESOURCE_MOVE"
        );

        resource.setParent(newParent);
        resource.setPath(
                newParent.getPath() + "/" + resource.getName()
        );

        resourceRepository.save(resource);

        auditLogService.log(
                resource.getTenant(),
                null,
                resource,
                "MOVE_RESOURCE",
                "RESOURCE",
                resourceId,
                "SUCCESS",
                "Resource moved",
                null,
                null
        );
    }

    /* =========================================================
       UTIL
       ========================================================= */
    private Resource loadResource(UUID resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() ->
                        new IllegalStateException("Resource not found: " + resourceId)
                );
    }
}
