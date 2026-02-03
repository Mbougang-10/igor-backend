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

interface Tenant {
    id: string;
    name: string;
    code: string;
    status: string;
    createdAt: string;
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
            const response = await api.get<Tenant[]>('/api/tenants');
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
        } catch (err) {
            console.error('Erreur lors du chargement des tenants:', err);
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
                <Card className="border-l-4 border-purple-600">
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

                <Card className="border-l-4 border-blue-600">
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

                <Card className="border-l-4 border-green-600">
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

                <Card className="border-l-4 border-orange-600">
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
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-3">
                        <Building2 className="h-6 w-6 text-purple-600" />
                        Liste des Organisations
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="rounded-md border">
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Organisation</TableHead>
                                    <TableHead>Code</TableHead>
                                    <TableHead>Statut</TableHead>
                                    <TableHead className="text-center">Utilisateurs</TableHead>
                                    <TableHead className="text-center">Ressources</TableHead>
                                    <TableHead>Date de création</TableHead>
                                    <TableHead className="text-right">Actions</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {tenants.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={7} className="text-center py-8 text-gray-500">
                                            Aucune organisation trouvée
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    tenants.map((tenant) => (
                                        <TableRow key={tenant.id}>
                                            <TableCell className="font-medium">
                                                <div className="flex items-center gap-2">
                                                    <Building2 className="h-4 w-4 text-gray-500" />
                                                    {tenant.name}
                                                </div>
                                            </TableCell>
                                            <TableCell>
                                                <span className="font-mono text-sm bg-gray-100 px-2 py-1 rounded">
                                                    {tenant.code}
                                                </span>
                                            </TableCell>
                                            <TableCell>
                                                <span
                                                    className={`px-2 py-1 rounded-full text-xs font-semibold ${tenant.status === 'ACTIVE'
                                                            ? 'bg-green-100 text-green-800'
                                                            : 'bg-gray-100 text-gray-800'
                                                        }`}
                                                >
                                                    {tenant.status}
                                                </span>
                                            </TableCell>
                                            <TableCell className="text-center">
                                                <div className="flex items-center justify-center gap-1">
                                                    <Users className="h-4 w-4 text-blue-600" />
                                                    <span className="font-semibold">{tenant.stats?.userCount || 0}</span>
                                                </div>
                                            </TableCell>
                                            <TableCell className="text-center">
                                                <div className="flex items-center justify-center gap-1">
                                                    <Building2 className="h-4 w-4 text-green-600" />
                                                    <span className="font-semibold">{tenant.stats?.resourceCount || 0}</span>
                                                </div>
                                            </TableCell>
                                            <TableCell className="text-gray-500 text-sm">
                                                {tenant.createdAt
                                                    ? new Date(tenant.createdAt).toLocaleDateString('fr-FR')
                                                    : '-'}
                                            </TableCell>
                                            <TableCell className="text-right">
                                                <div className="flex gap-2 justify-end">
                                                    <Link href={`/tenants/${tenant.id}`}>
                                                        <Button variant="outline" size="sm">
                                                            <Eye className="h-4 w-4 mr-1" />
                                                            Voir
                                                        </Button>
                                                    </Link>
                                                    <Link href={`/tenants/${tenant.id}/stats`}>
                                                        <Button variant="secondary" size="sm">
                                                            <BarChart3 className="h-4 w-4 mr-1" />
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
