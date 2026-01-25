// src/app/(app)/dashboard/page.tsx
'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Users, Building2, TreePine, Plus, Loader2 } from 'lucide-react';
import CreateDepartmentModal from '@/components/CreateDepartmentModal';
import HierarchyTree from '@/components/HierarchyTree';
import { api } from '@/services/api';

interface Department {
  id: string;
  name: string;
}

interface Tenant {
  id: string;
  name: string;
  code: string;
}

interface TenantStats {
  userCount: number;
  resourceCount: number;
}

export default function DashboardPage() {
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedParent, setSelectedParent] = useState<Department | null>(null);
  const [stats, setStats] = useState<TenantStats>({ userCount: 0, resourceCount: 0 });
  const [tenantName, setTenantName] = useState<string>('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchStats() {
      try {
        setLoading(true);

        // 1. Récupérer la liste des tenants accessibles
        const tenantsResponse = await api.get<Tenant[]>('/api/tenants');
        const tenants = tenantsResponse.data;

        if (tenants.length > 0) {
          const tenant = tenants[0];
          setTenantName(tenant.name);

          // 2. Récupérer les stats du tenant
          const statsResponse = await api.get<TenantStats>(`/api/tenants/${tenant.id}/stats`);
          setStats(statsResponse.data);
        }
      } catch (err) {
        console.error('Erreur lors du chargement des stats:', err);
      } finally {
        setLoading(false);
      }
    }

    fetchStats();
  }, []);

  const handleCreateDepartment = () => {
    setSelectedParent(null); // Pas de parent = département principal
    setModalOpen(true);
  };

  const handleCreateSubDepartment = (parent: Department) => {
    setSelectedParent(parent);
    setModalOpen(true);
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Tableau de bord</h1>
        <p className="text-gray-600 mt-2">
          Bienvenue dans votre espace d'administration
          {tenantName && <span className="font-medium"> - {tenantName}</span>}
        </p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="text-lg font-medium">Utilisateurs</CardTitle>
            <Users className="h-5 w-5 text-blue-600" />
          </CardHeader>
          <CardContent>
            {loading ? (
              <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
            ) : (
              <>
                <p className="text-3xl font-bold">{stats.userCount}</p>
                <p className="text-sm text-gray-600">
                  {stats.userCount <= 1 ? 'Utilisateur actif' : 'Utilisateurs actifs'}
                </p>
              </>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="text-lg font-medium">Départements</CardTitle>
            <Building2 className="h-5 w-5 text-green-600" />
          </CardHeader>
          <CardContent>
            {loading ? (
              <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
            ) : (
              <>
                <p className="text-3xl font-bold">{stats.resourceCount}</p>
                <p className="text-sm text-gray-600">
                  {stats.resourceCount <= 1 ? 'Ressource dans l\'organisation' : 'Ressources dans l\'organisation'}
                </p>
              </>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="text-lg font-medium">Hiérarchie</CardTitle>
            <TreePine className="h-5 w-5 text-purple-600" />
          </CardHeader>
          <CardContent>
            <p className="text-sm text-gray-600">Structure complète visible ci-dessous</p>
          </CardContent>
        </Card>
      </div>

      {/* Hierarchy Section avec l'arbre réel */}
      <Card className="mt-8">
        <CardHeader className="flex items-center justify-between">
          <CardTitle className="flex items-center gap-3">
            <TreePine className="h-6 w-6" />
            Hiérarchie de l'organisation
          </CardTitle>
          <Button
            size="lg"
            onClick={handleCreateDepartment}
            className="bg-blue-600 hover:bg-blue-700"
          >
            <Plus className="h-5 w-5 mr-2" />
            Créer un département
          </Button>
        </CardHeader>
        <CardContent>
          {/* L'arbre hiérarchique avec création de sous-département */}
          <HierarchyTree onCreateSubDepartment={handleCreateSubDepartment} />
        </CardContent>
      </Card>

      {/* Modal unique — s'adapte selon le parent sélectionné */}
      <CreateDepartmentModal
        open={modalOpen}
        onOpenChange={setModalOpen}
        parentDepartment={selectedParent}
      />
    </div>
  );
}
