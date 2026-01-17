// src/components/CreateUserModal.tsx
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
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Button } from '@/components/ui/button';

interface CreateUserModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export default function CreateUserModal({ open, onOpenChange }: CreateUserModalProps) {
  // Mock pour les rôles et départements (à remplacer par l'API plus tard)
  const roles = [
    { value: 'admin', label: 'Administrateur' },
    { value: 'manager', label: 'Manager de département' },
    { value: 'member', label: 'Membre' },
  ];

  const departments = [
    { value: 'none', label: 'Aucun (accès global)' },
    { value: '1', label: 'Ressources Humaines' },
    { value: '4', label: 'Recrutement' },
    { value: '2', label: 'Marketing' },
    { value: '3', label: 'IT' },
  ];

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[525px]">
        <DialogHeader>
          <DialogTitle>Créer un nouvel utilisateur</DialogTitle>
          <DialogDescription>
            Ajoutez un membre à votre organisation et définissez ses permissions.
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-4 py-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="grid gap-2">
              <Label htmlFor="firstName">Prénom</Label>
              <Input id="firstName" placeholder="Marie" required />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="lastName">Nom</Label>
              <Input id="lastName" placeholder="Dupont" required />
            </div>
          </div>

          <div className="grid gap-2">
            <Label htmlFor="email">Email</Label>
            <Input id="email" type="email" placeholder="marie.dupont@hello.org" required />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="grid gap-2">
              <Label htmlFor="password">Mot de passe</Label>
              <Input id="password" type="password" required />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="confirmPassword">Confirmer</Label>
              <Input id="confirmPassword" type="password" required />
            </div>
          </div>

          <div className="grid gap-2">
            <Label htmlFor="role">Rôle</Label>
            <Select>
              <SelectTrigger>
                <SelectValue placeholder="Choisir un rôle" />
              </SelectTrigger>
              <SelectContent>
                {roles.map((role) => (
                  <SelectItem key={role.value} value={role.value}>
                    {role.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="grid gap-2">
            <Label htmlFor="department">Département affecté (facultatif)</Label>
            <Select>
              <SelectTrigger>
                <SelectValue placeholder="Aucun département" />
              </SelectTrigger>
              <SelectContent>
                {departments.map((dept) => (
                  <SelectItem key={dept.value} value={dept.value}>
                    {dept.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <p className="text-sm text-gray-500">
              Si un département est choisi, l'utilisateur aura accès aux données de ce département.
            </p>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Annuler
          </Button>
          <Button onClick={() => {
            // TODO : Envoyer à l'API
            alert('Utilisateur créé avec succès !');
            onOpenChange(false);
          }}>
            Créer l'utilisateur
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}