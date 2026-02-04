# 📝 Guide d'Inscription Utilisateur

## Vue d'Ensemble

YowAccess propose maintenant **deux modes d'inscription** distincts :

### 1. **Inscription Simple** (Nouveau ✨)
- Créer un compte utilisateur personnel
- Aucune organisation créée automatiquement
- L'utilisateur peut ensuite :
  - Créer sa propre organisation
  - Être invité à rejoindre une organisation existante

### 2. **Inscription avec Organisation** (Existant)
- Créer un compte ET une organisation en une seule étape
- L'utilisateur devient automatiquement **TENANT_ADMIN** de l'organisation
- Accès immédiat aux fonctionnalités d'administration

---

## 🆕 Inscription Simple

### Endpoint Backend
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

### Réponse
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john@example.com",
  "username": "johndoe",
  "roles": []
}
```

### Caractéristiques
- ✅ **Validation** : Email unique, username unique (min 3 caractères), mot de passe min 8 caractères
- ✅ **Connexion automatique** : Token JWT retourné immédiatement
- ✅ **Aucun rôle** : L'utilisateur n'a aucun rôle par défaut (`roles: []`)
- ✅ **Compte activé** : Pas besoin de validation par email

### Page Frontend
**URL** : `/register`

**Fonctionnalités** :
- Formulaire avec username, email, password, confirmPassword
- Validation côté client (correspondance des mots de passe, longueur minimale)
- Redirection automatique vers `/dashboard` après inscription
- Liens vers :
  - Page de connexion (`/login`)
  - Création d'organisation (`/login?tab=create-org`)

---

## 🏢 Inscription avec Organisation

### Endpoint Backend
```http
POST /api/auth/register-tenant
Content-Type: application/json

{
  "email": "admin@acme.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "organizationName": "ACME Corporation"
}
```

### Réponse
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "admin@acme.com",
  "username": "admin@acme.com",
  "roles": ["TENANT_ADMIN"]
}
```

### Caractéristiques
- ✅ **Création simultanée** : Utilisateur + Tenant + Resource ROOT
- ✅ **Rôle TENANT_ADMIN** : Assigné automatiquement sur la resource ROOT
- ✅ **Code tenant unique** : Généré à partir du nom de l'organisation
- ✅ **Accès immédiat** : L'utilisateur peut gérer son organisation

### Page Frontend
**URL** : `/login?tab=create-org` (onglet dans la page de login)

---

## 🎯 Parcours Utilisateur

### Scénario 1 : Inscription Simple → Créer une Organisation
```
1. Utilisateur visite /register
2. Remplit le formulaire (username, email, password)
3. Compte créé → Redirection vers /dashboard
4. Dashboard affiche : "Créer une organisation" ou "Attendre une invitation"
5. Utilisateur clique sur "Créer mon organisation"
6. Redirection vers /login?tab=create-org
7. Remplit les informations de l'organisation
8. Organisation créée → Utilisateur devient TENANT_ADMIN
```

### Scénario 2 : Inscription Simple → Attendre une Invitation
```
1. Utilisateur visite /register
2. Remplit le formulaire
3. Compte créé → Redirection vers /dashboard
4. Dashboard affiche : "En attente d'invitation"
5. Un TENANT_ADMIN d'une organisation existante invite l'utilisateur
6. Utilisateur reçoit un email (fonctionnalité à implémenter)
7. Utilisateur accepte l'invitation
8. Rôle assigné → Accès à l'organisation
```

### Scénario 3 : Inscription Directe avec Organisation
```
1. Utilisateur visite /login
2. Clique sur l'onglet "Créer une organisation"
3. Remplit le formulaire complet (email, password, firstName, lastName, organizationName)
4. Compte + Organisation créés simultanément
5. Utilisateur devient TENANT_ADMIN
6. Redirection vers /dashboard avec accès complet
```

---

## 🔐 Sécurité et Validation

### Backend (AuthService.registerUser)
```java
// Vérifications
- Email unique (userRepository.existsByEmail)
- Username unique (userRepository.existsByUsername)
- Mot de passe hashé (BCrypt)

// Création
- enabled = true
- accountActivated = true
- mustChangePassword = false
- roles = [] (aucun rôle)
```

### Frontend (/register)
```typescript
// Validations
- Username : min 3 caractères
- Email : format valide
- Password : min 8 caractères
- ConfirmPassword : doit correspondre à password

// Stockage après inscription
localStorage.setItem('access_token', token);
localStorage.setItem('user_id', userId);
localStorage.setItem('user_email', email);
localStorage.setItem('user_name', username);
localStorage.setItem('userRole', 'user'); // Pas de rôle spécifique
```

---

## 📊 États de l'Utilisateur

