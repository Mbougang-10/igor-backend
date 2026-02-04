# 🧪 Guide de Test - YowAccess RBAC

## 🚀 Démarrage Rapide

### 1. Démarrer le Backend
```bash
cd access
mvn spring-boot:run
```
**Port:** http://localhost:8086

### 2. Démarrer le Frontend
```bash
cd frontend
npm run dev
```
**Port:** http://localhost:3000

---

## ✅ Scénarios de Test

### 📌 **Test 1 : Super Admin - Vue Globale des Tenants**

**Objectif:** Vérifier que le Super Admin voit tous les tenants

**Étapes:**
1. Aller sur http://localhost:3000/login
2. Se connecter avec:
   - Email: `admin@example.com`
   - Mot de passe: `admin123`
3. **Vérifications:**
   - ✅ Redirection vers `/dashboard`
   - ✅ Affichage du `SuperAdminDashboard`
   - ✅ Cartes statistiques visibles (Total Organisations, Utilisateurs, Ressources)
   - ✅ Tableau des tenants avec colonnes: Nom, Code, Statut, Utilisateurs, Ressources, Date
4. Cliquer sur **"Départements"** dans la sidebar
   - ✅ Affichage du même `SuperAdminDashboard`
5. Cliquer sur **"Voir"** pour un tenant
   - ✅ Redirection vers `/tenants/[id]`
   - ✅ Affichage des détails du tenant
   - ✅ Onglets "Utilisateurs" et "Hiérarchie" fonctionnels
6. Cliquer sur **"Stats"** pour un tenant
   - ✅ Redirection vers `/tenants/[id]/stats`
   - ✅ Affichage des métriques détaillées

**Résultat attendu:** ✅ Toutes les vérifications passent

---

### 📌 **Test 2 : Création d'une Organisation (Tenant)**

**Objectif:** Créer un nouveau tenant et vérifier les droits du Tenant Admin

**Étapes:**
1. Aller sur http://localhost:3000/login
2. Cliquer sur l'onglet **"Créer une organisation"**
3. Remplir le formulaire:
   - Prénom: `Jean`
   - Nom: `Dupont`
   - Email: `jean.dupont@acme.com`
   - Mot de passe: `Password123!`
   - Confirmer: `Password123!`
   - Organisation: `ACME Corporation`
4. Cliquer sur **"Créer mon organisation"**
5. **Vérifications:**
   - ✅ Redirection vers `/dashboard`
   - ✅ Affichage du `TenantAdminDashboard`
   - ✅ Onglets "Tableau de bord" et "Utilisateurs" visibles
6. Vérifier les permissions:
   - Cliquer sur **"Utilisateurs"** (onglet)
     - ✅ Possibilité de créer un utilisateur
   - Cliquer sur **"Départements"** (sidebar)
     - ✅ Affichage de la hiérarchie du tenant
     - ✅ Bouton "Créer un département" visible
   - Cliquer sur **"Rôles & Permissions"** (sidebar)
     - ✅ Affichage de la matrice des permissions
     - ✅ Bouton "Créer un rôle" visible

**Résultat attendu:** ✅ Le Tenant Admin a tous les droits sur son organisation

---

### 📌 **Test 3 : Création d'un Nouveau Rôle**

**Objectif:** Vérifier la fonctionnalité de création de rôles personnalisés

**Étapes:**
1. Se connecter en tant que Tenant Admin (ou Super Admin)
2. Aller sur `/roles`
3. Cliquer sur **"Créer un rôle"**
4. **Vérifications du modal:**
   - ✅ Modal s'ouvre
   - ✅ Champ "Nom du rôle" visible
   - ✅ Radio buttons "Portée" (Tenant/Global) visibles
   - ✅ Liste des permissions chargée
   - ✅ Checkboxes pour sélectionner les permissions
5. Remplir le formulaire:
   - Nom: `MANAGER`
   - Portée: `Tenant`
   - Permissions: Sélectionner `USER_READ`, `USER_CREATE`, `RESOURCE_READ`
6. Cliquer sur **"Créer le rôle"**
7. **Vérifications post-création:**
   - ✅ Modal se ferme
   - ✅ Nouveau rôle "MANAGER" apparaît dans la liste
   - ✅ Carte du rôle affiche "3 permission(s)"
   - ✅ Matrice des permissions mise à jour
   - ✅ Checkmarks visibles pour les permissions sélectionnées

**Résultat attendu:** ✅ Rôle créé avec succès et visible partout

---

### 📌 **Test 4 : Gestion des Utilisateurs (Tenant Admin)**

**Objectif:** Créer un utilisateur et lui assigner un rôle

**Étapes:**
1. Se connecter en tant que Tenant Admin
2. Aller sur le dashboard → Onglet **"Utilisateurs"**
3. Cliquer sur **"Créer un utilisateur"**
4. Remplir le formulaire:
   - Nom d'utilisateur: `testuser`
   - Email: `test@acme.com`
5. Cliquer sur **"Créer"**
6. **Vérifications:**
   - ✅ Utilisateur créé
   - ✅ Statut "Inactif" (compte non activé)
   - ✅ Email d'activation envoyé (vérifier les logs backend)
7. Assigner un rôle:
   - Cliquer sur le bouton de gestion des rôles
   - Sélectionner le rôle `USER` ou `MANAGER`
   - Sélectionner la ressource (département)
   - Cliquer sur **"Assigner"**
8. **Vérifications:**
   - ✅ Rôle assigné avec succès
   - ✅ Badge du rôle visible sur l'utilisateur

