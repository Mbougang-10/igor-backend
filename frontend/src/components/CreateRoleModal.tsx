// src/components/CreateRoleModal.tsx
'use client';

import { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Checkbox } from '@/components/ui/checkbox';
import { Button } from '@/components/ui/button';

const allPermissions = [
  { key: 'create_department', label: 'Créer un département' },
  { key: 'edit_department', label: 'Modifier un département' },
  { key: 'delete_department', label: 'Supprimer un département' },
  { key: 'create_user', label: 'Créer un utilisateur' },
  { key: 'edit_user', label: 'Modifier un utilisateur' },
  { key: 'delete_user', label: 'Supprimer un utilisateur' },
  { key: 'view_stats', label: 'Voir les statistiques' },
  { key: 'manage_roles', label: 'Gérer les rôles et permissions' },
];

interface CreateRoleModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export default function CreateRoleModal({ open, onOpenChange }: CreateRoleModalProps) {
  const [roleName, setRoleName] = useState('');
  const [selectedPermissions, setSelectedPermissions] = useState<string[]>([]);

  const togglePermission = (key: string) => {
    setSelectedPermissions(prev =>
      prev.includes(key)
        ? prev.filter(p => p !== key)
        : [...prev, key]
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>Créer un rôle personnalisé</DialogTitle>
          <DialogDescription>
            Définissez un nouveau rôle et choisissez ses permissions.
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-6 py-4">
          <div className="grid gap-2">
            <Label htmlFor="roleName">Nom du rôle</Label>
            <Input 
              id="roleName" 
              placeholder="ex: Responsable RH, Modérateur, Consultant..." 
              value={roleName}
              onChange={(e) => setRoleName(e.target.value)}
            />
          </div>

          <div className="grid gap-4">
            <Label>Permissions</Label>
            <div className="grid grid-cols-2 gap-4 max-h-96 overflow-y-auto p-4 border rounded-lg">
              {allPermissions.map((perm) => (
                <div key={perm.key} className="flex items-center space-x-2">
                  <Checkbox 
                    id={perm.key}
                    checked={selectedPermissions.includes(perm.key)}
                    onCheckedChange={() => togglePermission(perm.key)}
                  />
                  <label
                    htmlFor={perm.key}
                    className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                  >
                    {perm.label}
                  </label>
                </div>
              ))}
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Annuler
          </Button>
          <Button onClick={() => {
            if (roleName && selectedPermissions.length > 0) {
              alert(`Rôle "${roleName}" créé avec ${selectedPermissions.length} permissions !`);
              onOpenChange(false);
            }
          }}>
            Créer le rôle
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}