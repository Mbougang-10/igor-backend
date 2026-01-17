package com.yow.access.services;

import com.yow.access.dto.ResourceTreeResponse;
import com.yow.access.entities.*;
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

    /**
     * Create a child resource under a parent resource (RBAC protected).
     */
    @Transactional
    public Resource createChildResource(
            UUID actorUserId,
            UUID parentResourceId,
            String resourceName,
            String resourceType
    ) {
        Resource parent =
                resourceRepository.findById(parentResourceId)
                        .orElseThrow(() -> new IllegalStateException("Parent resource not found"));

        // 🔐 RBAC
        try {
            authorizationService.checkPermission(
                    actorUserId,
                    parent.getId(),
                    "RESOURCE_CREATE"
            );
        } catch (AccessDeniedException ex) {

            auditLogService.log(
                    parent.getTenant(),
                    null,
                    parent,
                    "CREATE_RESOURCE",
                    "RESOURCE",
                    null,
                    "FAILURE",
                    ex.getMessage(),
                    null,
                    null
            );

            throw ex;
        }

        //  Create resource
        Resource child =
                ResourceFactory.createChildResource(
                        parent,
                        resourceName,
                        resourceType
                );

        resourceRepository.save(child);

        //  Audit SUCCESS
        auditLogService.log(
                parent.getTenant(),
                null,
                child,
                "CREATE_RESOURCE",
                "RESOURCE",
                child.getId(),
                "SUCCESS",
                "Resource created under parent " + parent.getId(),
                null,
                null
        );

        return child;
    }


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

        Resource root =
                resourceRepository.findById(rootResourceId)
                        .orElseThrow(() -> new IllegalStateException("Resource not found"));

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


}
