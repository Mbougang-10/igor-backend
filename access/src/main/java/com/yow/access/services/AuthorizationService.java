package com.yow.access.services;

import com.yow.access.entities.Resource;
import com.yow.access.entities.UserRoleResource;
import com.yow.access.exceptions.AccessDeniedException;
import com.yow.access.repositories.ResourceRepository;
import com.yow.access.repositories.UserRoleResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuthorizationService {

    private final UserRoleResourceRepository urrRepository;
    private final ResourceRepository resourceRepository;

    public AuthorizationService(
            UserRoleResourceRepository urrRepository,
            ResourceRepository resourceRepository
    ) {
        this.urrRepository = urrRepository;
        this.resourceRepository = resourceRepository;
    }

    /**
     * RBAC check on a specific resource (hierarchy-aware).
     */
    public void checkPermission(
            UUID userId,
            UUID resourceId,
            String permissionName
    ) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalStateException("Resource not found"));

        if (!hasPermission(userId, permissionName, resource)) {
            throw new AccessDeniedException("Permission denied: " + permissionName);
        }
    }

    /**
     * RBAC check without resource scope (SYSTEM / GLOBAL).
     */
    public void checkGlobalPermission(
            UUID userId,
            String permissionName
    ) {
        boolean allowed =
                urrRepository.findAllByUserId(userId)
                        .stream()
                        .anyMatch(urr ->
                                urr.getRole()
                                        .getPermissions()
                                        .stream()
                                        .anyMatch(p -> p.getName().equals(permissionName))
                        );

        if (!allowed) {
            throw new AccessDeniedException("Permission denied: " + permissionName);
        }
    }

    /* ===== INTERNAL RBAC ENGINE ===== */
    public boolean hasPermission(
            UUID userId,
            String permissionName,
            Resource target
    ) {
        List<UserRoleResource> bindings =
                urrRepository.findAllByUserId(userId);

        Resource current = target;

        while (current != null) {
            for (UserRoleResource urr : bindings) {

                if (!urr.getResource().getId().equals(current.getId())) {
                    continue;
                }

                boolean allowed =
                        urr.getRole()
                                .getPermissions()
                                .stream()
                                .anyMatch(p -> p.getName().equals(permissionName));

                if (allowed) {
                    return true;
                }
            }
            current = current.getParent();
        }
        return false;
    }
}
