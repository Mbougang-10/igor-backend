// src/components/CreatePermissionModal.tsx
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
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';

interface CreatePermissionModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onCreate: (permission: { key: string; label: string }) => void;
}

export default function CreatePermissionModal({
  open,
  onOpenChange,
  onCreate,
}: CreatePermissionModalProps) {
  const [key, setKey] = useState('');
  const [label, setLabel] = useState('');
  const [description, setDescription] = useState('');

  const handleCreate = () => {
    if (key.trim() && label.trim()) {
      onCreate({
        key: key.trim().toLowerCase().replace(/\s+/g, '_'),
        label: label.trim(),
      });
      // Reset form
      setKey('');
      setLabel('');
      setDescription('');
      onOpenChange(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[525px]">
        <DialogHeader>
          <DialogTitle>Créer une nouvelle permission</DialogTitle>
          <DialogDescription>
            Ajoutez une permission personnalisée qui pourra être assignée aux rôles.
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="label">Nom affiché</Label>
            <Input
              id="label"
              placeholder="ex: Valider les congés"
              value={label}
              onChange={(e) => setLabel(e.target.value)}
            />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="key">Clé technique</Label>
            <Input
              id="key"
              placeholder="ex: validate_leave_requests"
              value={key}
              onChange={(e) => setKey(e.target.value)}
            />
            <p className="text-sm text-gray-500">
              Lettres minuscules, chiffres et underscores uniquement. Générez-la à partir du nom si vous voulez.
            </p>
          </div>

          <div className="grid gap-2">
            <Label htmlFor="description">Description (facultatif)</Label>
            <Textarea
              id="description"
              placeholder="Décrivez à quoi sert cette permission..."
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Annuler
          </Button>
          <Button onClick={handleCreate}>
            Créer la permission
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}