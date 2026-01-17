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

export default function LoginPage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);

  // Mock signup for admin + organization creation
  const handleSignup = () => {
    setIsLoading(true);

    // Mock: create organization and admin
    localStorage.setItem('userRole', 'admin');
    localStorage.setItem('organizationName', 'New Organization');

    // Simulate API call
    setTimeout(() => {
      alert('Organisation créée avec succès ! Vous êtes l\'administrateur.');
      router.push('/dashboard');
    }, 1000);
  };

  // Mock login with role selection (for testing)
  const handleLogin = (role: 'admin' | 'manager' | 'member') => {
    setIsLoading(true);
    localStorage.setItem('userRole', role);
    setTimeout(() => {
      if (role === 'admin') {
        router.push('/dashboard');
      } else {
        router.push('/community');
      }
    }, 1000);
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
          <Tabs defaultValue="signin" className="w-full">
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="signin">Se connecter</TabsTrigger>
              <TabsTrigger value="signup">Créer une organisation</TabsTrigger>
            </TabsList>

            {/* Sign In Tab */}
            <TabsContent value="signin" className="space-y-6 mt-6">
              <div className="grid gap-4">
                <div>
                  <Label htmlFor="email-login">Email</Label>
                  <Input id="email-login" type="email" placeholder="admin@exemple.com" required />
                </div>
                <div>
                  <Label htmlFor="password-login">Mot de passe</Label>
                  <Input id="password-login" type="password" required />
                </div>
              </div>

              {/* Mock role buttons for testing */}
              <div className="grid gap-3">
                <Button onClick={() => handleLogin('admin')} className="bg-red-600 hover:bg-red-700">
                  Connexion Admin
                </Button>
                <Button onClick={() => handleLogin('manager')} className="bg-blue-600 hover:bg-blue-700">
                  Connexion Manager
                </Button>
                <Button onClick={() => handleLogin('member')} className="bg-green-600 hover:bg-green-700">
                  Connexion Membre
                </Button>
              </div>
            </TabsContent>

            {/* Signup Tab - Admin creates organization */}
            <TabsContent value="signup" className="space-y-6 mt-6">
              <div className="text-center mb-4">
                <p className="text-sm text-gray-600">
                  Créez votre organisation — vous serez l'administrateur principal
                </p>
              </div>

              <div className="grid gap-4">
                <div>
                  <Label htmlFor="org-name">Nom de l'organisation</Label>
                  <Input id="org-name" placeholder="Ma Super Entreprise" required />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="first-name">Prénom</Label>
                    <Input id="first-name" placeholder="Jean" required />
                  </div>
                  <div>
                    <Label htmlFor="last-name">Nom</Label>
                    <Input id="last-name" placeholder="Dupont" required />
                  </div>
                </div>

                <div>
                  <Label htmlFor="email-signup">Email</Label>
                  <Input id="email-signup" type="email" placeholder="jean.dupont@exemple.com" required />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="password-signup">Mot de passe</Label>
                    <Input id="password-signup" type="password" required />
                  </div>
                  <div>
                    <Label htmlFor="confirm-password">Confirmer</Label>
                    <Input id="confirm-password" type="password" required />
                  </div>
                </div>

                <div>
                  <Label htmlFor="logo">Logo de l'organisation (facultatif)</Label>
                  <Input id="logo" type="file" accept="image/*" />
                </div>
              </div>

              <Button 
                onClick={handleSignup} 
                className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700"
                disabled={isLoading}
              >
                Créer mon organisation
              </Button>
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
}