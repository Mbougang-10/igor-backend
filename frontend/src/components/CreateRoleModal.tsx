'use client';

import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Checkbox } from '@/components/ui/checkbox';
import { api } from '@/services/api';
import { Loader2 } from 'lucide-react';

interface Permission {
  id: number;
  name: string;
  description: string;
}

interface CreateRoleModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onRoleCreated?: () => void;
}

export default function CreateRoleModal({
  open,
  onOpenChange,
  onRoleCreated,
}: CreateRoleModalProps) {
  const [roleName, setRoleName] = useState('');
  const [scope, setScope] = useState<'GLOBAL' | 'TENANT'>('TENANT');
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [selectedPermissions, setSelectedPermissions] = useState<number[]>([]);
  const [loading, setLoading] = useState(false);
  const [loadingPermissions, setLoadingPermissions] = useState(true);

  useEffect(() => {
    if (open) {
      fetchPermissions();
    }
  }, [open]);

  async function fetchPermissions() {
    try {
      setLoadingPermissions(true);
      const response = await api.get<Permission[]>('/api/permissions');
      setPermissions(response.data);
    } catch (err) {
      console.error('Erreur chargement permissions:', err);
    } finally {
      setLoadingPermissions(false);
    }
  }

  const handleTogglePermission = (permId: number) => {
    setSelectedPermissions((prev) =>
      prev.includes(permId)
        ? prev.filter((id) => id !== permId)
        : [...prev, permId]
    );
  };

  const handleSubmit = async () => {
    if (!roleName.trim()) {
      alert('Veuillez saisir un nom de rôle');
      return;
    }

    try {
      setLoading(true);
      await api.post('/api/roles', {
        name: roleName,
        scope: scope,
        permissionIds: selectedPermissions,
      });

      // Reset form
      setRoleName('');
      setSelectedPermissions([]);
      setScope('TENANT');

      onOpenChange(false);
      if (onRoleCreated) {
        onRoleCreated();
      }
    } catch (err: any) {
      console.error('Erreur création rôle:', err);
      alert(err?.message || 'Erreur lors de la création du rôle');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Créer un nouveau rôle</DialogTitle>
        </DialogHeader>

        <div className="space-y-6 py-4">
          {/* Role Name */}
          <div className="space-y-2">
            <Label htmlFor="roleName">Nom du rôle *</Label>
            <Input
              id="roleName"
              placeholder="Ex: MANAGER, DEVELOPER, VIEWER..."
              value={roleName}
              onChange={(e) => setRoleName(e.target.value.toUpperCase())}
            />
          </div>

          {/* Scope */}
          <div className="space-y-2">
            <Label>Portée du rôle</Label>
            <div className="flex gap-4">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  name="scope"
                  value="TENANT"
                  checked={scope === 'TENANT'}
                  onChange={() => setScope('TENANT')}
                  className="w-4 h-4"
                />
                <span>Tenant (Organisation)</span>
              </label>
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  name="scope"
                  value="GLOBAL"
                  checked={scope === 'GLOBAL'}
                  onChange={() => setScope('GLOBAL')}
                  className="w-4 h-4"
                />
                <span>Global (Système)</span>
              </label>
            </div>
          </div>

          {/* Permissions */}
          <div className="space-y-2">
            <Label>Permissions</Label>
            {loadingPermissions ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="h-6 w-6 animate-spin text-blue-600" />
              </div>
            ) : (
              <div className="border rounded-lg p-4 space-y-3 max-h-64 overflow-y-auto">
                {permissions.length === 0 ? (
                  <p className="text-sm text-gray-500">Aucune permission disponible</p>
                ) : (
                  permissions.map((perm) => (
                    <label
                      key={perm.id}
                      className="flex items-start gap-3 cursor-pointer hover:bg-gray-50 p-2 rounded"
                    >
                      <Checkbox
                        checked={selectedPermissions.includes(perm.id)}
                        onCheckedChange={() => handleTogglePermission(perm.id)}
                      />
                      <div className="flex-1">
                        <p className="font-medium text-sm">{perm.name}</p>
                        {perm.description && (
                          <p className="text-xs text-gray-500">{perm.description}</p>
                        )}
                      </div>
                    </label>
                  ))
                )}
              </div>
            )}
            <p className="text-xs text-gray-500">
              {selectedPermissions.length} permission(s) sélectionnée(s)
            </p>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Annuler
          </Button>
          <Button onClick={handleSubmit} disabled={loading}>
            {loading ? (
              <>
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                Création...
              </>
            ) : (
              'Créer le rôle'
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}