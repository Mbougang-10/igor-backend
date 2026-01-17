// src/components/Sidebar.tsx
'use client';  // Important: this is a Client Component

import { usePathname } from 'next/navigation';
import { Home, Users, Shield, Settings, LogOut, Building2 } from 'lucide-react';
import Link from 'next/link';

export default function Sidebar() {
  const pathname = usePathname();

  const isActive = (path: string) => pathname === path;

  return (
    <div className="w-64 bg-gray-900 text-white h-screen p-6 flex flex-col">
      <div className="mb-10">
        <h1 className="text-2xl font-bold flex items-center gap-3">
          <Building2 className="h-8 w-8" />
          Hello Org
        </h1>
      </div>

      <nav className="flex-1">
        <ul className="space-y-2">
          <li>
            <Link 
              href="/dashboard" 
              className={`flex items-center gap-3 px-4 py-3 rounded-lg transition ${
                isActive('/dashboard') ? 'bg-blue-600' : 'hover:bg-gray-800'
              }`}
            >
              <Home className="h-5 w-5" />
              Tableau de bord
            </Link>
          </li>
          <li>
            <Link 
              href="/departments" 
              className={`flex items-center gap-3 px-4 py-3 rounded-lg transition ${
                isActive('/departments') ? 'bg-blue-600' : 'hover:bg-gray-800'
              }`}
            >
              <Building2 className="h-5 w-5" />
              Départements
            </Link>
          </li>
          <li>
            <Link 
              href="/users" 
              className={`flex items-center gap-3 px-4 py-3 rounded-lg transition ${
                isActive('/users') ? 'bg-blue-600' : 'hover:bg-gray-800'
              }`}
            >
              <Users className="h-5 w-5" />
              Utilisateurs
            </Link>
          </li>
          <li>
            <Link 
              href="/roles" 
              className={`flex items-center gap-3 px-4 py-3 rounded-lg transition ${
                isActive('/roles') ? 'bg-blue-600' : 'hover:bg-gray-800'
              }`}
            >
              <Shield className="h-5 w-5" />
              Rôles & Permissions
            </Link>
          </li>
          <li>
            <Link 
              href="/settings" 
              className={`flex items-center gap-3 px-4 py-3 rounded-lg transition ${
                isActive('/settings') ? 'bg-blue-600' : 'hover:bg-gray-800'
              }`}
            >
              <Settings className="h-5 w-5" />
              Paramètres
            </Link>
          </li>
        </ul>
      </nav>

      <div className="border-t border-gray-700 pt-4">
        <button className="flex items-center gap-3 px-4 py-3 rounded-lg hover:bg-gray-800 w-full text-left">
          <LogOut className="h-5 w-5" />
          Déconnexion
        </button>
      </div>
    </div>
  );
}