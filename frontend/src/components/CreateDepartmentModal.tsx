// src/components/CreateDepartmentModal.tsx
'use client';

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

interface Department {
  id: string;
  name: string;
}

interface CreateDepartmentModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  parentDepartment?: Department | null;
}

export default function CreateDepartmentModal({
  open,
  onOpenChange,
  parentDepartment = null,
}: CreateDepartmentModalProps) {
  const users = [
    { id: '1', name: 'Adam Johnson (vous)' },
    { id: '2', name: 'Marie Dupont' },
    { id: '3', name: 'Jean Martin' },
    { id: '4', name: 'Sophie Bernard' },
  ];

  const parentDepartments = [
    { id: '1', name: 'Ressources Humaines' },
    { id: '2', name: 'Marketing' },
    { id: '3', name: 'IT' },
  ];

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
            Ajoutez un département ou sous-département et désignez son responsable.
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="name">Nom du département</Label>
            <Input id="name" placeholder="ex: Recrutement, Support Client..." required />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              placeholder="Décrivez le rôle de ce département..."
              rows={3}
            />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="manager">Responsable du département</Label>
            <Select>
              <SelectTrigger>
                <SelectValue placeholder="Choisir un responsable" />
              </SelectTrigger>
              <SelectContent>
                {users.map((user) => (
                  <SelectItem key={user.id} value={user.id}>
                    {user.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {parentDepartment ? (
            <div className="grid gap-2">
              <Label>Département parent</Label>
              <Input value={parentDepartment.name} disabled className="bg-gray-100" />
            </div>
          ) : (
            <div className="grid gap-2">
              <Label htmlFor="parent">Département parent (facultatif)</Label>
              <Select>
                <SelectTrigger>
                  <SelectValue placeholder="Aucun (département principal)" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="none">Aucun (département principal)</SelectItem>
                  {parentDepartments.map((dept) => (
                    <SelectItem key={dept.id} value={dept.id}>
                      {dept.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Annuler
          </Button>
          <Button onClick={() => {
            alert('Département créé avec succès !');
            onOpenChange(false);
          }}>
            Créer le département
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}