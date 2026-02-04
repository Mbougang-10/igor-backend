'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { User, Mail, Lock, Loader2, ArrowLeft, CheckCircle2, ShieldCheck } from 'lucide-react';
import { api } from '@/services/api';

export default function RegisterPage() {
    const router = useRouter();
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);

    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: ''
    });

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (formData.password !== formData.confirmPassword) {
            setError("Les mots de passe ne correspondent pas");
            return;
        }

        if (formData.password.length < 8) {
            setError("Le mot de passe doit contenir au moins 8 caractères");
            return;
        }

        setIsLoading(true);
        setError(null);

        try {
            const response = await api.post('/api/auth/register', {
                username: formData.username,
                email: formData.email,
                password: formData.password
            });

            const { token, userId, email, username, roles } = response.data;

            // Stockage local
            localStorage.setItem('access_token', token);
            localStorage.setItem('user_id', userId);
            localStorage.setItem('user_email', email);
            localStorage.setItem('user_name', username);

            // L'utilisateur n'a pas de rôle pour le moment
            localStorage.setItem('userRole', 'user');

            setSuccess(true);

            setTimeout(() => {
                router.push('/dashboard');
            }, 2000);
        } catch (err: any) {
            console.error('Registration error:', err);
            setError(err.message || "Une erreur est survenue lors de l'inscription");
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
                    <h1 className="text-3xl font-bold text-slate-900 tracking-tight">Créer un Compte</h1>
                    <p className="mt-2 text-slate-600">Rejoignez YowAccess et gérez vos accès</p>
                </div>

                <Card className="border-none shadow-xl bg-white/80 backdrop-blur-sm">
                    <CardHeader>
                        <CardTitle className="text-xl">Inscription Gratuite</CardTitle>
                        <CardDescription>
                            Créez votre compte personnel. Vous pourrez ensuite rejoindre des organisations.
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        {success ? (
                            <div className="text-center space-y-4 py-4">
                                <div className="mx-auto h-12 w-12 bg-green-100 rounded-full flex items-center justify-center">
                                    <CheckCircle2 className="h-8 w-8 text-green-600" />
                                </div>
                                <h3 className="text-lg font-semibold text-green-800">Compte créé !</h3>
                                <p className="text-sm text-slate-600">
                                    Votre compte a été créé avec succès. Redirection vers votre espace...
                                </p>
                            </div>
                        ) : (
                            <form onSubmit={handleSubmit} className="space-y-4">
                                {error && (
                                    <Alert variant="destructive">
                                        <AlertDescription>{error}</AlertDescription>
                                    </Alert>
                                )}

                                <div className="space-y-2">
                                    <Label htmlFor="username">Nom d'utilisateur</Label>
                                    <div className="relative">
                                        <User className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
                                        <Input
                                            id="username"
                                            type="text"
                                            placeholder="johndoe"
                                            className="pl-10 h-11 border-slate-200 focus:border-blue-500 focus:ring-blue-500"
                                            value={formData.username}
                                            onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                                            required
                                            minLength={3}
                                        />
                                    </div>
                                    <p className="text-xs text-slate-500">Minimum 3 caractères</p>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="email">Email</Label>
                                    <div className="relative">
                                        <Mail className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
                                        <Input
                                            id="email"
                                            type="email"
                                            placeholder="nom@exemple.com"
                                            className="pl-10 h-11 border-slate-200 focus:border-blue-500 focus:ring-blue-500"
                                            value={formData.email}
                                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                            required
                                        />
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="password">Mot de passe</Label>
                                    <div className="relative">
                                        <Lock className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
                                        <Input
                                            id="password"
                                            type="password"
                                            placeholder="••••••••"
                                            className="pl-10 h-11 border-slate-200 focus:border-blue-500 focus:ring-blue-500"
                                            value={formData.password}
                                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                                            required
                                            minLength={8}
                                        />
                                    </div>
                                    <p className="text-xs text-slate-500">Minimum 8 caractères</p>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="confirmPassword">Confirmer le mot de passe</Label>
                                    <div className="relative">
                                        <Lock className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
                                        <Input
                                            id="confirmPassword"
                                            type="password"
                                            placeholder="••••••••"
                                            className="pl-10 h-11 border-slate-200 focus:border-blue-500 focus:ring-blue-500"
                                            value={formData.confirmPassword}
                                            onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                                            required
                                        />
                                    </div>
                                </div>

                                <Button
                                    className="w-full h-11 bg-blue-600 hover:bg-blue-700 transition-all font-semibold"
                                    disabled={isLoading}
                                >
                                    {isLoading ? (
                                        <>
                                            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                            Création en cours...
                                        </>
                                    ) : (
                                        'Créer mon compte'
                                    )}
                                </Button>

                                <div className="relative my-6">
                                    <div className="absolute inset-0 flex items-center">
                                        <div className="w-full border-t border-slate-200"></div>
                                    </div>
                                    <div className="relative flex justify-center text-xs uppercase">
                                        <span className="bg-white px-2 text-slate-500">Ou</span>
                                    </div>
                                </div>

                                <div className="space-y-3">
                                    <div className="text-center">
                                        <Link href="/login" className="text-sm font-medium text-blue-600 hover:text-blue-500 flex items-center justify-center gap-2">
                                            <ArrowLeft className="h-4 w-4" /> Retour à la connexion
                                        </Link>
                                    </div>
                                </div>
                            </form>
                        )}
                    </CardContent>
                </Card>

                <p className="text-center text-xs text-slate-500">
                    En créant un compte, vous acceptez nos conditions d'utilisation et notre politique de confidentialité.
                </p>
            </div>
        </div>
    );
}
