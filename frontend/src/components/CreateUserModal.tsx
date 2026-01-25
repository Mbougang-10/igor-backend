// src/components/CreateUserModal.tsx
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
import { Button } from '@/components/ui/button';
import { Loader2 } from 'lucide-react';
import { api } from '@/services/api';

interface CreateUserModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export default function CreateUserModal({ open, onOpenChange }: CreateUserModalProps) {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Réinitialiser le formulaire quand la modal s'ouvre
  useEffect(() => {
    if (open) {
      setUsername('');
      setEmail('');
      setError(null);
      setSuccess(null);
    }
  }, [open]);

  const handleSubmit = async () => {
    // Validation
    if (!username.trim()) {
      setError('Le nom d\'utilisateur est requis');
      return;
    }
    if (!email.trim()) {
      setError('L\'email est requis');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);

      await api.post('/api/auth/users', {
        username: username.trim(),
        email: email.trim(),
      });

      setSuccess('Utilisateur créé avec succès. Un email d\'activation sera envoyé.');

      // Fermer après un délai et recharger
      setTimeout(() => {
        onOpenChange(false);
        window.location.reload();
      }, 2000);

    } catch (err: any) {
      console.error('Erreur lors de la création:', err);
      setError(err.message || 'Erreur lors de la création de l\'utilisateur');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[525px]">
        <DialogHeader>
          <DialogTitle>Créer un nouvel utilisateur</DialogTitle>
          <DialogDescription>
            Ajoutez un membre à votre organisation. Un email d'activation lui sera envoyé.
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-4 py-4">
          {error && (
            <div className="bg-red-50 text-red-600 p-3 rounded-md text-sm">
              {error}
            </div>
          )}

          {success && (
            <div className="bg-green-50 text-green-600 p-3 rounded-md text-sm">
              {success}
            </div>
          )}

          <div className="grid gap-2">
            <Label htmlFor="username">Nom d'utilisateur *</Label>
            <Input
              id="username"
              placeholder="marie.dupont"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="email">Email *</Label>
            <Input
              id="email"
              type="email"
              placeholder="marie.dupont@hello.org"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <p className="text-sm text-gray-500">
              L'utilisateur recevra un email pour activer son compte et définir son mot de passe.
            </p>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={submitting}>
            Annuler
          </Button>
          <Button onClick={handleSubmit} disabled={submitting || !!success}>
            {submitting ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin mr-2" />
                Création...
              </>
            ) : (
              'Créer l\'utilisateur'
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
