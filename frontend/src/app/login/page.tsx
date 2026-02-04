'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Building2, Lock, Mail, User, Loader2, CheckCircle2, ShieldCheck, ArrowRight } from 'lucide-react';
import { api } from '@/services/api';
import { Alert, AlertDescription } from '@/components/ui/alert';

export default function LoginPage() {
    const router = useRouter();
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    // Login state
    const [loginEmail, setLoginEmail] = useState('');
    const [loginPassword, setLoginPassword] = useState('');

    // Signup state
    const [signupEmail, setSignupEmail] = useState('');
    const [signupPassword, setSignupPassword] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [orgName, setOrgName] = useState('');

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setError(null);

        try {
            const response = await api.post('/api/auth/login', {
                email: loginEmail,
                password: loginPassword,
            });

            const { token, userId, email, username, roles } = response.data;

            // Stockage local
            localStorage.setItem('access_token', token);
            localStorage.setItem('user_id', userId);
            localStorage.setItem('user_email', email);
            localStorage.setItem('user_name', username);

            // Détermination du rôle pour l'interface
            let userRole = 'user';
            if (roles && Array.isArray(roles)) {
                if (roles.includes('ADMIN')) {
                    userRole = 'super_admin';
                } else if (roles.includes('TENANT_ADMIN')) {
                    userRole = 'tenant_admin';
                }
            }
            localStorage.setItem('userRole', userRole);

            router.push('/dashboard');
        } catch (err: any) {
            console.error('Login error:', err);
            setError(err?.message || 'Email ou mot de passe incorrect');
        } finally {
            setIsLoading(false);
        }
    };

    const handleSignup = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setError(null);

        try {
            const response = await api.post('/api/auth/register-tenant', {
                email: signupEmail,
                password: signupPassword,
                firstName,
                lastName,
                organizationName: orgName,
            });

            const { token, userId, email, username, roles } = response.data;

            // Stockage local
            localStorage.setItem('access_token', token);
            localStorage.setItem('user_id', userId);
            localStorage.setItem('user_email', email);
            localStorage.setItem('user_name', username);

            // Pour un nouveau tenant, c'est forcément un admin de tenant
            localStorage.setItem('userRole', 'tenant_admin');

            setSuccess('Organisation créée avec succès ! redirection...');

            // Laisser le temps de voir le message de succès
            setTimeout(() => {
                router.push('/dashboard');
            }, 1500);

        } catch (err: any) {
            console.error('Signup error:', err);
            setError(err?.message || 'Erreur lors de la création de l\'organisation');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
            <div className="max-w-md w-full space-y-8">
                <div className="text-center">
                    <div className="mx-auto h-16 w-16 bg-blue-600 rounded-2xl flex items-center justify-center shadow-lg shadow-blue-200 mb-4">
                        <ShieldCheck className="h-10 w-10 text-white" />
                    </div>
                    <h1 className="text-4xl font-extrabold text-slate-900 tracking-tight">YowAccess</h1>
                    <p className="mt-2 text-slate-600">Système de Gestion des Accès Multi-Tenant</p>
                </div>

                <Card className="border-none shadow-xl bg-white/80 backdrop-blur-sm">
                    <CardHeader className="pb-0">
                        <Tabs defaultValue="login" className="w-full">
                            <TabsList className="grid w-full grid-cols-2 mb-4 h-12">
                                <TabsTrigger value="login" className="text-sm font-medium">Connexion</TabsTrigger>
                                <TabsTrigger value="signup" className="text-sm font-medium">Créer une organisation</TabsTrigger>
                            </TabsList>

                            {error && (
                                <Alert variant="destructive" className="mb-4">
                                    <AlertDescription>{error}</AlertDescription>
                                </Alert>
                            )}

                            {success && (
                                <Alert className="mb-4 bg-green-50 border-green-200 text-green-800">
                                    <CheckCircle2 className="h-4 w-4 text-green-600" />
                                    <AlertDescription>{success}</AlertDescription>
                                </Alert>
                            )}

                            <TabsContent value="login" className="mt-0">
                                <CardHeader className="px-0 pt-2 pb-6">
                                    <CardTitle className="text-xl">Bon retour</CardTitle>
                                    <CardDescription>Connectez-vous à votre espace de travail</CardDescription>
                                </CardHeader>
                                <form onSubmit={handleLogin} className="space-y-4 pb-4">
                                    <div className="space-y-2">
                                        <Label htmlFor="email">Email professionnel</Label>
                                        <div className="relative">
                                            <Mail className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
                                            <Input
                                                id="email"
                                                type="email"
                                                placeholder="nom@entreprise.com"
                                                className="pl-10 h-11 border-slate-200 focus:border-blue-500 focus:ring-blue-500"
                                                value={loginEmail}
                                                onChange={(e) => setLoginEmail(e.target.value)}
                                                required
                                            />
                                        </div>
                                    </div>
                                    <div className="space-y-2">
                                        <div className="flex items-center justify-between">
                                            <Label htmlFor="password">Mot de passe</Label>
                                            <Button variant="link" className="px-0 font-normal text-xs text-blue-600" type="button">
                                                Mot de passe oublié ?
                                            </Button>
                                        </div>
                                        <div className="relative">
                                            <Lock className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
                                            <Input
                                                id="password"
                                                type="password"
                                                placeholder="••••••••"
                                                className="pl-10 h-11 border-slate-200 focus:border-blue-500 focus:ring-blue-500"
                                                value={loginPassword}
                                                onChange={(e) => setLoginPassword(e.target.value)}
                                                required
                                            />
                                        </div>
                                    </div>
                                    <Button className="w-full h-11 bg-blue-600 hover:bg-blue-700 transition-all font-semibold" disabled={isLoading}>
                                        {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : 'Se connecter'}
                                    </Button>
                                </form>
                            </TabsContent>

                            <TabsContent value="signup" className="mt-0">
                                <CardHeader className="px-0 pt-2 pb-6">
                                    <CardTitle className="text-xl">Nouvelle Organisation</CardTitle>
                                    <CardDescription>Déployez YowAccess pour votre entreprise</CardDescription>
                                </CardHeader>
                                <form onSubmit={handleSignup} className="space-y-4 pb-4">
                                    <div className="grid grid-cols-2 gap-4">
                                        <div className="space-y-2">
                                            <Label htmlFor="firstName">Prénom</Label>
                                            <Input
                                                id="firstName"
                                                placeholder="Jean"
                                                className="h-11 border-slate-200"
                                                value={firstName}
                                                onChange={(e) => setFirstName(e.target.value)}
                                                required
                                            />
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="lastName">Nom</Label>
                                            <Input
                                                id="lastName"
                                                placeholder="Dupont"
                                                className="h-11 border-slate-200"
                                                value={lastName}
                                                onChange={(e) => setLastName(e.target.value)}
                                                required
                                            />
                                        </div>
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="orgName">Nom de l'organisation</Label>
                                        <div className="relative">
                                            <Building2 className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
                                            <Input
                                                id="orgName"
                                                placeholder="ACME Corp"
                                                className="pl-10 h-11 border-slate-200"
                                                value={orgName}
                                                onChange={(e) => setOrgName(e.target.value)}
                                                required
                                            />
                                        </div>
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="signup-email">Email professionnel</Label>
                                        <div className="relative">
                                            <Mail className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
                                            <Input
                                                id="signup-email"
                                                type="email"
                                                placeholder="jean.dupont@acme.com"
                                                className="pl-10 h-11 border-slate-200"
                                                value={signupEmail}
                                                onChange={(e) => setSignupEmail(e.target.value)}
                                                required
                                            />
                                        </div>
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="signup-password">Mot de passe</Label>
                                        <div className="relative">
                                            <Lock className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
                                            <Input
                                                id="signup-password"
                                                type="password"
                                                placeholder="Min. 8 caractères"
                                                className="pl-10 h-11 border-slate-200"
                                                value={signupPassword}
                                                onChange={(e) => setSignupPassword(e.target.value)}
                                                required
                                                minLength={8}
                                            />
                                        </div>
                                    </div>
                                    <Button className="w-full h-11 bg-blue-600 hover:bg-blue-700 transition-all font-semibold flex items-center justify-center gap-2" disabled={isLoading}>
                                        {isLoading ? (
                                            <Loader2 className="h-4 w-4 animate-spin" />
                                        ) : (
                                            <>
                                                Créer mon organisation <ArrowRight className="h-4 w-4" />
                                            </>
                                        )}
                                    </Button>
                                    <p className="text-[10px] text-center text-slate-500 mt-4 leading-relaxed">
                                        En créant un compte, vous acceptez nos conditions générales d'utilisation
                                        et notre politique de confidentialité.
                                    </p>
                                </form>
                            </TabsContent>
                        </Tabs>
                    </CardHeader>
                </Card>

                <div className="text-center mt-6">
                    <p className="text-sm text-slate-600">
                        Pas encore de compte ?{' '}
                        <Link href="/register" className="text-blue-600 hover:text-blue-700 font-semibold hover:underline">
                            Créer un compte gratuit
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}
