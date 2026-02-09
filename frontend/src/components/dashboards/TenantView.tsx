'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Users, Building2, TreePine, Plus, Loader2, ArrowLeft } from 'lucide-react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import CreateDepartmentModal from '@/components/CreateDepartmentModal';
import HierarchyTree from '@/components/HierarchyTree';
import UserManagement from '@/components/UserManagement';
import { api } from '@/services/api';

interface TenantViewProps {
    tenantId: string;
    onBack: () => void;
}

interface TenantStats {
    userCount: number;
    resourceCount: number;
}

interface ResourceTree {
    id: string;
    type: string;
}

export default function TenantView({ tenantId, onBack }: TenantViewProps) {
    const [stats, setStats] = useState<TenantStats>({ userCount: 0, resourceCount: 0 });
    const [loading, setLoading] = useState(true);
    const [permissions, setPermissions] = useState<Set<string>>(new Set());
    const [rootResource, setRootResource] = useState<ResourceTree | null>(null);

    // UI States
    const [modalOpen, setModalOpen] = useState(false);
    const [selectedParent, setSelectedParent] = useState<any>(null);

    useEffect(() => {
        fetchData();
    }, [tenantId]);

    const fetchData = async () => {
        try {
            setLoading(true);

            // 1. Fetch Stats
            const statsRes = await api.get<TenantStats>(`/api/tenants/${tenantId}/stats`);
            setStats(statsRes.data);

            // 2. Fetch Root Resource to get context for permissions
            const resourcesRes = await api.get<ResourceTree[]>(`/api/resources/tenant/${tenantId}`);
            if (resourcesRes.data.length > 0) {
                const root = resourcesRes.data[0]; // Assuming first is root
                setRootResource(root);

                // 3. Fetch Permissions on Root
                const permsRes = await api.get<string[]>(`/api/resources/${root.id}/permissions`);
                setPermissions(new Set(permsRes.data));
            }

        } catch (err) {
            console.error("Erreur chargement tenant view", err);
        } finally {
            setLoading(false);
        }
    };

    const hasPermission = (perm: string) => {
        return permissions.has('ADMIN') || permissions.has('TENANT_ADMIN') || permissions.has(perm);
    };

    const handleCreateDepartment = () => {
        setSelectedParent(null);
        setModalOpen(true);
    };

    const handleCreateSubDepartment = (parent: any) => {
        setSelectedParent(parent);
        setModalOpen(true);
    };

    if (loading) return <div className="p-8 flex justify-center"><Loader2 className="animate-spin" /></div>;

    return (
        <div className="space-y-6">
            <Button variant="ghost" onClick={onBack} className="mb-4 pl-0 hover:bg-transparent hover:text-blue-600">
                <ArrowLeft className="mr-2 h-4 w-4" /> Retour mes organisations
            </Button>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between">
                        <CardTitle className="text-lg font-medium">Utilisateurs</CardTitle>
                        <Users className="h-5 w-5 text-blue-600" />
                    </CardHeader>
                    <CardContent>
                        <p className="text-3xl font-bold">{stats.userCount}</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between">
                        <CardTitle className="text-lg font-medium">Départements</CardTitle>
                        <Building2 className="h-5 w-5 text-green-600" />
                    </CardHeader>
                    <CardContent>
                        <p className="text-3xl font-bold">{stats.resourceCount}</p>
                    </CardContent>
                </Card>
            </div>

            <Tabs defaultValue="hierarchy" className="w-full">
                <TabsList>
                    <TabsTrigger value="hierarchy">Structure Organisation</TabsTrigger>
                    {/* Show Users tab if has permission */}
                    {(hasPermission('USER_READ') || hasPermission('USER_CREATE')) && (
                        <TabsTrigger value="users">Utilisateurs & Rôles</TabsTrigger>
                    )}
                </TabsList>

                <TabsContent value="hierarchy" className="mt-6">
                    <Card>
                        <CardHeader className="flex items-center justify-between">
                            <CardTitle className="flex items-center gap-3">
                                <TreePine className="h-6 w-6" />
                                Hiérarchie
                            </CardTitle>
                            {/* GATE CREATE BUTTON */}
                            {hasPermission('RESOURCE_CREATE') && (
                                <Button onClick={handleCreateDepartment} className="bg-blue-600 hover:bg-blue-700">
                                    <Plus className="h-5 w-5 mr-2" />
                                    Créer un département
                                </Button>
                            )}
                        </CardHeader>
                        <CardContent>
                            <HierarchyTree
                                onCreateSubDepartment={hasPermission('RESOURCE_CREATE') ? handleCreateSubDepartment : undefined}
                            />
                        </CardContent>
                    </Card>
                </TabsContent>

                <TabsContent value="users" className="mt-6">
                    {/* Pass permissions or readonly mode to UserManagement? 
                         For now, UserManagement does its own checks or backend blocks it. 
                         But we can at lease show it. */}
                    <UserManagement tenantId={tenantId} />
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
