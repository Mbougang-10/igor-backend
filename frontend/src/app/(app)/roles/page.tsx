'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Shield, Loader2, CheckCircle, XCircle, Plus } from 'lucide-react';
import { api } from '@/services/api';
import CreateRoleModal from '@/components/CreateRoleModal';

interface Permission {
  id: number;
  name: string;
  description: string;
}

interface Role {
  id: number;
  name: string;
  permissions: Permission[];
}

export default function RolesPage() {
  const [roles, setRoles] = useState<Role[]>([]);
  const [allPermissions, setAllPermissions] = useState<Permission[]>([]);
  const [loading, setLoading] = useState(true);
  const [createRoleModalOpen, setCreateRoleModalOpen] = useState(false);

  useEffect(() => {
    fetchRolesAndPermissions();
  }, []);

  async function fetchRolesAndPermissions() {
    try {
      setLoading(true);
      const rolesResponse = await api.get<Role[]>('/api/roles');
      setRoles(rolesResponse.data);

      // Extraire toutes les permissions uniques de tous les rôles
      const permissionsSet = new Set<string>();
      const permissionsMap = new Map<string, Permission>();

      rolesResponse.data.forEach((role) => {
        role.permissions.forEach((perm) => {
          if (!permissionsSet.has(perm.name)) {
            permissionsSet.add(perm.name);
            permissionsMap.set(perm.name, perm);
          }
        });
      });

      setAllPermissions(Array.from(permissionsMap.values()));
    } catch (err) {
      console.error('Erreur lors du chargement des rôles:', err);
    } finally {
      setLoading(false);
    }
  }

  const hasPermission = (role: Role, permissionName: string): boolean => {
    return role.permissions.some((p) => p.name === permissionName);
  };

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Rôles & Permissions</h1>
          <p className="text-gray-600 mt-2">
            Vue d'ensemble du système RBAC (Role-Based Access Control)
          </p>
        </div>
        <Button
          onClick={() => setCreateRoleModalOpen(true)}
          className="bg-purple-600 hover:bg-purple-700"
        >
          <Plus className="h-5 w-5 mr-2" />
          Créer un rôle
        </Button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="border-l-4 border-purple-600">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Total Rôles
            </CardTitle>
            <Shield className="h-5 w-5 text-purple-600" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-gray-900">{roles.length}</div>
            <p className="text-xs text-gray-500 mt-1">Rôles système</p>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-blue-600">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Total Permissions
            </CardTitle>
            <Shield className="h-5 w-5 text-blue-600" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-gray-900">{allPermissions.length}</div>
            <p className="text-xs text-gray-500 mt-1">Permissions disponibles</p>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-green-600">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Moyenne
            </CardTitle>
            <Shield className="h-5 w-5 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-gray-900">
              {roles.length > 0
                ? Math.round(
                  roles.reduce((sum, role) => sum + role.permissions.length, 0) / roles.length
                )
                : 0}
            </div>
            <p className="text-xs text-gray-500 mt-1">Permissions par rôle</p>
          </CardContent>
        </Card>
      </div>

      {/* Permissions Matrix */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-3">
            <Shield className="h-6 w-6" />
            Matrice des permissions
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-64 font-semibold">Permission</TableHead>
                  {roles.map((role) => (
                    <TableHead key={role.id} className="text-center font-semibold">
                      <div className="flex flex-col items-center gap-1">
                        <Shield className="h-4 w-4" />
                        <span>{role.name}</span>
                      </div>
                    </TableHead>
                  ))}
                </TableRow>
              </TableHeader>
              <TableBody>
                {allPermissions.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={roles.length + 1} className="text-center py-8 text-gray-500">
                      Aucune permission trouvée
                    </TableCell>
                  </TableRow>
                ) : (
                  allPermissions.map((permission) => (
                    <TableRow key={permission.id}>
                      <TableCell className="font-medium">
                        <div>
                          <p className="font-semibold">{permission.name}</p>
                          {permission.description && (
                            <p className="text-sm text-gray-500">{permission.description}</p>
                          )}
                        </div>
                      </TableCell>
                      {roles.map((role) => (
                        <TableCell key={role.id} className="text-center">
                          {hasPermission(role, permission.name) ? (
                            <div className="flex justify-center">
                              <CheckCircle className="h-5 w-5 text-green-600" />
                            </div>
                          ) : (
                            <div className="flex justify-center">
                              <XCircle className="h-5 w-5 text-gray-300" />
                            </div>
                          )}
                        </TableCell>
                      ))}
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>

      {/* Roles Details */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {roles.map((role) => (
          <Card key={role.id} className="border-l-4 border-purple-600">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Shield className="h-5 w-5" />
                {role.name}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <p className="text-sm text-gray-600 font-semibold">
                  {role.permissions.length} permission(s)
                </p>
                <div className="flex flex-wrap gap-2">
                  {role.permissions.map((perm) => (
                    <span
                      key={perm.id}
                      className="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full font-medium"
                    >
                      {perm.name}
                    </span>
                  ))}
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <CreateRoleModal
        open={createRoleModalOpen}
        onOpenChange={setCreateRoleModalOpen}
        onRoleCreated={fetchRolesAndPermissions}
      />
    </div>
  );
}