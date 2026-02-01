package com.yow.access.entities;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity representing the assignment of a role to a user on a resource.
 *
 * Core RBAC table.
 *
 * Author: Alan Tchapda
 * Date: 2025-12-30
 */
@Entity
@Table(name = "user_role_resource")
public class UserRoleResource {

    @EmbeddedId
    private UserRoleResourceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("resourceId")
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    // JPA only
    public UserRoleResource() {
    }

    // Getters & Setters

    public UserRoleResourceId getId() {
        return id;
    }

    public void setId(UserRoleResourceId id) {
        this.id = id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

