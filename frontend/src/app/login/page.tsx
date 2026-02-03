// src/app/login/page.tsx
'use client';

import { useState } from 'react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { useRouter } from 'next/navigation';
import { api } from '@/services/api';

export default function LoginPage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Login form state
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  // Signup form state
  const [orgName, setOrgName] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [signupEmail, setSignupEmail] = useState('');
  const [signupPassword, setSignupPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  // Real login with backend API
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);

    try {
      const response = await api.post('/api/auth/login', {
        email: loginEmail,
        password: loginPassword,
      });

      const { token, userId, email, username, roles } = response.data;

      // Store token and user info
      localStorage.setItem('access_token', token);
      localStorage.setItem('user_id', userId);
      localStorage.setItem('user_email', email);
      localStorage.setItem('user_name', username);

      // Determine initial role context
      // Note: In a real multi-tenant app, the user might have different roles per tenant.
      // Here we set a "global" session role for the UI.
      let userRole = 'user';
      if (roles && Array.isArray(roles)) {
        if (roles.includes('ADMIN') || roles.includes('TENANT_ADMIN')) {
          userRole = 'admin';
        }
      }
      localStorage.setItem('userRole', userRole);

      // Redirect to dashboard
      router.push('/dashboard');
    } catch (err: any) {
      console.error('Login error:', err);

      const message =
        err?.response?.data?.message ||
        err?.message ||
        'Email ou mot de passe incorrect';
      setError(message);
    } finally {
      setIsLoading(false);
    }
  };

  // Signup - Create organization
  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);

    if (signupPassword !== confirmPassword) {
      setError('Les mots de passe ne correspondent pas');
      setIsLoading(false);
      return;
    }

    try {
      const response = await api.post('/api/auth/register-tenant', {
        email: signupEmail,
        password: signupPassword,
        firstName: firstName,
        lastName: lastName,
        organizationName: orgName
      });

      const { token, userId, email, username, roles } = response.data;

      // Store token and user info
      localStorage.setItem('access_token', token);
      localStorage.setItem('user_id', userId);
      localStorage.setItem('user_email', email);
      localStorage.setItem('user_name', username);

      // Set role (Admin since they created the tenant)
      let userRole = 'user';
      if (roles && Array.isArray(roles)) {
        if (roles.includes('ADMIN') || roles.includes('TENANT_ADMIN')) {
          userRole = 'admin';
        }
      }
      localStorage.setItem('userRole', userRole);

      // Redirect to dashboard
      router.push('/dashboard');
    } catch (err: any) {
      console.error('Signup error:', err);
      const message = err?.message || 'Erreur lors de la création de l\'organisation';
      setError(message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 px-4">
      <Card className="w-full max-w-lg shadow-xl">
        <CardHeader className="text-center">
          <CardTitle className="text-3xl font-bold">Gestionnaire de Communauté</CardTitle>
          <CardDescription className="text-lg mt-2">
            Plateforme multi-tenant pour organisations et équipes
          </CardDescription>
        </CardHeader>

        <CardContent>
          {error && (
            <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
              {error}
            </div>
          )}

          <Tabs defaultValue="signin" className="w-full">
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="signin">Se connecter</TabsTrigger>
              <TabsTrigger value="signup">Créer une organisation</TabsTrigger>
            </TabsList>

            {/* Sign In Tab */}
            <TabsContent value="signin" className="space-y-6 mt-6">
              <form onSubmit={handleLogin} className="space-y-4">
                <div className="grid gap-4">
                  <div>
                    <Label htmlFor="email-login">Email</Label>
                    <Input
                      id="email-login"
                      type="email"
                      placeholder="admin@example.com"
                      value={loginEmail}
                      onChange={(e) => setLoginEmail(e.target.value)}
                      required
                    />
                  </div>
                  <div>
                    <Label htmlFor="password-login">Mot de passe</Label>
                    <Input
                      id="password-login"
                      type="password"
                      placeholder="Admin123!"
                      value={loginPassword}
                      onChange={(e) => setLoginPassword(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <Button
                  type="submit"
                  className="w-full bg-blue-600 hover:bg-blue-700"
                  disabled={isLoading}
                >
                  {isLoading ? 'Connexion...' : 'Se connecter'}
                </Button>

                {/* Test credentials hint */}
                <div className="text-center text-sm text-gray-500 mt-4 p-3 bg-gray-50 rounded">
                  <p className="font-medium">Identifiants de test :</p>
                  <p>Email: admin@example.com</p>
                  <p>Mot de passe: Admin123!</p>
                </div>
              </form>
            </TabsContent>

            {/* Signup Tab - Admin creates organization */}
            <TabsContent value="signup" className="space-y-6 mt-6">
              <form onSubmit={handleSignup} className="space-y-4">
                <div className="text-center mb-4">
                  <p className="text-sm text-gray-600">
                    Créez votre organisation — vous serez l'administrateur principal
                  </p>
                </div>

                <div className="grid gap-4">
                  <div>
                    <Label htmlFor="org-name">Nom de l'organisation</Label>
                    <Input
                      id="org-name"
                      placeholder="Ma Super Entreprise"
                      value={orgName}
                      onChange={(e) => setOrgName(e.target.value)}
                      required
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <Label htmlFor="first-name">Prénom</Label>
                      <Input
                        id="first-name"
                        placeholder="Jean"
                        value={firstName}
                        onChange={(e) => setFirstName(e.target.value)}
                        required
                      />
                    </div>
                    <div>
                      <Label htmlFor="last-name">Nom</Label>
                      <Input
                        id="last-name"
                        placeholder="Dupont"
                        value={lastName}
                        onChange={(e) => setLastName(e.target.value)}
                        required
                      />
                    </div>
                  </div>

                  <div>
                    <Label htmlFor="email-signup">Email</Label>
                    <Input
                      id="email-signup"
                      type="email"
                      placeholder="jean.dupont@exemple.com"
                      value={signupEmail}
                      onChange={(e) => setSignupEmail(e.target.value)}
                      required
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <Label htmlFor="password-signup">Mot de passe</Label>
                      <Input
                        id="password-signup"
                        type="password"
                        value={signupPassword}
                        onChange={(e) => setSignupPassword(e.target.value)}
                        required
                      />
                    </div>
                    <div>
                      <Label htmlFor="confirm-password">Confirmer</Label>
                      <Input
                        id="confirm-password"
                        type="password"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        required
                      />
                    </div>
                  </div>
                </div>

                <Button
                  type="submit"
                  className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700"
                  disabled={isLoading}
                >
                  {isLoading ? 'Création...' : 'Créer mon organisation'}
                </Button>
              </form>
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
}
