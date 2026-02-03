// src/app/(app)/departments/page.tsx
'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Building2, Plus, Loader2 } from 'lucide-react';
import CreateDepartmentModal from '@/components/CreateDepartmentModal';
import HierarchyTree from '@/components/HierarchyTree';
import SuperAdminDashboard from '@/components/dashboards/SuperAdminDashboard';

interface Department {
  id: string;
  name: string;
}

export default function DepartmentsPage() {
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedParent, setSelectedParent] = useState<Department | null>(null);
  const [isSuperAdmin, setIsSuperAdmin] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Vérification du Super Admin (basé sur email pour ce MVP)
    const email = localStorage.getItem('user_email');
    if (email === 'admin@example.com') {
      setIsSuperAdmin(true);
    }
    setLoading(false);
  }, []);

  const handleCreateDepartment = () => {
    setSelectedParent(null); // Département principal
    setModalOpen(true);
  };

  const handleCreateSubDepartment = (parent: Department) => {
    setSelectedParent(parent);
    setModalOpen(true);
  };

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
      </div>
    );
  }

  // Si Super Admin, on affiche la vue globale des organisations
  if (isSuperAdmin) {
    return <SuperAdminDashboard />;
  }

  // Sinon, vue standard pour Tenant Admin (Hiérarchie)
  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Départements</h1>
          <p className="text-gray-600 mt-2">
            Gérez la structure hiérarchique de votre organisation
          </p>
        </div>
        <Button
          size="lg"
          onClick={handleCreateDepartment}
          className="bg-blue-600 hover:bg-blue-700"
        >
          <Plus className="h-5 w-5 mr-2" />
          Créer un département
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-3">
            <Building2 className="h-6 w-6" />
            Hiérarchie actuelle
          </CardTitle>
        </CardHeader>
        <CardContent>
          {/* L'arbre avec création de sous-département */}
          <HierarchyTree onCreateSubDepartment={handleCreateSubDepartment} />
        </CardContent>
      </Card>

      {/* Modal unique — s'adapte au parent sélectionné */}
      <CreateDepartmentModal
        open={modalOpen}
        onOpenChange={setModalOpen}
        parentDepartment={selectedParent}
      />
    </div>
  );
}