// src/app/community/page.tsx
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Bell, Calendar, FileText, Users } from 'lucide-react';
import Link from 'next/link';

export default function CommunityHome() {
  return (
    <div className="space-y-12">
      <div className="text-center py-12">
        <h1 className="text-5xl font-bold text-gray-900 mb-4">
          Bienvenue dans votre communauté !
        </h1>
        <p className="text-xl text-gray-600 max-w-3xl mx-auto">
          Restez connecté avec votre équipe : annonces importantes, événements à venir, documents utiles et annuaire des membres.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
        <Link href="/community/announcements">
          <Card className="hover:shadow-xl transition-shadow cursor-pointer">
            <CardHeader>
              <CardTitle className="flex items-center gap-4">
                <div className="p-3 bg-blue-100 rounded-lg">
                  <Bell className="h-8 w-8 text-blue-600" />
                </div>
                <div>
                  <p className="text-2xl font-bold">5</p>
                  <p className="text-gray-600">Annonces</p>
                </div>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-gray-500">3 nouvelles cette semaine</p>
            </CardContent>
          </Card>
        </Link>

        <Link href="/community/events">
          <Card className="hover:shadow-xl transition-shadow cursor-pointer">
            <CardHeader>
              <CardTitle className="flex items-center gap-4">
                <div className="p-3 bg-green-100 rounded-lg">
                  <Calendar className="h-8 w-8 text-green-600" />
                </div>
                <div>
                  <p className="text-2xl font-bold">2</p>
                  <p className="text-gray-600">Événements</p>
                </div>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-gray-500">Prochains événements</p>
            </CardContent>
          </Card>
        </Link>

        <Link href="/community/documents">
          <Card className="hover:shadow-xl transition-shadow cursor-pointer">
            <CardHeader>
              <CardTitle className="flex items-center gap-4">
                <div className="p-3 bg-purple-100 rounded-lg">
                  <FileText className="h-8 w-8 text-purple-600" />
                </div>
                <div>
                  <p className="text-2xl font-bold">24</p>
                  <p className="text-gray-600">Documents</p>
                </div>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-gray-500">Ressources partagées</p>
            </CardContent>
          </Card>
        </Link>

        <Link href="/community/members">
          <Card className="hover:shadow-xl transition-shadow cursor-pointer">
            <CardHeader>
              <CardTitle className="flex items-center gap-4">
                <div className="p-3 bg-orange-100 rounded-lg">
                  <Users className="h-8 w-8 text-orange-600" />
                </div>
                <div>
                  <p className="text-2xl font-bold">48</p>
                  <p className="text-gray-600">Membres</p>
                </div>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-gray-500">Dans l'organisation</p>
            </CardContent>
          </Card>
        </Link>
      </div>
    </div>
  );
}