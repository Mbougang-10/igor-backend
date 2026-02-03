'use client';

import { useState, useEffect } from 'react';
import { Loader2 } from 'lucide-react';
import SuperAdminDashboard from '@/components/dashboards/SuperAdminDashboard';
import TenantAdminDashboard from '@/components/dashboards/TenantAdminDashboard';
import UserDashboard from '@/components/dashboards/UserDashboard';

export default function DashboardPage() {
  const [role, setRole] = useState<string | null>(null);
  const [email, setEmail] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Récupérer les données stockées côté client uniquement
    const userRole = localStorage.getItem('userRole');
    const userEmail = localStorage.getItem('user_email');

    setRole(userRole);
    setEmail(userEmail);
    setLoading(false);
  }, []);

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
      </div>
    );
  }

  const isSuperAdmin = email === 'admin@example.com';

  if (isSuperAdmin) {
    return <SuperAdminDashboard />;
  }

  if (role === 'admin') {
    return <TenantAdminDashboard />;
  }

  return <UserDashboard />;
}
