package com.yow.access.controllers;

import com.yow.access.config.security.context.AuthenticatedUserContext;
import com.yow.access.dto.CreateResourceRequest;
import com.yow.access.dto.MoveResourceRequest;
import com.yow.access.dto.ResourceTreeResponse;
import com.yow.access.services.ResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final AuthenticatedUserContext userContext;

    public ResourceController(
            ResourceService resourceService,
            AuthenticatedUserContext userContext
    ) {
        this.resourceService = resourceService;
        this.userContext = userContext;
    }

    /**
     * READ RESOURCE TREE (RBAC protected)
     */
    @GetMapping("/tree/{rootId}")
    public ResponseEntity<ResourceTreeResponse> getTree(
            @PathVariable UUID rootId
    ) {
        return ResponseEntity.ok(
                resourceService.getResourceTree(
                        userContext.getUserId(),
                        rootId
                )
        );
    }

    /**
     * CREATE CHILD RESOURCE (RBAC protected)
     */
    @PostMapping
    public ResponseEntity<Void> createResource(
            @Valid @RequestBody CreateResourceRequest request
    ) {
        resourceService.createChildResource(
                userContext.getUserId(),
                request.getParentResourceId(),
                request.getName(),
                request.getType()
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * DELETE RESOURCE (RBAC protected)
     */
    @DeleteMapping("/{resourceId}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable UUID resourceId
    ) {
        resourceService.deleteResource(
                userContext.getUserId(),
                resourceId
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * MOVE RESOURCE (RBAC protected)
     */
    @PatchMapping("/{resourceId}/move")
    public ResponseEntity<Void> moveResource(
            @PathVariable UUID resourceId,
            @Valid @RequestBody MoveResourceRequest request
    ) {
        resourceService.moveResource(
                userContext.getUserId(),
                resourceId,
                request.getNewParentId()
        );

        return ResponseEntity.noContent().build();
    }
}
