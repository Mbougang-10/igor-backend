// src/components/CreateAnnouncementModal.tsx
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
import { AlertCircle } from 'lucide-react';

interface CreateAnnouncementModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export default function CreateAnnouncementModal({
  open,
  onOpenChange,
}: CreateAnnouncementModalProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>Nouvelle annonce</DialogTitle>
          <DialogDescription>
            Publiez une annonce visible par tous les membres de l'organisation.
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="title">Titre de l'annonce</Label>
            <Input 
              id="title" 
              placeholder="ex: Réunion d'équipe vendredi, Nouvelles règles de télétravail..." 
            />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="content">Contenu</Label>
            <Textarea 
              id="content" 
              placeholder="Décrivez l'annonce en détail..."
              rows={6}
            />
          </div>

          <div className="flex items-center gap-3">
            <input type="checkbox" id="important" className="w-4 h-4" />
            <Label htmlFor="important" className="flex items-center gap-2 cursor-pointer">
              <AlertCircle className="h-5 w-5 text-red-600" />
              Marquer comme importante (affichée en haut avec bordure rouge)
            </Label>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Annuler
          </Button>
          <Button onClick={() => {
            alert('Annonce publiée avec succès !');
            onOpenChange(false);
          }}>
            Publier l'annonce
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}