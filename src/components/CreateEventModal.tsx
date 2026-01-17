// src/components/CreateEventModal.tsx
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
import { Button } from '@/components/ui/button';

interface CreateEventModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export default function CreateEventModal({
  open,
  onOpenChange,
}: CreateEventModalProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[525px]">
        <DialogHeader>
          <DialogTitle>Nouvel événement</DialogTitle>
          <DialogDescription>
            Créez un événement visible par tous les membres de l'organisation.
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="title">Titre de l'événement</Label>
            <Input id="title" placeholder="ex: Afterwork, Formation sécurité..." />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="grid gap-2">
              <Label htmlFor="date">Date</Label>
              <Input id="date" type="date" />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="time">Heure</Label>
              <Input id="time" type="time" />
            </div>
          </div>

          <div className="grid gap-2">
            <Label htmlFor="location">Lieu</Label>
            <Input id="location" placeholder="ex: Salle A, En ligne (Zoom)..." />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="description">Description</Label>
            <Textarea 
              id="description" 
              placeholder="Décrivez l'événement en détail..."
              rows={4}
            />
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Annuler
          </Button>
          <Button onClick={() => {
            alert('Événement créé avec succès !');
            onOpenChange(false);
          }}>
            Créer l'événement
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}