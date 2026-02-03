package com.yow.access.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a security role.
 *
 * Roles are static and predefined.
 *
 * Author: Alan Tchapda
 * Date: 2025-12-30
 */
@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@Table(name = "role")
public class Role {

    @Id
    @Column(name = "id", nullable = false)
    private Short id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "scope", nullable = false, length = 20)
    private String scope;

    @ManyToMany
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    // JPA only
    public Role() {
    }

    // Getters & Setters




    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
}