### Utilisateur Sans Organisation
```json
{
  "userId": "...",
  "email": "john@example.com",
  "username": "johndoe",
  "roles": [],
  "tenants": []
}
```

**Dashboard affiché** : `UserDashboard.tsx`
- Message : "En attente d'organisation"
- Boutons :
  - "Créer mon organisation" (actif)
  - "En attente d'invitation" (désactivé)

### Utilisateur avec Organisation (TENANT_ADMIN)
```json
{
  "userId": "...",
  "email": "admin@acme.com",
  "username": "admin@acme.com",
  "roles": ["TENANT_ADMIN"],
  "tenants": ["ACME_CORPORATION"]
}
```

**Dashboard affiché** : `TenantAdminDashboard.tsx`
- Gestion des utilisateurs
- Gestion des départements
- Gestion des rôles
- Statistiques de l'organisation

### Super Admin
```json
{
  "userId": "...",
  "email": "admin@example.com",
  "username": "admin",
  "roles": ["ADMIN"],
  "tenants": ["SYSTEM"]
}
```

**Dashboard affiché** : `SuperAdminDashboard.tsx`
- Vue globale de tous les tenants
- Statistiques système
- Gestion multi-tenant

---

## 🚀 Prochaines Étapes (À Implémenter)

### 1. Système d'Invitation
- [ ] Endpoint `POST /api/invitations` (créer une invitation)
- [ ] Endpoint `POST /api/invitations/{token}/accept` (accepter une invitation)
- [ ] Email d'invitation avec lien d'activation
- [ ] Page `/invitations/accept?token=...`

### 2. Gestion des Utilisateurs Sans Organisation
- [ ] Liste des utilisateurs "orphelins" pour les TENANT_ADMIN
- [ ] Fonction "Inviter un utilisateur existant"
- [ ] Recherche d'utilisateurs par email

### 3. Multi-Tenant pour un Utilisateur
- [ ] Un utilisateur peut appartenir à plusieurs organisations
- [ ] Sélecteur d'organisation dans la sidebar
- [ ] Contexte tenant dans le JWT ou en session

---

## 📝 Exemples de Code

### Appel API Frontend (Inscription Simple)
```typescript
const response = await api.post('/api/auth/register', {
  username: 'johndoe',
  email: 'john@example.com',
  password: 'SecurePass123!'
});

const { token, userId, email, username, roles } = response.data;

// Stocker les informations
localStorage.setItem('access_token', token);
localStorage.setItem('userRole', 'user'); // Pas de rôle spécifique

// Rediriger
router.push('/dashboard');
```

### Vérification du Rôle (Dashboard)
```typescript
const role = localStorage.getItem('userRole');

if (role === 'super_admin') {
  return <SuperAdminDashboard />;
} else if (role === 'tenant_admin') {
  return <TenantAdminDashboard />;
} else {
  return <UserDashboard />; // Utilisateur sans organisation
}
```

---

## ✅ Checklist de Test

### Inscription Simple
- [ ] Créer un compte avec username, email, password
- [ ] Vérifier que l'email est unique
- [ ] Vérifier que le username est unique
- [ ] Vérifier la validation du mot de passe (min 8 caractères)
- [ ] Vérifier la correspondance password/confirmPassword
- [ ] Vérifier la connexion automatique après inscription
- [ ] Vérifier la redirection vers /dashboard
- [ ] Vérifier que le UserDashboard s'affiche correctement

### Inscription avec Organisation
- [ ] Créer un compte + organisation
- [ ] Vérifier que le tenant est créé
- [ ] Vérifier que la resource ROOT est créée
- [ ] Vérifier que le rôle TENANT_ADMIN est assigné
- [ ] Vérifier l'accès au TenantAdminDashboard

### Navigation
- [ ] Lien "Créer un compte gratuit" sur /login fonctionne
- [ ] Lien "Déjà un compte ? Se connecter" sur /register fonctionne
- [ ] Lien "Créer une organisation" sur /register fonctionne
- [ ] Bouton "Créer mon organisation" sur UserDashboard fonctionne

---

## 🎨 Design

### Page /register
- Gradient de fond (slate-50 → blue-50 → slate-100)
- Icône ShieldCheck avec gradient (blue-600 → purple-600)
- Formulaire avec icônes (User, Mail, Lock)
- Validation en temps réel
- Message de succès avec animation
- Liens vers login et création d'organisation

### UserDashboard
- 2 cartes principales :
  - "Créer une Organisation" (bordure bleue)
  - "Rejoindre une Organisation" (bordure violette)
- Carte d'informations du compte
- Statut "En attente d'organisation" (badge jaune)
- Liens vers documentation et support

---

Cette fonctionnalité permet une **flexibilité maximale** pour l'onboarding des utilisateurs tout en maintenant la **sécurité** et l'**isolation multi-tenant** de YowAccess.
