'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Package, Folder } from 'lucide-react';

export default function UserDashboard() {
    return (
        <div className="space-y-8">
            <div>
                <h1 className="text-3xl font-bold text-gray-900">Mon Espace</h1>
                <p className="text-gray-600 mt-2">
                    Bienvenue sur votre tableau de bord personnel.
                </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium">Mes Ressources</CardTitle>
                        <Package className="h-4 w-4 text-blue-500" />
                    </CardHeader>
                    <CardContent>
                        <p className="text-sm text-gray-500">
                            Vous avez accès à 3 ressources partagées.
                        </p>
                        <div className="mt-4 space-y-2">
                            {/* Placeholder for user specific resources */}
                            <div className="p-3 bg-gray-50 rounded border flex items-center gap-3">
                                <Folder className="h-5 w-5 text-yellow-500" />
                                <div>
                                    <p className="font-medium text-sm">Documents RH</p>
                                    <p className="text-xs text-gray-400">Accès lecture seule</p>
                                </div>
                            </div>
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Mes Tâches</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-sm text-gray-500">Aucune tâche assignée pour le moment.</p>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
