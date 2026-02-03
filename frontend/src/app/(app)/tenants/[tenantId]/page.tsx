'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import {
    Building2,
    Users,
    ArrowLeft,
    Loader2,
    TreePine,
    Shield,
    Activity,
} from 'lucide-react';
import { api } from '@/services/api';

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

interface User {
    id: string;
    username: string;
    email: string;
    enabled: boolean;
    accountActivated: boolean;
}

interface ResourceTree {
    id: string;
    name: string;
    type: string;
    children: ResourceTree[];
}

export default function TenantDetailPage() {
    const params = useParams();
    const router = useRouter();
    const tenantId = params?.tenantId as string;

    const [tenant, setTenant] = useState<Tenant | null>(null);
    const [stats, setStats] = useState<TenantStats | null>(null);
    const [users, setUsers] = useState<User[]>([]);
    const [resources, setResources] = useState<ResourceTree[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (tenantId) {
            fetchTenantData();
        }
    }, [tenantId]);

    async function fetchTenantData() {
        try {
            setLoading(true);
            const [tenantRes, statsRes, usersRes, resourcesRes] = await Promise.all([
                api.get<Tenant>(`/api/tenants/${tenantId}`),
                api.get<TenantStats>(`/api/tenants/${tenantId}/stats`),
                api.get<User[]>(`/api/users/tenant/${tenantId}`),
                api.get<ResourceTree[]>(`/api/resources/tenant/${tenantId}`),
            ]);

            setTenant(tenantRes.data);
            setStats(statsRes.data);
            setUsers(usersRes.data);
            setResources(resourcesRes.data);
        } catch (err) {
            console.error('Erreur chargement tenant:', err);
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

    if (!tenant) {
        return <div>Organisation introuvable</div>;
    }

    const renderResourceTree = (resource: ResourceTree, level = 0) => (
        <div key={resource.id} className={`${level > 0 ? 'ml-8 border-l-2 border-gray-200 pl-6' : ''}`}>
            <div className="flex items-center gap-3 py-3 px-4 bg-white rounded-lg shadow-sm mb-2">
                <Building2 className="h-5 w-5 text-blue-600" />
                <div>
                    <p className="font-semibold">{resource.name}</p>
                    <p className="text-sm text-gray-500">{resource.type}</p>
                </div>
            </div>
            {resource.children && resource.children.length > 0 && (
                <div className="mt-2">
                    {resource.children.map((child) => renderResourceTree(child, level + 1))}
                </div>
            )}
        </div>
    );

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <Button variant="outline" onClick={() => router.back()}>
                        <ArrowLeft className="h-4 w-4 mr-2" />
                        Retour
                    </Button>
                    <div>
                        <h1 className="text-3xl font-bold text-gray-900">{tenant.name}</h1>
                        <p className="text-gray-600 mt-1">
                            Code: <span className="font-mono bg-gray-100 px-2 py-1 rounded">{tenant.code}</span>
                        </p>
                    </div>
                </div>
                <span
                    className={`px-4 py-2 rounded-full text-sm font-semibold ${tenant.status === 'ACTIVE'
                            ? 'bg-green-100 text-green-800'
                            : 'bg-gray-100 text-gray-800'
                        }`}
                >
                    {tenant.status}
                </span>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between">
                        <CardTitle className="text-lg font-medium">Utilisateurs</CardTitle>
                        <Users className="h-5 w-5 text-blue-600" />
                    </CardHeader>
                    <CardContent>
                        <p className="text-3xl font-bold">{stats?.userCount || 0}</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between">
                        <CardTitle className="text-lg font-medium">Ressources</CardTitle>
                        <Building2 className="h-5 w-5 text-green-600" />
                    </CardHeader>
                    <CardContent>
                        <p className="text-3xl font-bold">{stats?.resourceCount || 0}</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between">
                        <CardTitle className="text-lg font-medium">Créé le</CardTitle>
                        <Activity className="h-5 w-5 text-purple-600" />
                    </CardHeader>
                    <CardContent>
                        <p className="text-lg font-semibold">
                            {new Date(tenant.createdAt).toLocaleDateString('fr-FR', {
                                day: 'numeric',
                                month: 'long',
                                year: 'numeric',
                            })}
                        </p>
                    </CardContent>
                </Card>
            </div>

            {/* Tabs */}
            <Tabs defaultValue="users" className="w-full">
                <TabsList>
                    <TabsTrigger value="users">
                        <Users className="h-4 w-4 mr-2" />
                        Utilisateurs
                    </TabsTrigger>
                    <TabsTrigger value="resources">
                        <TreePine className="h-4 w-4 mr-2" />
                        Hiérarchie
                    </TabsTrigger>
                </TabsList>

                <TabsContent value="users" className="mt-6">
                    <Card>
                        <CardHeader>
                            <CardTitle>Liste des utilisateurs</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>Nom d'utilisateur</TableHead>
                                        <TableHead>Email</TableHead>
                                        <TableHead>Statut</TableHead>
                                        <TableHead>Compte activé</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {users.length === 0 ? (
                                        <TableRow>
                                            <TableCell colSpan={4} className="text-center py-8 text-gray-500">
                                                Aucun utilisateur
                                            </TableCell>
                                        </TableRow>
                                    ) : (
                                        users.map((user) => (
                                            <TableRow key={user.id}>
                                                <TableCell className="font-medium">{user.username}</TableCell>
                                                <TableCell>{user.email}</TableCell>
                                                <TableCell>
                                                    <span
                                                        className={`px-2 py-1 rounded-full text-xs font-semibold ${user.enabled
                                                                ? 'bg-green-100 text-green-800'
                                                                : 'bg-red-100 text-red-800'
                                                            }`}
                                                    >
                                                        {user.enabled ? 'Actif' : 'Désactivé'}
                                                    </span>
                                                </TableCell>
                                                <TableCell>
                                                    <span
                                                        className={`px-2 py-1 rounded-full text-xs font-semibold ${user.accountActivated
                                                                ? 'bg-blue-100 text-blue-800'
                                                                : 'bg-yellow-100 text-yellow-800'
                                                            }`}
                                                    >
                                                        {user.accountActivated ? 'Oui' : 'En attente'}
                                                    </span>
                                                </TableCell>
                                            </TableRow>
                                        ))
                                    )}
                                </TableBody>
                            </Table>
                        </CardContent>
                    </Card>
                </TabsContent>

                <TabsContent value="resources" className="mt-6">
                    <Card>
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <TreePine className="h-5 w-5" />
                                Hiérarchie des ressources
                            </CardTitle>
                        </CardHeader>
                        <CardContent>
                            {resources.length === 0 ? (
                                <div className="text-center py-12 text-gray-500">
                                    <Building2 className="h-16 w-16 mx-auto mb-4 text-gray-400" />
                                    <p>Aucune ressource</p>
                                </div>
                            ) : (
                                <div className="space-y-4">
                                    {resources.map((resource) => renderResourceTree(resource))}
                                </div>
                            )}
                        </CardContent>
                    </Card>
                </TabsContent>
            </Tabs>
        </div>
    );
}
