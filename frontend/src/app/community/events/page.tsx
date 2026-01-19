// src/app/community/events/page.tsx
'use client';

import { useState } from 'react';
import { useRole } from '@/hooks/useUserRole';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import CreateEventModal from '@/components/CreateEventModal';

const mockEvents = [
  {
    id: '1',
    title: 'Afterwork d\'équipe',
    date: '20 janvier 2026',
    time: '18h30',
    location: 'Bar Le Central',
    description: 'Moment convivial pour commencer l\'année',
  },
  {
    id: '2',
    title: 'Formation sécurité',
    date: '10 janvier 2026',
    time: '14h00 - 16h00',
    location: 'Salle de réunion A',
    description: 'Formation obligatoire',
  },
];

export default function EventsPage() {
  const role = useRole();
  const canCreate = role === 'manager' || role === 'admin';

  const [modalOpen, setModalOpen] = useState(false);

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-4xl font-bold text-gray-900">Événements</h1>
          <p className="text-xl text-gray-600 mt-4">
            Découvrez les prochains événements de votre organisation
          </p>
        </div>

        {canCreate && (
          <Button 
            onClick={() => setModalOpen(true)}
            className="bg-blue-600 hover:bg-blue-700"
          >
            <Plus className="h-5 w-5 mr-2" />
            Nouvel événement
          </Button>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {mockEvents.map((event) => (
          <Card key={event.id} className="hover:shadow-lg transition">
            <CardHeader>
              <CardTitle className="text-xl">{event.title}</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-gray-700"><strong>Date :</strong> {event.date}</p>
              <p className="text-gray-700"><strong>Heure :</strong> {event.time}</p>
              <p className="text-gray-700"><strong>Lieu :</strong> {event.location}</p>
              <p className="text-gray-600 mt-4">{event.description}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      <CreateEventModal open={modalOpen} onOpenChange={setModalOpen} />
    </div>
  );
}