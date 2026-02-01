'use client';

import { useState } from 'react';
import { useCreateTenant } from '@/hooks/useCreateTenant';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useRouter } from 'next/navigation';

export default function CreateOrganizationTab() {
  const router = useRouter();
  const [orgName, setOrgName] = useState('');
  const { create, loading, error } = useCreateTenant();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!orgName.trim()) return;

    const result = await create(orgName);
    if (result.success) {
      // Redirection vers la page des tenants après création
      router.push('/tenants');
      router.refresh(); // Rafraîchir les données
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <Label htmlFor="org-name">Nom de l'organisation</Label>
        <Input
          id="org-name"
          value={orgName}
          onChange={(e) => setOrgName(e.target.value)}
          placeholder="Ma Super Organisation"
          required
          minLength={3}
          maxLength={150}
        />
        <p className="text-xs text-gray-500 mt-1">
          Le code sera généré automatiquement (lettres majuscules et underscores)
        </p>
      </div>

      {error && (
        <div className="p-3 bg-red-50 border border-red-200 rounded-md">
          <p className="text-red-600 text-sm">{error}</p>
        </div>
      )}

      <Button
        type="submit"
        className="w-full"
        disabled={loading || !orgName.trim()}
      >
        {loading ? 'Création en cours...' : 'Créer l\'organisation'}
      </Button>
    </form>
  );
}