'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import {
    ArrowLeft,
    Loader2,
    Users,
    Building2,
    TrendingUp,
    Activity,
    BarChart3,
} from 'lucide-react';
import { api } from '@/services/api';

interface Tenant {
    id: string;
    name: string;
    code: string;
}

interface TenantStats {
    userCount: number;
    resourceCount: number;
}

export default function TenantStatsPage() {
    const params = useParams();
    const router = useRouter();
    const tenantId = params?.tenantId as string;

    const [tenant, setTenant] = useState<Tenant | null>(null);
    const [stats, setStats] = useState<TenantStats | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (tenantId) {
            fetchData();
        }
    }, [tenantId]);

    async function fetchData() {
        try {
            setLoading(true);
            const [tenantRes, statsRes] = await Promise.all([
                api.get<Tenant>(`/api/tenants/${tenantId}`),
                api.get<TenantStats>(`/api/tenants/${tenantId}/stats`),
            ]);

            setTenant(tenantRes.data);
            setStats(statsRes.data);
        } catch (err) {
            console.error('Erreur chargement stats:', err);
        } finally {
            setLoading(false);
        }
    }

    if (loading) {
        return (
            <div className="flex h-screen items-center justify-center">
                <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
            </div>
        );
    }

    if (!tenant || !stats) {
        return <div>Données introuvables</div>;
    }

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center gap-4">
                <Button variant="outline" onClick={() => router.back()}>
                    <ArrowLeft className="h-4 w-4 mr-2" />
                    Retour
                </Button>
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">
                        Statistiques - {tenant.name}
                    </h1>
                    <p className="text-gray-600 mt-1">Analyse détaillée de l'organisation</p>
                </div>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <Card className="border-l-4 border-blue-600">
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium text-gray-600">
                            Utilisateurs
                        </CardTitle>
                        <Users className="h-5 w-5 text-blue-600" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-3xl font-bold text-gray-900">{stats.userCount}</div>
                        <p className="text-xs text-gray-500 mt-1">Membres actifs</p>
                    </CardContent>
                </Card>

                <Card className="border-l-4 border-green-600">
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium text-gray-600">
                            Ressources
                        </CardTitle>
                        <Building2 className="h-5 w-5 text-green-600" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-3xl font-bold text-gray-900">{stats.resourceCount}</div>
                        <p className="text-xs text-gray-500 mt-1">Départements et sous-départements</p>
                    </CardContent>
                </Card>

                <Card className="border-l-4 border-purple-600">
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium text-gray-600">
                            Ratio
                        </CardTitle>
                        <TrendingUp className="h-5 w-5 text-purple-600" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-3xl font-bold text-gray-900">
                            {stats.resourceCount > 0
                                ? (stats.userCount / stats.resourceCount).toFixed(1)
                                : '0'}
                        </div>
                        <p className="text-xs text-gray-500 mt-1">Utilisateurs par ressource</p>
                    </CardContent>
                </Card>

                <Card className="border-l-4 border-orange-600">
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium text-gray-600">
                            Activité
                        </CardTitle>
                        <Activity className="h-5 w-5 text-orange-600" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-3xl font-bold text-gray-900">
                            {stats.userCount > 0 ? 'Active' : 'Inactive'}
                        </div>
                        <p className="text-xs text-gray-500 mt-1">État de l'organisation</p>
                    </CardContent>
                </Card>
            </div>

            {/* Detailed Stats */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <BarChart3 className="h-5 w-5" />
                            Répartition des utilisateurs
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="space-y-4">
                            <div className="flex items-center justify-between p-4 bg-blue-50 rounded-lg">
                                <span className="font-medium">Total utilisateurs</span>
                                <span className="text-2xl font-bold text-blue-600">{stats.userCount}</span>
                            </div>
                            <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                                <span className="font-medium">Moyenne par ressource</span>
                                <span className="text-2xl font-bold text-gray-600">
                                    {stats.resourceCount > 0
                                        ? (stats.userCount / stats.resourceCount).toFixed(1)
                                        : '0'}
                                </span>
                            </div>
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <Building2 className="h-5 w-5" />
                            Structure organisationnelle
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="space-y-4">
                            <div className="flex items-center justify-between p-4 bg-green-50 rounded-lg">
                                <span className="font-medium">Total ressources</span>
                                <span className="text-2xl font-bold text-green-600">
                                    {stats.resourceCount}
                                </span>
                            </div>
                            <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                                <span className="font-medium">Profondeur estimée</span>
                                <span className="text-2xl font-bold text-gray-600">
                                    {stats.resourceCount > 0 ? Math.ceil(Math.log2(stats.resourceCount + 1)) : 0}
                                </span>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
