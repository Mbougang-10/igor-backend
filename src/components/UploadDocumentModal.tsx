// src/components/UploadDocumentModal.tsx
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

interface UploadDocumentModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export default function UploadDocumentModal({
  open,
  onOpenChange,
}: UploadDocumentModalProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[525px]">
        <DialogHeader>
          <DialogTitle>Uploader un document</DialogTitle>
          <DialogDescription>
            Partagez un document avec tous les membres de l'organisation.
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="title">Titre du document</Label>
            <Input id="title" placeholder="ex: Rapport annuel 2025, Charte télétravail..." />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="file">Fichier</Label>
            <Input id="file" type="file" />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="category">Catégorie</Label>
            <Select>
              <SelectTrigger>
                <SelectValue placeholder="Choisir une catégorie" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="hr">Ressources Humaines</SelectItem>
                <SelectItem value="marketing">Marketing</SelectItem>
                <SelectItem value="it">IT</SelectItem>
                <SelectItem value="general">Général</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Annuler
          </Button>
          <Button onClick={() => {
            alert('Document uploadé avec succès !');
            onOpenChange(false);
          }}>
            Uploader
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}