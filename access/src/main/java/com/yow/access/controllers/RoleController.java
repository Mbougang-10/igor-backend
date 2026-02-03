package com.yow.access.controllers;

import com.yow.access.dto.CreateRoleRequest;
import com.yow.access.entities.Permission;
import com.yow.access.entities.Role;
import com.yow.access.repositories.PermissionRepository;
import com.yow.access.repositories.RoleRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleController(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@Valid @RequestBody CreateRoleRequest request) {
        // Vérifier si le rôle existe déjà
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Un rôle avec ce nom existe déjà");
        }

        // Trouver le prochain ID disponible
        Short nextId = (short) (roleRepository.findAll().stream()
                .mapToInt(Role::getId)
                .max()
                .orElse(0) + 1);

        // Créer le rôle
        Role role = new Role();
        role.setId(nextId);
        role.setName(request.getName());
        role.setScope(request.getScope() != null ? request.getScope() : "TENANT");

        // Assigner les permissions si fournies
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = new HashSet<>();
            for (Short permId : request.getPermissionIds()) {
                Permission perm = permissionRepository.findById(permId)
                        .orElseThrow(() -> new IllegalArgumentException("Permission introuvable: " + permId));
                permissions.add(perm);
            }
            role.setPermissions(permissions);
        }

        roleRepository.save(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }
}
