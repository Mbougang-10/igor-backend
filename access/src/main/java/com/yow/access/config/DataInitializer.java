package com.yow.access.config;

import com.yow.access.entities.AppUser;
import com.yow.access.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@Order(1)
@Transactional
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final com.yow.access.repositories.RoleRepository roleRepository;
    private final com.yow.access.repositories.PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(
            UserRepository userRepository,
            com.yow.access.repositories.RoleRepository roleRepository,
            com.yow.access.repositories.PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("EXÉCUTION DU DATA INITIALIZER");
        log.info("========================================");

        initializeRoles();
        initializeDefaultAdmin();
        logAllUsers();

        log.info("========================================");
        log.info("DATA INITIALIZER TERMINÉ");
        log.info("========================================");
    }

    private void initializeRoles() {
        // 1. Create Roles
        createRoleIfNotFound((short) 1, "ADMIN", "GLOBAL");
        createRoleIfNotFound((short) 2, "USER", "GLOBAL");
        createRoleIfNotFound((short) 3, "TENANT_ADMIN", "TENANT");

        // 2. Create Permissions
        createPermissionIfNotFound((short) 10, "RESOURCE_CREATE", "Create child resources");
        createPermissionIfNotFound((short) 11, "RESOURCE_READ", "View resources");
        createPermissionIfNotFound((short) 12, "RESOURCE_UPDATE", "Update resources");
        createPermissionIfNotFound((short) 13, "RESOURCE_DELETE", "Delete resources");
        createPermissionIfNotFound((short) 14, "RESOURCE_MOVE", "Move resources");
        
        createPermissionIfNotFound((short) 20, "ASSIGN_ROLE", "Assign roles to users");
        createPermissionIfNotFound((short) 21, "REMOVE_ROLE", "Remove roles from users");
        
        createPermissionIfNotFound((short) 30, "USER_CREATE", "Create new users");
        createPermissionIfNotFound((short) 31, "USER_READ", "View users");
        createPermissionIfNotFound((short) 32, "USER_UPDATE", "Update users");
        createPermissionIfNotFound((short) 33, "USER_DELETE", "Delete users");

        // 3. Assign All Permissions to TENANT_ADMIN
        assignAllPermissionsToRole("TENANT_ADMIN");
        assignAllPermissionsToRole("ADMIN"); // Global admin gets everything too
    }

    private void createRoleIfNotFound(Short id, String name, String scope) {
        if (roleRepository.findByName(name).isEmpty()) {
            com.yow.access.entities.Role role = new com.yow.access.entities.Role();
            role.setId(id);
            role.setName(name);
            role.setScope(scope);
            roleRepository.save(role);
            log.info("✅ Rôle créé: {} (ID: {}, Scope: {})", name, id, scope);
        } else {
            log.info("ℹ️  Le rôle existe déjà: {}", name);
        }
    }

    private void createPermissionIfNotFound(Short id, String name, String description) {
        if (permissionRepository.findByName(name).isEmpty()) {
            com.yow.access.entities.Permission permission = new com.yow.access.entities.Permission();
            permission.setId(id);
            permission.setName(name);
            permission.setDescription(description);
            permissionRepository.save(permission);
            log.info("✅ Permission créée: {} (ID: {})", name, id);
        }
    }

    private void assignAllPermissionsToRole(String roleName) {
        roleRepository.findByName(roleName).ifPresent(role -> {
            java.util.List<com.yow.access.entities.Permission> allPermissions = permissionRepository.findAll();
            boolean changed = false;
            for (com.yow.access.entities.Permission p : allPermissions) {
                if (!role.getPermissions().contains(p)) {
                    role.getPermissions().add(p);
                    changed = true;
                }
            }
            if (changed) {
                roleRepository.save(role);
                log.info("✅ Permissions assignées au rôle: {}", roleName);
            }
        });
    }

    private void initializeDefaultAdmin() {
        // Créer UN SEUL admin avec un username unique
        String adminEmail = "admin@example.com";
        String adminUsername = "admin";

        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            AppUser admin = new AppUser();
            admin.setEmail(adminEmail);
            admin.setUsername(adminUsername);
            admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
            admin.setAccountActivated(true);
            admin.setEnabled(true);
            admin.setMustChangePassword(false);
            admin.setCreatedAt(Instant.now());

            userRepository.save(admin);

            log.info("✅ Utilisateur admin créé:");
            log.info("   Email: {}", adminEmail);
            log.info("   Username: {}", adminUsername);
            log.info("   Mot de passe: Admin123!");
        } else {
            log.info("ℹ️  L'utilisateur admin existe déjà: {}", adminEmail);
        }

        // Si vous voulez un deuxième utilisateur de test, utilisez un username différent
        String testEmail = "test@yow.com";
        String testUsername = "testuser";

        if (userRepository.findByEmail(testEmail).isEmpty()) {
            AppUser testUser = new AppUser();
            testUser.setEmail(testEmail);
            testUser.setUsername(testUsername);
            testUser.setPasswordHash(passwordEncoder.encode("Test123!"));
            testUser.setAccountActivated(true);
            testUser.setEnabled(true);
            testUser.setMustChangePassword(false);
            testUser.setCreatedAt(Instant.now());

            userRepository.save(testUser);

            log.info("✅ Utilisateur test créé:");
            log.info("   Email: {}", testEmail);
            log.info("   Username: {}", testUsername);
            log.info("   Mot de passe: Test123!");
        }
    }

    private void logAllUsers() {
        try {
            List<AppUser> allUsers = userRepository.findAll();
            log.info("Nombre total d'utilisateurs en base: {}", allUsers.size());

            if (!allUsers.isEmpty()) {
                log.info("=== LISTE DES UTILISATEURS ===");
                allUsers.forEach(user -> {
                    log.info("ID: {} | Email: {} | Username: {} | Activé: {} | Enabled: {}",
                            user.getId(),
                            user.getEmail(),
                            user.getUsername(),
                            user.isAccountActivated(),
                            user.isEnabled());
                });
                log.info("==============================");
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs: {}", e.getMessage());
        }
    }
}