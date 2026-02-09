'use client';
import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Building2, Users, Mail, AlertCircle, CheckCircle2, ArrowRight, Loader2, LogIn } from 'lucide-react';
import Link from 'next/link';
import { api } from '@/services/api';
import TenantView from './TenantView';

interface Tenant {
    id: string;
    name: string;
    code: string;
    status: string;
}

export default function UserDashboard() {
    const [tenants, setTenants] = useState<Tenant[]>([]);
    const [loading, setLoading] = useState(true);
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);

    useEffect(() => {
        fetchTenants();
    }, []);

    const fetchTenants = async () => {
        try {
            const res = await api.get<Tenant[]>('/api/tenants');
            setTenants(res.data);
        } catch (err) {
            console.error("Erreur chargement tenants", err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex h-64 items-center justify-center">
                <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
            </div>
        );
    }

    if (selectedTenantId) {
        return <TenantView tenantId={selectedTenantId} onBack={() => setSelectedTenantId(null)} />;
    }

    const hasTenants = tenants.length > 0;

    return (
        <div className="space-y-8 max-w-4xl mx-auto">
            <div className="text-center">
                <h1 className="text-3xl font-bold text-gray-900">Bienvenue sur YowAccess !</h1>
                <p className="text-gray-600 mt-2">
                    {hasTenants
                        ? "Sélectionnez une organisation pour y accéder"
                        : "Votre compte a été créé avec succès"}
                </p>
            </div>

            {hasTenants ? (
                <div className="grid gap-6">
                    {tenants.map(tenant => (
                        <Card key={tenant.id} className="border-l-4 border-l-blue-600 hover:shadow-md transition-shadow">
                            <CardHeader className="flex flex-row items-center justify-between pb-2">
                                <div>
                                    <CardTitle className="text-xl">{tenant.name}</CardTitle>
                                    <CardDescription>Code: {tenant.code}</CardDescription>
                                </div>
                                <Building2 className="h-8 w-8 text-slate-400" />
                            </CardHeader>
                            <CardContent>
                                <div className="flex items-center justify-between">
                                    <div className="flex items-center gap-2">
                                        <span className={`px-2 py-1 rounded-full text-xs font-semibold ${tenant.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                                            }`}>
                                            {tenant.status}
                                        </span>
                                        <span className="text-sm text-gray-500">Membre</span>
                                    </div>
                                    <Button onClick={() => setSelectedTenantId(tenant.id)}>
                                        Accéder <LogIn className="ml-2 h-4 w-4" />
                                    </Button>
                                </div>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            ) : (
                <>
                    <Alert className="border-blue-200 bg-blue-50">
                        <AlertCircle className="h-4 w-4 text-blue-600" />
                        <AlertDescription className="text-blue-800">
                            <strong>Prochaine étape :</strong> Pour accéder aux fonctionnalités, vous devez soit créer une organisation, soit être invité à rejoindre une organisation existante.
                        </AlertDescription>
                    </Alert>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <Card className="border-2 border-blue-100 hover:border-blue-300 transition-all hover:shadow-lg">
                            <CardHeader>
                                <div className="h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4">
                                    <Building2 className="h-6 w-6 text-blue-600" />
                                </div>
                                <CardTitle className="text-xl">Créer une Organisation</CardTitle>
                                <CardDescription>
                                    Créez votre propre organisation et invitez des membres. Vous serez automatiquement administrateur.
                                </CardDescription>
                            </CardHeader>
                            <CardContent>
                                <ul className="space-y-2 mb-4">
                                    <li className="flex items-start gap-2 text-sm text-gray-600">
                                        <CheckCircle2 className="h-4 w-4 text-green-500 mt-0.5 flex-shrink-0" />
                                        <span>Gestion complète des utilisateurs et des rôles</span>
                                    </li>
                                    <li className="flex items-start gap-2 text-sm text-gray-600">
                                        <CheckCircle2 className="h-4 w-4 text-green-500 mt-0.5 flex-shrink-0" />
                                        <span>Hiérarchie de ressources personnalisée</span>
                                    </li>
                                    <li className="flex items-start gap-2 text-sm text-gray-600">
                                        <CheckCircle2 className="h-4 w-4 text-green-500 mt-0.5 flex-shrink-0" />
                                        <span>Contrôle d'accès granulaire (RBAC)</span>
                                    </li>
                                </ul>
                                <Button className="w-full bg-blue-600 hover:bg-blue-700" asChild>
                                    <Link href="/login?tab=create-org" className="flex items-center justify-center gap-2">
                                        Créer mon organisation <ArrowRight className="h-4 w-4" />
                                    </Link>
                                </Button>
                            </CardContent>
                        </Card>

                        <Card className="border-2 border-purple-100 hover:border-purple-300 transition-all hover:shadow-lg">
                            <CardHeader>
                                <div className="h-12 w-12 bg-purple-100 rounded-lg flex items-center justify-center mb-4">
                                    <Users className="h-6 w-6 text-purple-600" />
                                </div>
                                <CardTitle className="text-xl">Rejoindre une Organisation</CardTitle>
                                <CardDescription>
                                    Attendez qu'un administrateur vous invite à rejoindre une organisation existante.
                                </CardDescription>
                            </CardHeader>
                            <CardContent>
                                <ul className="space-y-2 mb-4">
                                    <li className="flex items-start gap-2 text-sm text-gray-600">
                                        <Mail className="h-4 w-4 text-purple-500 mt-0.5 flex-shrink-0" />
                                        <span>Vous recevrez une invitation par email</span>
                                    </li>
                                    <li className="flex items-start gap-2 text-sm text-gray-600">
                                        <CheckCircle2 className="h-4 w-4 text-green-500 mt-0.5 flex-shrink-0" />
                                        <span>Accès immédiat après acceptation</span>
                                    </li>
                                    <li className="flex items-start gap-2 text-sm text-gray-600">
                                        <CheckCircle2 className="h-4 w-4 text-green-500 mt-0.5 flex-shrink-0" />
                                        <span>Droits définis par l'administrateur</span>
                                    </li>
                                </ul>
                                <Button variant="outline" className="w-full" disabled>
                                    En attente d'invitation
                                </Button>
                            </CardContent>
                        </Card>
                    </div>
                </>
            )}

            <Card className="bg-gradient-to-br from-slate-50 to-slate-100 border-slate-200">
                <CardHeader>
                    <CardTitle className="text-lg">Informations de votre compte</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                    <div className="flex justify-between items-center py-2 border-b border-slate-200">
                        <span className="text-sm text-gray-600">Email</span>
                        <span className="text-sm font-medium">{typeof window !== 'undefined' ? localStorage.getItem('user_email') : ''}</span>
                    </div>
                    <div className="flex justify-between items-center py-2 border-b border-slate-200">
                        <span className="text-sm text-gray-600">Nom d'utilisateur</span>
                        <span className="text-sm font-medium">{typeof window !== 'undefined' ? localStorage.getItem('user_name') : ''}</span>
                    </div>
                    <div className="flex justify-between items-center py-2">
                        <span className="text-sm text-gray-600">Statut</span>
                        {hasTenants ? (
                            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 bg-green-100 text-green-800 text-xs font-medium rounded-full">
                                <CheckCircle2 className="h-3 w-3" />
                                Actif
                            </span>
                        ) : (
                            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 bg-yellow-100 text-yellow-800 text-xs font-medium rounded-full">
                                <AlertCircle className="h-3 w-3" />
                                En attente d'organisation
                            </span>
                        )}
                    </div>
                </CardContent>
            </Card>

            <div className="text-center text-sm text-gray-500">
                <p>
                    Besoin d'aide ? Consultez notre{' '}
                    <a href="#" className="text-blue-600 hover:underline">documentation</a>
                    {' '}ou contactez le{' '}
                    <a href="#" className="text-blue-600 hover:underline">support</a>.
                </p>
            </div>
        </div>
    );
}
