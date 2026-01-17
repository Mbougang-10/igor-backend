package com.yow.access.controllers;

import com.yow.access.dto.CreateResourceRequest;
import com.yow.access.dto.ResourceTreeResponse;
import com.yow.access.entities.Resource;
import com.yow.access.services.ResourceService;
import com.yow.access.config.security.context.AuthenticatedUserContext;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
     * CREATE CHILD RESOURCE
     */
    @PostMapping
    public ResponseEntity<Resource> createResource(
            @Valid @RequestBody CreateResourceRequest request
    ) {
        UUID userId = userContext.getUserId();

        Resource resource =
                resourceService.createChildResource(
                        userId,
                        request.getParentResourceId(),
                        request.getName(),
                        request.getType()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resource);
    }

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


}
