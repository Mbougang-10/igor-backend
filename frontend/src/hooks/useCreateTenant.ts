import { useState } from 'react';
import { TenantService } from '@/services/tenant.service';
import { generateTenantCode } from '@/lib/tenant-code';

export function useCreateTenant() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function create(name: string) {
    try {
      setLoading(true);
      setError(null);

      await TenantService.create({
        name,
        code: generateTenantCode(name),
      });

      // Succès - pas de redirection ici, laisser le composant gérer
      return { success: true };
    } catch (e: any) {
      const errorMessage = e?.response?.data?.message || e?.message || 'Erreur inconnue';
      setError(errorMessage);
      return { success: false, error: errorMessage };
    } finally {
      setLoading(false);
    }
  }

  return { create, loading, error };
}