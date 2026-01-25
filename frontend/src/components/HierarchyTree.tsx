// src/components/HierarchyTree.tsx
'use client';

import { useState, useEffect } from 'react';
import { Building2, Users, Plus, Loader2, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { api } from '@/services/api';

// Interface pour les ressources du backend
interface ResourceTree {
  id: string;
  name: string;
  type: string;
  children: ResourceTree[];
}

// Interface pour les tenants
interface Tenant {
  id: string;
  name: string;
  code: string;
}

// Interface pour l'affichage (compatible avec l'ancienne structure)
export interface Department {
  id: string;
  name: string;
  description: string;
  managerName: string;
  children: Department[];
}

interface HierarchyTreeProps {
  onCreateSubDepartment: (parent: Department) => void;
}

// Convertir ResourceTree en Department pour l'affichage
function resourceToDepartment(resource: ResourceTree): Department {
  return {
    id: resource.id,
    name: resource.name,
    description: resource.type || 'Ressource',
    managerName: '-',
    children: resource.children?.map(resourceToDepartment) || [],
  };
}

function DepartmentNode({
  dept,
  level = 0,
  onCreateSubDepartment
}: {
  dept: Department;
  level?: number;
  onCreateSubDepartment: (parent: Department) => void;
}) {
  return (
    <div className={`${level > 0 ? 'ml-8 border-l-2 border-gray-200 pl-6' : ''}`}>
      <div className="flex items-center justify-between py-4 px-6 bg-white rounded-lg shadow-sm hover:shadow-md transition">
        <div className="flex items-center gap-4">
          <Building2 className="h-8 w-8 text-blue-600" />
          <div>
            <h3 className="text-lg font-semibold">{dept.name}</h3>
            <p className="text-sm text-gray-600">{dept.description}</p>
            {dept.managerName !== '-' && (
              <p className="text-sm text-gray-500 mt-1 flex items-center gap-2">
                <Users className="h-4 w-4" />
                Responsable : <span className="font-medium">{dept.managerName}</span>
              </p>
            )}
          </div>
        </div>
        <Button
          size="sm"
          variant="outline"
          onClick={() => onCreateSubDepartment(dept)}
        >
          <Plus className="h-4 w-4 mr-2" />
          Sous-département
        </Button>
      </div>

      {dept.children.length > 0 && (
        <div className="mt-4">
          {dept.children.map((child) => (
            <DepartmentNode
              key={child.id}
              dept={child}
              level={level + 1}
              onCreateSubDepartment={onCreateSubDepartment}
            />
          ))}
        </div>
      )}
    </div>
  );
}

export default function HierarchyTree({ onCreateSubDepartment }: HierarchyTreeProps) {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchData() {
      try {
        setLoading(true);
        setError(null);

        // 1. Récupérer la liste des tenants accessibles
        const tenantsResponse = await api.get<Tenant[]>('/api/tenants');
        const tenants = tenantsResponse.data;

        if (tenants.length === 0) {
          setDepartments([]);
          return;
        }

        // 2. Pour le premier tenant, récupérer l'arbre des ressources
        const tenantId = tenants[0].id;
        const resourcesResponse = await api.get<ResourceTree[]>(`/api/resources/tenant/${tenantId}`);
        const resources = resourcesResponse.data;

        // 3. Convertir les ressources en départements
        const depts = resources.map(resourceToDepartment);
        setDepartments(depts);

      } catch (err: any) {
        console.error('Erreur lors du chargement:', err);
        setError(err.message || 'Erreur lors du chargement des données');
      } finally {
        setLoading(false);
      }
    }

    fetchData();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
        <span className="ml-2 text-gray-600">Chargement...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-12">
        <AlertCircle className="h-16 w-16 text-red-400 mx-auto mb-4" />
        <p className="text-xl text-red-600">{error}</p>
      </div>
    );
  }

  if (departments.length === 0) {
    return (
      <div className="text-center py-12">
        <Building2 className="h-16 w-16 text-gray-400 mx-auto mb-4" />
        <p className="text-xl text-gray-600">Aucun département créé</p>
        <p className="text-sm text-gray-500 mt-2">
          Créez votre premier département pour commencer
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {departments.map((dept) => (
        <DepartmentNode
          key={dept.id}
          dept={dept}
          onCreateSubDepartment={onCreateSubDepartment}
        />
      ))}
    </div>
  );
}
