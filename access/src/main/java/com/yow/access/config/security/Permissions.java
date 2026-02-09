package com.yow.access.config.security;

public final class Permissions {
    public static final String TENANT_CREATE = "TENANT_CREATE";
    private Permissions() {}

    public static final String TENANT_READ   = "TENANT_READ";
    public static final String TENANT_LIST   = "TENANT_LIST";

    // Resource Permissions
    public static final String RESOURCE_CREATE = "RESOURCE_CREATE";
    public static final String RESOURCE_READ   = "RESOURCE_READ";
    public static final String RESOURCE_UPDATE = "RESOURCE_UPDATE";
    public static final String RESOURCE_DELETE = "RESOURCE_DELETE";
    public static final String RESOURCE_MOVE   = "RESOURCE_MOVE";

    // User/Role Permissions
    public static final String USER_CREATE     = "USER_CREATE";
    public static final String USER_READ       = "USER_READ";
    public static final String USER_UPDATE     = "USER_UPDATE";
    public static final String USER_DELETE     = "USER_DELETE";
    public static final String ASSIGN_ROLE     = "ASSIGN_ROLE";
    public static final String REMOVE_ROLE     = "REMOVE_ROLE";
}

