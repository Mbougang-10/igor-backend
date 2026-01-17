// src/app/(app)/roles/page.tsx
'use client';

import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Shield, Plus, Key } from 'lucide-react';
import { Checkbox } from '@/components/ui/checkbox';
import CreateRoleModal from '@/components/CreateRoleModal';
import CreatePermissionModal from '@/components/CreatePermissionModal';

interface Permission {
  key: string;
  label: string;
}

interface Role {
  name: string;
  permissions: string[];
}

export default function RolesPage() {
  const [roles, setRoles] = useState<Role[]>([
    { name: 'Administrateur', permissions: ['create_department', 'edit_department', 'delete_department', 'create_user', 'edit_user', 'delete_user', 'view_stats', 'manage_roles'] },
    { name: 'Manager de département', permissions: ['create_department', 'edit_department', 'create_user', 'edit_user', 'view_stats'] },
    { name: 'Membre', permissions: ['view_stats'] },
  ]);

  const [permissions, setPermissions] = useState<Permission[]>([
    { key: 'create_department', label: 'Créer un département' },
    { key: 'edit_department', label: 'Modifier un département' },
    { key: 'delete_department', label: 'Supprimer un département' },
    { key: 'create_user', label: 'Créer un utilisateur' },
    { key: 'edit_user', label: 'Modifier un utilisateur' },
    { key: 'delete_user', label: 'Supprimer un utilisateur' },
    { key: 'view_stats', label: 'Voir les statistiques' },
    { key: 'manage_roles', label: 'Gérer les rôles et permissions' },
  ]);

  const [roleModalOpen, setRoleModalOpen] = useState(false);
  const [permissionModalOpen, setPermissionModalOpen] = useState(false);

  const handleCreatePermission = (newPerm: { key: string; label: string }) => {
    setPermissions([...permissions, newPerm]);
  };

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Rôles & Permissions</h1>
          <p className="text-gray-600 mt-2">
            Définissez les droits d'accès et créez des permissions personnalisées
          </p>
        </div>
        <div className="flex gap-4">
          <Button 
            onClick={() => setPermissionModalOpen(true)}
            variant="outline"
            className="border-blue-600 text-blue-600 hover:bg-blue-50"
          >
            <Key className="h-5 w-5 mr-2" />
            Créer une permission
          </Button>
          <Button 
            onClick={() => setRoleModalOpen(true)}
            className="bg-blue-600 hover:bg-blue-700"
          >
            <Plus className="h-5 w-5 mr-2" />
            Créer un rôle
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-3">
            <Shield className="h-6 w-6" />
            Matrice des permissions
          </CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-64">Permission</TableHead>
                {roles.map((role) => (
                  <TableHead key={role.name} className="text-center">
                    {role.name}
                  </TableHead>
                ))}
              </TableRow>
            </TableHeader>
            <TableBody>
              {permissions.map((perm) => (
                <TableRow key={perm.key}>
                  <TableCell className="font-medium">{perm.label}</TableCell>
                  {roles.map((role) => (
                    <TableCell key={role.name} className="text-center">
                      <Checkbox 
                        checked={role.permissions.includes(perm.key)}
                        disabled
                      />
                    </TableCell>
                  ))}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <CreateRoleModal open={roleModalOpen} onOpenChange={setRoleModalOpen} />
      <CreatePermissionModal 
        open={permissionModalOpen} 
        onOpenChange={setPermissionModalOpen} 
        onCreate={handleCreatePermission}
      />
    </div>
  );
}