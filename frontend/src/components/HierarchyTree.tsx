// src/components/HierarchyTree.tsx
'use client';

import { Building2, Users, Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';

interface Department {
  id: string;
  name: string;
  description: string;
  managerName: string;
  children: Department[];
}

interface HierarchyTreeProps {
  onCreateSubDepartment: (parent: Department) => void;
}

const mockDepartments: Department[] = [
  {
    id: '1',
    name: 'Ressources Humaines',
    description: 'Gestion du personnel et recrutement',
    managerName: 'Marie Dupont',
    children: [
      {
        id: '4',
        name: 'Recrutement',
        description: "Processus d'embauche",
        managerName: 'Sophie Bernard',
        children: [],
      },
    ],
  },
  {
    id: '2',
    name: 'Marketing',
    description: 'Stratégie et communication',
    managerName: 'Jean Martin',
    children: [],
  },
  {
    id: '3',
    name: 'IT',
    description: 'Infrastructure et support technique',
    managerName: 'Adam Johnson',
    children: [],
  },
];

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
            <p className="text-sm text-gray-500 mt-1 flex items-center gap-2">
              <Users className="h-4 w-4" />
              Responsable : <span className="font-medium">{dept.managerName}</span>
            </p>
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
  if (mockDepartments.length === 0) {
    return (
      <div className="text-center py-12">
        <Building2 className="h-16 w-16 text-gray-400 mx-auto mb-4" />
        <p className="text-xl text-gray-600">Aucun département créé</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {mockDepartments.map((dept) => (
        <DepartmentNode 
          key={dept.id} 
          dept={dept} 
          onCreateSubDepartment={onCreateSubDepartment}
        />
      ))}
    </div>
  );
}