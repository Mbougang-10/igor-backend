// src/app/(app)/users/page.tsx
'use client';

import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Users, Plus, Shield, User } from 'lucide-react';
import CreateUserModal from '@/components/CreateUserModal';

const mockUsers = [ /* ton tableau mock d'utilisateurs, comme avant */ ];

export default function UsersPage() {
  const [modalOpen, setModalOpen] = useState(false);

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Utilisateurs</h1>
          <p className="text-gray-600 mt-2">
            Gérez les membres de votre organisation et leurs permissions
          </p>
        </div>
        <Button
          size="lg"
          onClick={() => setModalOpen(true)}
          className="bg-blue-600 hover:bg-blue-700"
        >
          <Plus className="h-5 w-5 mr-2" />
          Créer un utilisateur
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-3">
            <Users className="h-6 w-6" />
            Liste des utilisateurs ({mockUsers.length})
          </CardTitle>
        </CardHeader>
        <CardContent>
          {/* Ta table existante reste identique */}
          <Table>
            {/* ... ton code table inchangé */}
          </Table>
        </CardContent>
      </Card>

      <CreateUserModal open={modalOpen} onOpenChange={setModalOpen} />
    </div>
  );
}