package com.yow.access.dto;

import java.util.UUID;

public class UserRoleDTO {
    private Short roleId;
    private String roleName;
    private UUID resourceId;
    private String resourceName;
    private String resourceType;

    public UserRoleDTO(Short roleId, String roleName, UUID resourceId, String resourceName, String resourceType) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
    }

    public Short getRoleId() { return roleId; }
    public String getRoleName() { return roleName; }
    public UUID getResourceId() { return resourceId; }
    public String getResourceName() { return resourceName; }
    public String getResourceType() { return resourceType; }
}
