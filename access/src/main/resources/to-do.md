##  ROADMAP COMPLÈTE — PROJET RBAC MULTI-TENANT
#  ÉTAT INITIAL (LÀ OÙ TU ES) ----------(OK)

✅ Base PostgreSQL créée

✅ Liquibase exécuté

✅ Schéma stable

❌ Aucun code métier encore

 C’est exactement le bon moment pour coder.

# PHASE 1 — SOCLE DOMAIN (ENTITÉS JPA) ---------(OK)

🎯 Objectif : représenter fidèlement la base, sans logique métier.

📌 Quand ?

➡️ Juste après Liquibase, avant tout service ou controller.

📌 Pourquoi ?

Les services reposent sur le modèle

Les repositories dépendent des entités

Toute erreur ici se propage partout

🔹 Entités à créer (dans cet ordre)

Tenant -----------(ok)

AppUser-----------(ok)

Resource -----------(ok)

Role -----------(ok)

Permission ------(ok)

UserRoleResource --------(ok)

AuditLog ----------(ok)

👉 Aucune logique métier, uniquement :

@Entity

@Id

relations (@ManyToOne, etc.)

⛔ Pas de @Service, pas de règles, pas de contrôles.

# PHASE 2 — REPOSITORIES (ACCÈS DONNÉES) ----------------(OK)

🎯 Objectif : lire / écrire proprement, sans logique métier.

📌 Quand ?

➡️ Dès que les entités sont stables.

📌 Pourquoi ?

Les services ne parlent jamais à l’EntityManager

Les règles métier doivent rester testables

🔹 Repositories à créer

TenantRepository

UserRepository

ResourceRepository

UserRoleResourceRepository

RoleRepository

PermissionRepository

AuditLogRepository

📌 À ce stade :

méthodes simples (findById, existsBy…)

aucun @Transactional complexe

# PHASE 3 — SERVICES MÉTIER (LOGIQUE FORTE)

🎯 Objectif : appliquer les règles RBAC, pas juste persister.

📌 Quand ?

➡️ Quand entités + repositories sont terminés.

📌 Pourquoi ?

Le RBAC est une logique métier

Les controllers ne doivent rien décider

🔹 3.1 — TenantService (POINT DE DÉPART OBLIGATOIRE) ----(ok)
Pourquoi commencer par lui ?

Parce que tout part du tenant.

Responsabilités :

Créer un tenant

Créer la resource racine associée

Assigner le ADMIN_TENANT initial

👉 C’est ici que ta phrase prend sens :

une resource = un tenant

📌 À FAIRE MAINTENANT, avant toute API publique.

🔹 3.2 — ResourceService ----------(ok)

Créer des ressources enfants

Gérer la hiérarchie

Vérifier les permissions via RBAC

🔹 3.3 — UserService ---------------------(ok)

Créer un utilisateur

Activer / désactiver

Assigner des rôles via RBAC

🔹 3.4 — AuthorizationService (CLÉ)  --------------(ok)

🎯 Le cerveau du système.

Responsabilité unique :

“Cet utilisateur a-t-il le droit de faire X sur Y ?”

Appelé par tous les autres services

Basé sur user_role_resource

Aucun accès HTTP ici

# PHASE 4 — CONTROLLERS (API)

🎯 Objectif : exposer, pas décider.

📌 Quand ?

➡️ Uniquement quand les services sont prêts

📌 Pourquoi ?

Un controller mal conçu = faille de sécurité

Toute logique ici est une dette

🔹 Controllers à créer

TenantController ----------(ok)

UserController -----------(ok)

ResourceController --------(ok)

RoleAssignmentController

AuthController (plus tard)

📌 Ils font :

validation DTO

appel service

mapping réponse

⛔ Ils ne font PAS :

vérification RBAC

règles métier

requêtes SQL

🧪 PHASE 5 — TESTS (À NE PAS SAUTER)

🎯 Objectif : prouver que le RBAC est solide

Ordre :

Tests repository

Tests service

Tests sécurité (cas interdits)

🧠 VUE D’ENSEMBLE (TIMELINE)
Liquibase ✅
↓
Entités JPA
↓
Repositories
↓
TenantService (création tenant + resource racine)
↓
RBAC core (UserRoleResource, AuthorizationService)
↓
Controllers REST
↓
Tests & sécurité

# POINT CLÉ À COMPRENDRE (IMPORTANT)

👉 Tu ne crées PAS tout en même temps.

Entités → structure

Repositories → accès

Services → règles

Controllers → exposition

C’est cette discipline qui fait la différence entre :

un projet qui “marche”

un projet architecturé

🚀 CE QUE JE TE CONSEILLE MAINTENANT

👉 Prochaine action concrète
Créer les entités JPA uniquement, une par une.

Quand tu veux, on commence par :

Tenant

Resource (avec resource racine)

ou UserRoleResource (cœur RBAC)

Dis-moi par quoi tu veux démarrer.