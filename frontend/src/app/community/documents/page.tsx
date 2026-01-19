// src/app/community/documents/page.tsx
'use client';

import { useState } from 'react';
import { useRole } from '@/hooks/useUserRole';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import UploadDocumentModal from '@/components/UploadDocumentModal';

const mockDocuments = [
  { id: '1', title: 'Rapport annuel 2025.pdf', category: 'Direction', size: '5.2 MB' },
  { id: '2', title: 'Charte télétravail.pdf', category: 'Ressources Humaines', size: '1.1 MB' },
  { id: '3', title: 'Guidelines marketing 2026.docx', category: 'Marketing', size: '456 KB' },
];

export default function DocumentsPage() {
  const role = useRole();
  const canUpload = role === 'manager' || role === 'admin';

  const [modalOpen, setModalOpen] = useState(false);

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-4xl font-bold text-gray-900">Documents</h1>
          <p className="text-xl text-gray-600 mt-4">
            Accédez aux documents partagés de l'organisation
          </p>
        </div>

        {canUpload && (
          <Button 
            onClick={() => setModalOpen(true)}
            className="bg-blue-600 hover:bg-blue-700"
          >
            <Plus className="h-5 w-5 mr-2" />
            Uploader un document
          </Button>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {mockDocuments.map((doc) => (
          <Card key={doc.id} className="hover:shadow-lg transition">
            <CardHeader>
              <CardTitle className="text-lg">{doc.title}</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-gray-600">{doc.category}</p>
              <p className="text-sm text-gray-500 mt-2">{doc.size}</p>
              <Button variant="outline" className="w-full mt-4">
                Télécharger
              </Button>
            </CardContent>
          </Card>
        ))}
      </div>

      <UploadDocumentModal open={modalOpen} onOpenChange={setModalOpen} />
    </div>
  );
}