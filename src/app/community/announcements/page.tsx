// src/app/community/announcements/page.tsx
'use client';

import { useState } from 'react';
import { useRole } from '@/hooks/useUserRole';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import CreateAnnouncementModal from '@/components/CreateAnnouncementModal';

const mockAnnouncements = [
  {
    id: '1',
    title: 'Réunion d\'équipe hebdomadaire',
    content: 'N\'oubliez pas la réunion d\'équipe ce vendredi à 10h. Nous ferons le point sur les projets en cours et les objectifs du mois.',
    author: 'Marie Dupont',
    department: 'Ressources Humaines',
    date: '28 décembre 2025',
    important: true,
  },
  {
    id: '2',
    title: 'Nouvelles guidelines de télétravail',
    content: 'Les nouvelles règles de télétravail ont été mises à jour. Merci de prendre connaissance du document joint.',
    author: 'Jean Martin',
    department: 'Direction',
    date: '27 décembre 2025',
    important: false,
  },
  {
    id: '3',
    title: 'Joyeuses fêtes !',
    content: 'Toute l\'équipe vous souhaite de joyeuses fêtes de fin d\'année. Le bureau sera fermé du 24 décembre au 2 janvier.',
    author: 'Adam Johnson',
    department: 'Direction',
    date: '23 décembre 2025',
    important: true,
  },
];

export default function AnnouncementsPage() {
  const role = useRole();
  const canCreate = role === 'manager' || role === 'admin';

  const [modalOpen, setModalOpen] = useState(false);

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-4xl font-bold text-gray-900">Annonces</h1>
          <p className="text-xl text-gray-600 mt-4">
            Restez informé des dernières nouvelles de votre organisation
          </p>
        </div>

        {/* Bouton visible seulement pour manager et admin */}
        {canCreate && (
          <Button 
            onClick={() => setModalOpen(true)}
            className="bg-blue-600 hover:bg-blue-700"
          >
            <Plus className="h-5 w-5 mr-2" />
            Nouvelle annonce
          </Button>
        )}
      </div>

      {/* Liste des annonces */}
      <div className="space-y-6">
        {mockAnnouncements.map((announcement) => (
          <Card 
            key={announcement.id} 
            className={announcement.important ? 'border-l-4 border-l-red-500' : ''}
          >
            <CardHeader>
              <CardTitle className="text-xl">
                {announcement.important && <span className="text-red-600 mr-2">⚡</span>}
                {announcement.title}
              </CardTitle>
              <div className="flex items-center gap-4 mt-2 text-sm text-gray-600">
                <span>{announcement.author}</span>
                <span>•</span>
                <span>{announcement.department}</span>
                <span>•</span>
                <span>{announcement.date}</span>
              </div>
            </CardHeader>
            <CardContent>
              <p className="text-gray-700 leading-relaxed">
                {announcement.content}
              </p>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Le modal "Nouvelle annonce" */}
      <CreateAnnouncementModal 
        open={modalOpen} 
        onOpenChange={setModalOpen} 
      />
    </div>
  );
}