// src/components/CreateDepartmentModal.tsx
'use client';

import { useState, useEffect } from 'react';
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
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { Loader2 } from 'lucide-react';
import { api } from '@/services/api';

interface Department {
  id: string;
  name: string;
}

interface ResourceTree {
  id: string;
  name: string;
  type: string;
  children: ResourceTree[];
}

interface Tenant {
  id: string;
  name: string;
}

interface CreateDepartmentModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  parentDepartment?: Department | null;
}

// Fonction pour aplatir l'arbre des ressources en liste
function flattenResources(resources: ResourceTree[], level = 0): { id: string; name: string; level: number; type: string }[] {
  const result: { id: string; name: string; level: number; type: string }[] = [];
  for (const resource of resources) {
    result.push({ id: resource.id, name: resource.name, level, type: resource.type });
    if (resource.children?.length > 0) {
      result.push(...flattenResources(resource.children, level + 1));
    }
  }
  return result;
}

export default function CreateDepartmentModal({
  open,
  onOpenChange,
  parentDepartment = null,
}: CreateDepartmentModalProps) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [selectedParentId, setSelectedParentId] = useState<string>('');
  const [resources, setResources] = useState<{ id: string; name: string; level: number; type: string }[]>([]);
  const [rootResourceId, setRootResourceId] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Charger les ressources disponibles comme parents potentiels
  useEffect(() => {
    async function fetchResources() {
      if (!open) return;

      try {
        setLoading(true);
        const tenantsResponse = await api.get<Tenant[]>('/api/tenants');
        const tenants = tenantsResponse.data;

        if (tenants.length > 0) {
          const tenantId = tenants[0].id;
          const resourcesResponse = await api.get<ResourceTree[]>(`/api/resources/tenant/${tenantId}`);
          const flatList = flattenResources(resourcesResponse.data);
          setResources(flatList);

          // Trouver et stocker la ressource ROOT
          const rootResource = flatList.find(r => r.type === 'ROOT');
          if (rootResource) {
            setRootResourceId(rootResource.id);
          }
        }
      } catch (err) {
        console.error('Erreur lors du chargement des ressources:', err);
      } finally {
        setLoading(false);
      }
    }

    fetchResources();
  }, [open]);

  // Réinitialiser le formulaire quand la modal s'ouvre/se ferme
  useEffect(() => {
    if (open) {
      setName('');
      setDescription('');
      setSelectedParentId(parentDepartment?.id || '');
      setError(null);
    }
  }, [open, parentDepartment]);

  const handleSubmit = async () => {
    // Validation
    if (!name.trim()) {
      setError('Le nom du département est requis');
      return;
    }

    // Utiliser le parent sélectionné, ou la ressource ROOT si aucun parent n'est sélectionné
    const parentId = parentDepartment?.id || selectedParentId || rootResourceId;

    if (!parentId) {
      setError('Impossible de créer le département : aucune organisation trouvée');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);

      await api.post('/api/resources', {
        parentResourceId: parentId,
        name: name.trim(),
        type: 'DEPARTMENT',
      });

      // Fermer la modal et recharger la page pour voir les changements
      onOpenChange(false);
      window.location.reload();
    } catch (err: any) {
      console.error('Erreur lors de la création:', err);
      setError(err.message || 'Erreur lors de la création du département');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[525px]">
        <DialogHeader>
          <DialogTitle>
            {parentDepartment
              ? `Créer un sous-département dans "${parentDepartment.name}"`
              : 'Créer un nouveau département'}
          </DialogTitle>
          <DialogDescription>
            Ajoutez un département ou sous-département à votre organisation.
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-4 py-4">
          {error && (
            <div className="bg-red-50 text-red-600 p-3 rounded-md text-sm">
              {error}
            </div>
          )}

          <div className="grid gap-2">
            <Label htmlFor="name">Nom du département *</Label>
            <Input
              id="name"
              placeholder="ex: Recrutement, Support Client..."
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              placeholder="Décrivez le rôle de ce département..."
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          {parentDepartment ? (
            <div className="grid gap-2">
              <Label>Département parent</Label>
              <Input value={parentDepartment.name} disabled className="bg-gray-100" />
            </div>
          ) : (
            <div className="grid gap-2">
              <Label htmlFor="parent">
                Département parent {!rootResourceId && '*'}
              </Label>
              {loading ? (
                <div className="flex items-center gap-2 text-gray-500">
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Chargement...
                </div>
              ) : resources.length === 0 ? (
                <p className="text-sm text-amber-600">
                  Aucune organisation trouvée. Veuillez d'abord créer une organisation.
                </p>
              ) : (
                <>
                  <Select value={selectedParentId} onValueChange={setSelectedParentId}>
                    <SelectTrigger>
                      <SelectValue placeholder={rootResourceId ? "Racine de l'organisation (par défaut)" : "Sélectionner un parent"} />
                    </SelectTrigger>
                    <SelectContent>
                      {resources.map((resource) => (
                        <SelectItem key={resource.id} value={resource.id}>
                          {'  '.repeat(resource.level)}
                          {resource.type === 'ROOT' ? `🏢 ${resource.name}` : resource.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {rootResourceId && !selectedParentId && (
                    <p className="text-xs text-gray-500">
                      Si non sélectionné, le département sera créé à la racine de l'organisation.
                    </p>
                  )}
                </>
              )}
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={submitting}>
            Annuler
          </Button>
          <Button onClick={handleSubmit} disabled={submitting || (resources.length === 0 && !parentDepartment)}>
            {submitting ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin mr-2" />
                Création...
              </>
            ) : (
              'Créer le département'
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
