🧪 PLAN DE TESTS COMPLET — RBAC MULTI-TENANT
🔹 NIVEAU 1 — TESTS REPOSITORY (FONDATION)

🎯 Objectif : vérifier que la base + JPA se comportent exactement comme prévu.
👉 Aucun service, aucune logique.

1️⃣ TenantRepository ------------(ok)

créer un tenant

retrouver par id

vérifier unicité (code / name)

suppression tenant → pas de cascade involontaire

2️⃣ AppUserRepository ------------(ok)

créer un user

findByEmail

findByUsername

existsByEmail

existsByUsername

activationToken lookup

3️⃣ ResourceRepository

créer resource racine

créer resource enfant

vérifier parent / children

récupérer l’arbre (si méthode custom)

suppression contrôlée (pas de delete sauvage)

4️⃣ RoleRepository

find role par id

vérifier rôles système (ADMIN_TENANT, ADMIN_GLOBAL, etc.)

5️⃣ PermissionRepository

récupérer permissions par nom

mapping rôle → permissions

6️⃣ UserRoleResourceRepository (CRITIQUE)

assigner user + role + resource

empêcher doublon

récupérer rôles d’un user sur une resource

récupérer permissions effectives

7️⃣ AuditLogRepository

insertion log

récupération par user

récupération par resource

ordre chronologique

🔹 NIVEAU 2 — TESTS SERVICE (LOGIQUE MÉTIER)

🎯 Objectif : tester le RBAC réel, pas la base.

8️⃣ TenantService ------------(ok)

créer tenant → crée resource racine

assignation automatique ADMIN_TENANT

interdiction création tenant sans droits

audit log généré

9️⃣ ResourceService 

créer resource enfant autorisé

refuser création sans permission

hiérarchie correcte

propagation des droits (si prévue)

audit log

🔟 UserService ------------(ok)

créer utilisateur

activer / désactiver

assigner rôle sur resource (autorisé)

refuser assignation sans permission

plusieurs rôles sur une même resource

audit log sur assignation

1️⃣1️⃣ AuthorizationService (LE PLUS IMPORTANT)

Tests purs, isolés.

user a permission directe → OK

user hérite via resource parent → OK

user sans rôle → REFUS

user avec rôle mais mauvaise resource → REFUS

ADMIN_TENANT override → OK

ADMIN_GLOBAL override total → OK

👉 Ces tests doivent être rapides et nombreux.

1️⃣2️⃣ RoleAssignmentService / Controller (si séparé)

assign role → OK

remove role → OK

refuser auto-promotion

refuser assignation cross-tenant

audit log

🔹 NIVEAU 3 — TESTS API (CONTROLLERS)

🎯 Objectif : vérifier que l’API expose correctement la logique, sans faille.

1️⃣3️⃣ AuthController

login valide

login invalide

compte non activé

forgot password (email existant / non existant)

reset password token valide

reset password token expiré

1️⃣4️⃣ TenantController ------------(ok)

créer tenant (autorisé)

refuser création (non autorisé)

lister tenants visibles

1️⃣5️⃣ ResourceController

GET /resources/tree

créer resource enfant

refuser accès hors scope

1️⃣6️⃣ UserController ------------(ok)

créer user

activer / désactiver

lister users selon droits

1️⃣7️⃣ RoleAssignmentController

assign role (autorisé)

refuser assign role

remove role

tentative escalade → REFUS

🔹 NIVEAU 4 — TESTS SÉCURITÉ (NON NÉGOCIABLE)

🎯 Objectif : attaquer ton système avant les autres.

accès API sans token → 401

token invalide → 401

token expiré → 401

user A agit sur resource B → REFUS

cross-tenant access → BLOQUÉ

double submit (idempotence)

🔹 NIVEAU 5 — TESTS DE COHÉRENCE GLOBALE

🎯 Objectif : valider le système complet.

scénario réel :

créer tenant

créer users

assigner rôles

créer resources

vérifier permissions finales

audit log complet du scénario

rollback transaction en cas d’erreur

🧠 ORDRE STRICT D’EXÉCUTION

1️⃣ Repositories
2️⃣ AuthorizationService
3️⃣ TenantService
4️⃣ ResourceService
5️⃣ UserService
6️⃣ Controllers
7️⃣ Sécurité