**Résultat attendu:** ✅ Utilisateur créé et rôle assigné

---

### 📌 **Test 5 : Hiérarchie des Départements**

**Objectif:** Créer une structure hiérarchique de départements

**Étapes:**
1. Se connecter en tant que Tenant Admin
2. Aller sur **"Départements"**
3. Cliquer sur **"Créer un département"**
4. Créer le département racine:
   - Nom: `Direction Générale`
   - Type: `DEPARTMENT`
5. **Vérifications:**
   - ✅ Département créé
   - ✅ Visible dans l'arbre hiérarchique
6. Créer un sous-département:
   - Cliquer sur l'icône "+" à côté de "Direction Générale"
   - Nom: `Département IT`
   - Type: `DEPARTMENT`
7. **Vérifications:**
   - ✅ Sous-département créé
   - ✅ Indentation visible dans l'arbre
   - ✅ Relation parent-enfant correcte
8. Créer un autre niveau:
   - Cliquer sur "+" à côté de "Département IT"
   - Nom: `Équipe DevOps`
   - Type: `TEAM`
9. **Vérifications:**
   - ✅ 3 niveaux de hiérarchie visibles
   - ✅ Structure arborescente claire

**Résultat attendu:** ✅ Hiérarchie multi-niveaux fonctionnelle

---

### 📌 **Test 6 : Vérification des Permissions RBAC**

**Objectif:** Vérifier que les permissions sont correctement appliquées

**Étapes:**
1. Se connecter en tant que Super Admin
2. Aller sur `/roles`
3. **Vérifier la matrice:**
   - ✅ Rôle `ADMIN` : Toutes les permissions cochées
   - ✅ Rôle `TENANT_ADMIN` : Toutes les permissions cochées
   - ✅ Rôle `USER` : Permissions limitées
   - ✅ Rôle `MANAGER` (si créé) : Permissions sélectionnées uniquement
4. Vérifier les cartes de rôles:
   - ✅ Chaque carte affiche le bon nombre de permissions
   - ✅ Les badges de permissions sont corrects

**Résultat attendu:** ✅ Matrice RBAC cohérente

---

## 🔍 Vérifications Backend (Logs)

### Logs à surveiller au démarrage:

```
========================================
EXÉCUTION DU DATA INITIALIZER
========================================
```

**Vérifier:**
- ✅ Création des rôles: ADMIN, USER, TENANT_ADMIN
- ✅ Création des permissions: RESOURCE_*, USER_*, ASSIGN_ROLE, etc.
- ✅ Assignation des permissions aux rôles
- ✅ Création de l'utilisateur admin@example.com

### Logs lors de la création d'un tenant:

```
Tenant 'ACME Corporation' créé avec succès par jean.dupont@acme.com
```

**Vérifier:**
- ✅ Utilisateur admin créé
- ✅ Tenant créé avec code unique
- ✅ Ressource racine créée
- ✅ Rôle TENANT_ADMIN assigné

---

## 🐛 Problèmes Courants et Solutions

### ❌ **Problème:** SuperAdmin ne voit pas les tenants

**Solution:**
- Vérifier que l'email est bien `admin@example.com`
- Vérifier dans les logs que le rôle `ADMIN` est assigné
- Vérifier le localStorage: `userRole` doit être `admin`

### ❌ **Problème:** Tenant Admin n'a pas les permissions

**Solution:**
- Vérifier dans la DB que le rôle `TENANT_ADMIN` a toutes les permissions
- Vérifier la table `user_role_resource` pour l'assignation
- Relancer le DataInitializer si nécessaire

### ❌ **Problème:** Impossible de créer un rôle

**Solution:**
- Vérifier que l'endpoint `POST /api/roles` est accessible
- Vérifier que l'endpoint `GET /api/permissions` retourne des données
- Vérifier les logs backend pour les erreurs de validation

### ❌ **Problème:** 404 sur /dashboard

**Solution:**
- Vérifier que le localStorage contient `userRole`
- Vérifier que le composant `useEffect` s'exécute
- Vider le cache du navigateur

---

## 📊 Endpoints à Tester (Postman/cURL)

### 1. Liste des Tenants (Super Admin)
```bash
curl -X GET http://localhost:8086/api/tenants \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2. Stats d'un Tenant
```bash
curl -X GET http://localhost:8086/api/tenants/{TENANT_ID}/stats \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Liste des Rôles
```bash
curl -X GET http://localhost:8086/api/roles \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Liste des Permissions
```bash
curl -X GET http://localhost:8086/api/permissions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5. Créer un Rôle
```bash
curl -X POST http://localhost:8086/api/roles \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "VIEWER",
    "scope": "TENANT",
    "permissionIds": [11, 31]
  }'
```

---

## ✅ Checklist Complète

- [ ] Backend démarre sans erreur
- [ ] Frontend démarre sans erreur
- [ ] Super Admin peut se connecter
- [ ] Super Admin voit tous les tenants
- [ ] Création d'organisation fonctionne
- [ ] Tenant Admin a tous les droits
- [ ] Création de rôle fonctionne
- [ ] Assignation de permissions fonctionne
- [ ] Création d'utilisateur fonctionne
- [ ] Assignation de rôle à utilisateur fonctionne
- [ ] Hiérarchie de départements fonctionne
- [ ] Matrice RBAC est correcte
- [ ] Navigation entre pages fonctionne
- [ ] Tous les endpoints répondent

---

**Status:** 🎯 **Prêt pour les tests**  
**Durée estimée:** 30-45 minutes pour tous les tests
