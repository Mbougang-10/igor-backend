import Sidebar from '@/components/ui/Sidebar';
import { verifyAuth } from '@/lib/auth-utils';

export default function AppLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  // TODO: Ajouter vérification d'authentification ici
  // const isAuthenticated = verifyAuth();

  return (
    <div className="flex h-screen bg-gray-100">
      <Sidebar />
      <main className="flex-1 overflow-y-auto p-8">
        {children}
      </main>
    </div>
  );
}