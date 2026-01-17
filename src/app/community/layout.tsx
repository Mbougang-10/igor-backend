// src/app/community/layout.tsx
import { Bell, Home, Calendar, FileText, Users, LogOut, Building2 } from 'lucide-react';
import Link from 'next/link';

export default function CommunityLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-8">
              <Link href="/community" className="flex items-center gap-3">
                <div className="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center">
                  <Building2 className="h-6 w-6 text-white" />
                </div>
                <h1 className="text-2xl font-bold text-gray-900">Hello Community</h1>
              </Link>

              <nav className="hidden md:flex items-center gap-6">
                <Link href="/community" className="text-gray-700 hover:text-blue-600 font-medium flex items-center gap-2">
                  <Home className="h-5 w-5" />
                  Accueil
                </Link>
                <Link href="/community/announcements" className="text-gray-700 hover:text-blue-600 font-medium flex items-center gap-2">
                  <Bell className="h-5 w-5" />
                  Annonces
                </Link>
                <Link href="/community/events" className="text-gray-700 hover:text-blue-600 font-medium flex items-center gap-2">
                  <Calendar className="h-5 w-5" />
                  Événements
                </Link>
                <Link href="/community/documents" className="text-gray-700 hover:text-blue-600 font-medium flex items-center gap-2">
                  <FileText className="h-5 w-5" />
                  Documents
                </Link>
                <Link href="/community/members" className="text-gray-700 hover:text-blue-600 font-medium flex items-center gap-2">
                  <Users className="h-5 w-5" />
                  Membres
                </Link>
              </nav>
            </div>

            <div className="flex items-center gap-4">
              <button className="relative p-2 text-gray-600 hover:text-gray-900">
                <Bell className="h-6 w-6" />
                <span className="absolute top-0 right-0 h-2 w-2 bg-red-500 rounded-full"></span>
              </button>
              <div className="flex items-center gap-3">
                <div className="text-right">
                  <p className="text-sm font-medium text-gray-900">Marie Dupont</p>
                  <p className="text-xs text-gray-500">Ressources Humaines</p>
                </div>
                <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white font-bold">
                  MD
                </div>
              </div>
              <button className="text-gray-600 hover:text-gray-900">
                <LogOut className="h-5 w-5" />
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </main>
    </div>
  );
}