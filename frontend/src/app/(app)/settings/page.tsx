// src/app/(app)/settings/page.tsx
'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Settings, Building2, Globe, Bell } from 'lucide-react';

export default function SettingsPage() {
  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Paramètres</h1>
        <p className="text-gray-600 mt-2">
          Gérez les paramètres généraux de votre organisation
        </p>
      </div>

      {/* Informations de l'organisation */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-3">
            <Building2 className="h-6 w-6" />
            Organisation
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="grid gap-2">
            <Label htmlFor="org-name">Nom de l'organisation</Label>
            <Input id="org-name" defaultValue="Hello Org" />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="description">Description</Label>
            <Textarea 
              id="description" 
              defaultValue="Plateforme de gestion communautaire multi-tenant"
              rows={3}
            />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="logo">Logo de l'organisation</Label>
            <div className="flex items-center gap-4">
              <div className="w-24 h-24 bg-gray-200 border-2 border-dashed rounded-xl flex items-center justify-center">
                <Building2 className="h-12 w-12 text-gray-400" />
              </div>
              <Button variant="outline">
                Changer le logo
              </Button>
            </div>
          </div>

          <div className="pt-4">
            <Button className="bg-blue-600 hover:bg-blue-700">
              Enregistrer les modifications
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Paramètres généraux */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-3">
            <Settings className="h-6 w-6" />
            Paramètres généraux
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="grid gap-2">
            <Label htmlFor="language">Langue de l'interface</Label>
            <select className="w-full px-3 py-2 border rounded-md">
              <option>Français</option>
              <option>English</option>
            </select>
          </div>

          <div className="grid gap-2">
            <Label>Notifications</Label>
            <div className="space-y-3">
              <label className="flex items-center gap-3">
                <input type="checkbox" defaultChecked className="w-4 h-4" />
                <span>Email pour les nouveaux utilisateurs</span>
              </label>
              <label className="flex items-center gap-3">
                <input type="checkbox" defaultChecked className="w-4 h-4" />
                <span>Notifications pour les changements de rôle</span>
              </label>
            </div>
          </div>

          <div className="pt-4">
            <Button variant="outline">
              Réinitialiser les paramètres
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Statistiques */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-3">
            <Globe className="h-6 w-6" />
            Statistiques de l'organisation
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-6">
            <div>
              <p className="text-sm text-gray-600">Date de création</p>
              <p className="text-xl font-semibold">15 octobre 2025</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Utilisateurs actifs</p>
              <p className="text-xl font-semibold">4</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Départements</p>
              <p className="text-xl font-semibold">3</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Rôles personnalisés</p>
              <p className="text-xl font-semibold">0</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}