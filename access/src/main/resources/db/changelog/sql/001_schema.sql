-- =========================
-- EXTENSIONS
-- =========================
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- USER (IDENTITE EXPOSEE)
-- =========================
CREATE TABLE app_user (
                          id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          username        VARCHAR(100) NOT NULL UNIQUE,
                          email           VARCHAR(150) NOT NULL UNIQUE,
                          password_hash   VARCHAR(255) NOT NULL,
                          enabled         BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- TENANT (RACINE METIER)
-- =========================
CREATE TABLE tenant (
                        id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        name            VARCHAR(150) NOT NULL,
                        code            VARCHAR(50) NOT NULL UNIQUE,
                        status          VARCHAR(30),
                        created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- RESOURCE (HIERARCHIQUE, INTERNE)
-- =========================
CREATE TABLE resource (
                          id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          tenant_id       UUID NOT NULL,
                          parent_id       UUID,
                          type            VARCHAR(50) NOT NULL,
                          name            VARCHAR(150) NOT NULL,
                          path            VARCHAR(500),
                          created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_resource_tenant
                              FOREIGN KEY (tenant_id)
                                  REFERENCES tenant(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_resource_parent
                              FOREIGN KEY (parent_id)
                                  REFERENCES resource(id)
                                  ON DELETE CASCADE
);

-- =========================
-- ROLE (STATIQUE, FONCTIONNEL)
-- =========================
CREATE TABLE role (
                      id      SMALLINT PRIMARY KEY,
                      name    VARCHAR(50) NOT NULL UNIQUE,
                      scope   VARCHAR(20) NOT NULL
                          CHECK (scope IN ('GLOBAL', 'TENANT', 'RESOURCE'))
);

-- =========================
-- PERMISSION (STATIQUE)
-- =========================
CREATE TABLE permission (
                            id          SMALLINT PRIMARY KEY,
                            name        VARCHAR(100) NOT NULL UNIQUE,
                            description VARCHAR(255)
);

-- =========================
-- ROLE_PERMISSION (M:N)
-- =========================
CREATE TABLE role_permission (
                                 role_id         SMALLINT NOT NULL,
                                 permission_id   SMALLINT NOT NULL,

                                 CONSTRAINT pk_role_permission
                                     PRIMARY KEY (role_id, permission_id),

                                 CONSTRAINT fk_rp_role
                                     FOREIGN KEY (role_id)
                                         REFERENCES role(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_rp_permission
                                     FOREIGN KEY (permission_id)
                                         REFERENCES permission(id)
                                         ON DELETE CASCADE
);

-- =========================
-- USER_ROLE_RESOURCE (COEUR RBAC)
-- =========================
CREATE TABLE user_role_resource (
                                    user_id         UUID NOT NULL,
                                    role_id         SMALLINT NOT NULL,
                                    resource_id     UUID NOT NULL,
                                    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT pk_user_role_resource
                                        PRIMARY KEY (user_id, role_id, resource_id),

                                    CONSTRAINT fk_urr_user
                                        FOREIGN KEY (user_id)
                                            REFERENCES app_user(id)
                                            ON DELETE CASCADE,

                                    CONSTRAINT fk_urr_role
                                        FOREIGN KEY (role_id)
                                            REFERENCES role(id)
                                            ON DELETE CASCADE,

                                    CONSTRAINT fk_urr_resource
                                        FOREIGN KEY (resource_id)
                                            REFERENCES resource(id)
                                            ON DELETE CASCADE
);

-- =========================
-- AUDIT_LOG (TRACABILITE)
-- =========================
CREATE TABLE audit_log (
                           id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           tenant_id       UUID,
                           user_id         UUID,
                           action          VARCHAR(100) NOT NULL,
                           target_type     VARCHAR(50),
                           target_id       UUID,
                           resource_id     UUID,
                           outcome         VARCHAR(20) NOT NULL,
                           message         TEXT,
                           ip_address      VARCHAR(45),
                           user_agent      TEXT,
                           created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_audit_tenant
                               FOREIGN KEY (tenant_id)
                                   REFERENCES tenant(id),

                           CONSTRAINT fk_audit_user
                               FOREIGN KEY (user_id)
                                   REFERENCES app_user(id),

                           CONSTRAINT fk_audit_resource
                               FOREIGN KEY (resource_id)
                                   REFERENCES resource(id)
);

-- =========================
-- INDEXES (PERFORMANCE)
-- =========================
CREATE INDEX idx_resource_tenant ON resource(tenant_id);
CREATE INDEX idx_resource_parent ON resource(parent_id);
CREATE INDEX idx_resource_path ON resource(path);

CREATE INDEX idx_urr_user ON user_role_resource(user_id);
CREATE INDEX idx_urr_role ON user_role_resource(role_id);
CREATE INDEX idx_urr_resource ON user_role_resource(resource_id);

CREATE INDEX idx_audit_tenant ON audit_log(tenant_id);
CREATE INDEX idx_audit_user ON audit_log(user_id);
CREATE INDEX idx_audit_created_at ON audit_log(created_at);