'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Users, Building2, TreePine, Plus, Loader2 } from 'lucide-react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import CreateDepartmentModal from '@/components/CreateDepartmentModal';
import HierarchyTree from '@/components/HierarchyTree';
import UserManagement from '@/components/UserManagement';
import { api } from '@/services/api';

interface Department {
    id: string;
    name: string;
}

interface Tenant {
    id: string;
    name: string;
}

interface TenantStats {
    userCount: number;
    resourceCount: number;
}

export default function TenantAdminDashboard() {
    const [modalOpen, setModalOpen] = useState(false);
    const [selectedParent, setSelectedParent] = useState<Department | null>(null);
    const [stats, setStats] = useState<TenantStats>({ userCount: 0, resourceCount: 0 });
    const [tenant, setTenant] = useState<Tenant | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function fetchStats() {
            try {
                setLoading(true);

                const tenantsResponse = await api.get<Tenant[]>('/api/tenants');
                const tenants = tenantsResponse.data;

                if (tenants.length > 0) {
                    const currentTenant = tenants[0];
                    setTenant(currentTenant);

                    const statsResponse = await api.get<TenantStats>(`/api/tenants/${currentTenant.id}/stats`);
                    setStats(statsResponse.data);
                }
            } catch (err) {
                console.error('Erreur lors du chargement des stats:', err);
            } finally {
                setLoading(false);
            }
        }

        fetchStats();
    }, []);

    const handleCreateDepartment = () => {
        setSelectedParent(null);
        setModalOpen(true);
    };

    const handleCreateSubDepartment = (parent: Department) => {
        setSelectedParent(parent);
        setModalOpen(true);
    };

    if (loading) {
        return (
            <div className="flex h-screen items-center justify-center">
                <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
            </div>
        );
    }

    if (!tenant) {
        return <div>Aucune organisation trouvée.</div>;
    }

    return (
        <div className="space-y-8">
            <div>
                <h1 className="text-3xl font-bold text-gray-900 border-l-4 border-blue-600 pl-4">
                    Administration Organisation
                </h1>
                <p className="text-gray-600 mt-2 pl-4">
                    Gestion de votre espace <span className="font-semibold">{tenant.name}</span>
                </p>
            </div>

            <Tabs defaultValue="dashboard" className="w-full">
                <TabsList>
                    <TabsTrigger value="dashboard">Tableau de bord</TabsTrigger>
                    <TabsTrigger value="users">Utilisateurs</TabsTrigger>
                </TabsList>

                <TabsContent value="dashboard" className="space-y-6 mt-6">
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between">
                                <CardTitle className="text-lg font-medium">Utilisateurs</CardTitle>
                                <Users className="h-5 w-5 text-blue-600" />
                            </CardHeader>
                            <CardContent>
                                <p className="text-3xl font-bold">{stats.userCount}</p>
                                <p className="text-sm text-gray-600">
                                    {stats.userCount <= 1 ? 'Utilisateur actif' : 'Utilisateurs actifs'}
                                </p>
                            </CardContent>
                        </Card>

                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between">
                                <CardTitle className="text-lg font-medium">Départements</CardTitle>
                                <Building2 className="h-5 w-5 text-green-600" />
                            </CardHeader>
                            <CardContent>
                                <p className="text-3xl font-bold">{stats.resourceCount}</p>
                                <p className="text-sm text-gray-600">
                                    {stats.resourceCount <= 1 ? 'Ressource' : 'Ressources'}
                                </p>
                            </CardContent>
                        </Card>

                        <Card>
                            <CardHeader className="flex flex-row items-center justify-between">
                                <CardTitle className="text-lg font-medium">Hiérarchie</CardTitle>
                                <TreePine className="h-5 w-5 text-purple-600" />
                            </CardHeader>
                            <CardContent>
                                <p className="text-sm text-gray-600">Structure complète visible ci-dessous</p>
                            </CardContent>
                        </Card>
                    </div>

                    <Card className="mt-8">
                        <CardHeader className="flex items-center justify-between">
                            <CardTitle className="flex items-center gap-3">
                                <TreePine className="h-6 w-6" />
                                Hiérarchie de l'organisation
                            </CardTitle>
                            <Button size="lg" onClick={handleCreateDepartment} className="bg-blue-600 hover:bg-blue-700">
                                <Plus className="h-5 w-5 mr-2" />
                                Créer un département
                            </Button>
                        </CardHeader>
                        <CardContent>
                            <HierarchyTree onCreateSubDepartment={handleCreateSubDepartment} />
                        </CardContent>
                    </Card>
                </TabsContent>

                <TabsContent value="users" className="mt-6">
                    <UserManagement tenantId={tenant.id} />
                </TabsContent>
            </Tabs>

            <CreateDepartmentModal
                open={modalOpen}
                onOpenChange={setModalOpen}
                parentDepartment={selectedParent}
            />
        </div>
    );
}
