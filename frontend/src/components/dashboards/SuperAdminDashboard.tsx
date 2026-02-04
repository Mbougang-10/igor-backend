'use client';

import { useState, useEffect } from 'react';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import {
    Building2,
    Users,
    Loader2,
    Eye,
    BarChart3,
    Globe,
    TrendingUp
} from 'lucide-react';
import { api } from '@/services/api';
import Link from 'next/link';

// ... (imports remain the same)

interface Tenant {
    id: string;
    name: string;
    code: string;
    status: string;
    createdAt: string;
    ownerName?: string;
    ownerEmail?: string;
}

interface TenantStats {
    userCount: number;
    resourceCount: number;
}

interface TenantWithStats extends Tenant {
    stats?: TenantStats;
}

export default function SuperAdminDashboard() {
    const [tenants, setTenants] = useState<TenantWithStats[]>([]);
    const [loading, setLoading] = useState(true);
    const [totalUsers, setTotalUsers] = useState(0);
    const [totalResources, setTotalResources] = useState(0);

    useEffect(() => {
        fetchTenants();
    }, []);

    async function fetchTenants() {
        try {
            setLoading(true);
            // Utiliser le nouvel endpoint qui retourne aussi les propriétaires
            const response = await api.get<Tenant[]>('/api/tenants/summary');
            const tenantsData = response.data;

            // Récupérer les stats pour chaque tenant
            const tenantsWithStats = await Promise.all(
                tenantsData.map(async (tenant) => {
                    try {
                        const statsResponse = await api.get<TenantStats>(
                            `/api/tenants/${tenant.id}/stats`
                        );
                        return { ...tenant, stats: statsResponse.data };
                    } catch (err) {
                        console.error(`Erreur stats pour tenant ${tenant.id}:`, err);
                        return { ...tenant, stats: { userCount: 0, resourceCount: 0 } };
                    }
                })
            );

            setTenants(tenantsWithStats);

            // Calculer les totaux
            const totalU = tenantsWithStats.reduce((sum, t) => sum + (t.stats?.userCount || 0), 0);
            const totalR = tenantsWithStats.reduce((sum, t) => sum + (t.stats?.resourceCount || 0), 0);
            setTotalUsers(totalU);
            setTotalResources(totalR);
        } catch (err: any) {
            console.error('Erreur détaillée lors du chargement des tenants:', {
                message: err.message,
                status: err.status,
                data: err.data,
                original: err
            });
            if (err.status === 403) {
                alert("Accès refusé : Vous n'avez pas les droits de Super Administrateur.");
            }
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

    return (
        <div className="space-y-8">
            {/* Header */}
            <div>
                <h1 className="text-3xl font-bold text-gray-900 border-l-4 border-purple-600 pl-4">
                    Super Administration
                </h1>
                <p className="text-gray-600 mt-2 pl-4">
                    Vue globale de toutes les organisations de la plateforme
                </p>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <Card className="border-l-4 border-purple-600 shadow-md">
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium text-gray-600">
                            Total Organisations
                        </CardTitle>
                        <Globe className="h-5 w-5 text-purple-600" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-3xl font-bold text-gray-900">{tenants.length}</div>
                        <p className="text-xs text-gray-500 mt-1">
                            {tenants.filter(t => t.status === 'ACTIVE').length} actives
                        </p>
                    </CardContent>
                </Card>

                <Card className="border-l-4 border-blue-600 shadow-md">
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium text-gray-600">
                            Total Utilisateurs
                        </CardTitle>
                        <Users className="h-5 w-5 text-blue-600" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-3xl font-bold text-gray-900">{totalUsers}</div>
                        <p className="text-xs text-gray-500 mt-1">
                            Sur toutes les organisations
                        </p>
                    </CardContent>
                </Card>

                <Card className="border-l-4 border-green-600 shadow-md">
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium text-gray-600">
                            Total Ressources
                        </CardTitle>
                        <Building2 className="h-5 w-5 text-green-600" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-3xl font-bold text-gray-900">{totalResources}</div>
                        <p className="text-xs text-gray-500 mt-1">
                            Départements et sous-départements
                        </p>
                    </CardContent>
                </Card>

                <Card className="border-l-4 border-orange-600 shadow-md">
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium text-gray-600">
                            Moyenne par Org
                        </CardTitle>
                        <TrendingUp className="h-5 w-5 text-orange-600" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-3xl font-bold text-gray-900">
                            {tenants.length > 0 ? Math.round(totalUsers / tenants.length) : 0}
                        </div>
                        <p className="text-xs text-gray-500 mt-1">
                            Utilisateurs par organisation
                        </p>
                    </CardContent>
                </Card>
            </div>

            {/* Tenants Table */}
            <Card className="shadow-lg border-none">
                <CardHeader className="bg-gradient-to-r from-gray-50 to-white">
                    <CardTitle className="flex items-center gap-3">
                        <Building2 className="h-6 w-6 text-purple-600" />
                        Liste des Organisations et Propriétaires
                    </CardTitle>
                </CardHeader>
                <CardContent className="p-0">
                    <div className="border-t">
                        <Table>
                            <TableHeader className="bg-gray-50">
                                <TableRow>
                                    <TableHead className="py-4">Organisation</TableHead>
                                    <TableHead>Propriétaire</TableHead>
                                    <TableHead className="text-center">Stats</TableHead>
                                    <TableHead>Statut</TableHead>
                                    <TableHead>Date de création</TableHead>
                                    <TableHead className="text-right pr-6">Actions</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {tenants.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={6} className="text-center py-12 text-gray-500">
                                            <div className="flex flex-col items-center gap-2">
                                                <Users className="h-8 w-8 text-gray-300" />
                                                <p>Aucune organisation trouvée</p>
                                            </div>
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    tenants.map((tenant) => (
                                        <TableRow key={tenant.id} className="hover:bg-blue-50/50 transition-colors">
                                            <TableCell className="font-medium">
                                                <div className="flex flex-col">
                                                    <div className="flex items-center gap-2 text-base text-gray-900">
                                                        <Building2 className="h-4 w-4 text-purple-600" />
                                                        {tenant.name}
                                                    </div>
                                                    <span className="text-xs text-gray-500 font-mono mt-1 ml-6 bg-gray-100 w-fit px-1.5 py-0.5 rounded">
                                                        Code: {tenant.code}
                                                    </span>
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                {tenant.ownerName ? (
                                                    <div className="flex flex-col">
                                                        <div className="flex items-center gap-1.5 font-medium text-gray-900">
                                                            <Users className="h-3.5 w-3.5 text-blue-500" />
                                                            {tenant.ownerName}
                                                        </div>
                                                        <span className="text-xs text-gray-500 ml-5">
                                                            {tenant.ownerEmail}
                                                        </span>
                                                    </div>
                                                ) : (
                                                    <span className="text-sm text-gray-400 italic">Non assigné</span>
                                                )}
                                            </TableCell>
                                            <TableCell className="text-center">
                                                <div className="flex flex-col gap-1 items-center text-xs">
                                                    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-blue-100 text-blue-800">
                                                        <Users className="h-3 w-3" />
                                                        {tenant.stats?.userCount || 0} users
                                                    </span>
                                                    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-green-100 text-green-800">
                                                        <Building2 className="h-3 w-3" />
                                                        {tenant.stats?.resourceCount || 0} res.
                                                    </span>
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                <span
                                                    className={`px-2.5 py-1 rounded-full text-xs font-semibold border ${tenant.status === 'ACTIVE'
                                                        ? 'bg-green-50 text-green-700 border-green-200'
                                                        : 'bg-gray-50 text-gray-700 border-gray-200'
                                                        }`}
                                                >
                                                    {tenant.status || 'INCONNU'}
                                                </span>
                                            </TableCell>
                                            <TableCell className="text-gray-500 text-sm">
                                                {tenant.createdAt
                                                    ? new Date(tenant.createdAt).toLocaleDateString('fr-FR', {
                                                        year: 'numeric',
                                                        month: 'short',
                                                        day: 'numeric'
                                                    })
                                                    : '-'}
                                            </TableCell>
                                            <TableCell className="text-right pr-6">
                                                <div className="flex gap-2 justify-end">
                                                    <Link href={`/tenants/${tenant.id}`}>
                                                        <Button variant="outline" size="sm" className="h-8 hover:bg-purple-50 hover:text-purple-700 hover:border-purple-200">
                                                            <Eye className="h-3.5 w-3.5 mr-1.5" />
                                                            Voire
                                                        </Button>
                                                    </Link>
                                                    <Link href={`/tenants/${tenant.id}/stats`}>
                                                        <Button variant="secondary" size="sm" className="h-8 bg-gray-100 hover:bg-gray-200 text-gray-700">
                                                            <BarChart3 className="h-3.5 w-3.5 mr-1" />
                                                            Stats
                                                        </Button>
                                                    </Link>
                                                </div>
                                            </TableCell>
                                        </TableRow>
                                    ))
                                )}
                            </TableBody>
                        </Table>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}
