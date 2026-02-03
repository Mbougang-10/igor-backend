package com.yow.access.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing a permission.
 *
 * A permission defines an atomic action that can be granted to a role.
 *
 * Author: Alan Tchapda
 * Date: 2025-12-30
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@Table(name = "permission")
public class Permission {

    @Id
    @Column(name = "id", nullable = false)
    private Short id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    // JPA only
    public Permission() {
    }

    // Getters & Setters

}

