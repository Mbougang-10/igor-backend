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
import { Button } from '@/components/ui/button';
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
    DialogFooter,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select';
import { Plus, UserPlus, Loader2, Trash2 } from 'lucide-react';
import { api } from '@/services/api';

interface User {
    id: string;
    username: string;
    email: string;
    enabled: boolean;
    accountActivated: boolean;
}

interface Role {
    id: number;
    name: string;
}

interface Resource {
    id: string;
    name: string;
}

interface UserManagementProps {
    tenantId: string;
}

export default function UserManagement({ tenantId }: UserManagementProps) {
    const [users, setUsers] = useState<User[]>([]);
    const [roles, setRoles] = useState<Role[]>([]);
    const [loading, setLoading] = useState(true);
    const [modalOpen, setModalOpen] = useState(false);
    const [inviteModalOpen, setInviteModalOpen] = useState(false);
    const [assignRoleModalOpen, setAssignRoleModalOpen] = useState(false);

    // Form States
    const [newUser, setNewUser] = useState({ username: '', email: '', password: '' });
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [selectedRole, setSelectedRole] = useState<string>('');
    const [rootResource, setRootResource] = useState<Resource | null>(null);
    const [inviteEmail, setInviteEmail] = useState('');

    useEffect(() => {
        fetchData();
    }, [tenantId]);

    async function fetchData() {
        try {
            setLoading(true);
            const [usersRes, rolesRes, resourcesRes] = await Promise.all([
                api.get<User[]>(`/api/users/tenant/${tenantId}`),
                api.get<Role[]>('/api/roles'),
                api.get<any[]>(`/api/resources/tenant/${tenantId}`)
            ]);

            setUsers(usersRes.data);
            setRoles(rolesRes.data);

            // Assume first resource is root
            if (resourcesRes.data.length > 0) {
                setRootResource(resourcesRes.data[0]);
            }
        } catch (err) {
            console.error('Erreur chargement données:', err);
        } finally {
            setLoading(false);
        }
    }

    const handleCreateUser = async () => {
        if (!rootResource) return alert("Ressource racine introuvable");

        try {
            // 1. Create User
            const createRes = await api.post<User>('/api/users', {
                username: newUser.username,
                email: newUser.email,
                passwordHash: newUser.password
            });
            const createdUser = createRes.data;

            // 2. Assign Default Role (USER) on Root Resource
            const userRole = roles.find(r => r.name === 'USER');
            if (userRole) {
                await api.post(`/api/users/${createdUser.id}/roles`, {
                    roleId: userRole.id,
                    resourceId: rootResource.id
                });
            }

            setModalOpen(false);
            setNewUser({ username: '', email: '', password: '' });
            fetchData(); // Refresh list
        } catch (err: any) {
            console.error('Erreur création utilisateur:', err);
            // Si l'utilisateur existe déjà, on suggère l'invitation
            if (err.response?.data?.message?.includes("email") || err.response?.status === 400) { // Adapter selon le msg d'erreur exact
                alert("Cet email ou username est déjà pris. Utilisez 'Inviter Existant' si l'utilisateur existe déjà.");
            } else {
                alert("Erreur lors de la création de l'utilisateur: " + (err.response?.data?.message || err.message));
            }
        }
    };

    const handleInviteUser = async () => {
        if (!rootResource) return alert("Organisation non chargée");
        if (!inviteEmail) return alert("Veuillez entrer un email");

        try {
            // 1. Chercher l'utilisateur par email
            const searchRes = await api.get<User>(`/api/users/search?email=${inviteEmail}`);
            const foundUser = searchRes.data;

            // 2. Assigner le rôle par défaut (USER)
            const userRole = roles.find(r => r.name === 'USER');
            if (!userRole) return alert("Role USER introuvable");

            await api.post(`/api/users/${foundUser.id}/roles`, {
                roleId: userRole.id,
                resourceId: rootResource.id
            });

            setInviteModalOpen(false);
            setInviteEmail('');
            fetchData();
            alert("Utilisateur ajouté avec succès !");
        } catch (err: any) {
            console.error("Erreur invitation:", err);
            if (err.response && err.response.status === 404) {
                alert("Aucun utilisateur trouvé avec cet email.");
            } else {
                alert("Erreur: " + (err.response?.data?.message || err.message));
            }
        }
    }

    const handleAssignRole = async () => {
        if (!selectedUser || !selectedRole || !rootResource) return;

        try {
            await api.post(`/api/users/${selectedUser.id}/roles`, {
                roleId: parseInt(selectedRole),
                resourceId: rootResource.id
            });

            setAssignRoleModalOpen(false);
            alert("Rôle assigné avec succès");
        } catch (err) {
            console.error('Erreur assignation rôle:', err);
            alert("Erreur lors de l'assignation");
        }
    };

    const toggleUserStatus = async (user: User) => {
        try {
            await api.patch(`/api/users/${user.id}/enabled?enabled=${!user.enabled}`);
            fetchData();
        } catch (err) {
            console.error('Erreur status utilisateur:', err);
        }
    };

    return (
        <div className="space-y-4">
            <div className="flex justify-between items-center">
                <h2 className="text-xl font-semibold">Utilisateurs du Tenant</h2>
                <div className="flex gap-2">
                    <Button onClick={() => setInviteModalOpen(true)} variant="outline">
                        <UserPlus className="mr-2 h-4 w-4" /> Inviter Existant
                    </Button>
                    <Button onClick={() => setModalOpen(true)}>
                        <Plus className="mr-2 h-4 w-4" /> Créer Nouveau
                    </Button>
                </div>
            </div>

            {/* ... Table code ... */}

            <div className="rounded-md border">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>Username</TableHead>
                            <TableHead>Email</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead>Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {loading ? (
                            <TableRow>
                                <TableCell colSpan={4} className="text-center py-8">
                                    <Loader2 className="h-6 w-6 animate-spin mx-auto" />
                                </TableCell>
                            </TableRow>
                        ) : users.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={4} className="text-center py-8 text-gray-500">
                                    Aucun utilisateur trouvé.
                                </TableCell>
                            </TableRow>
                        ) : (
                            users.map((user) => (
                                <TableRow key={user.id}>
                                    <TableCell className="font-medium">{user.username}</TableCell>
                                    <TableCell>{user.email}</TableCell>
                                    <TableCell>
                                        <span className={`px-2 py-1 rounded-full text-xs font-semibold ${user.enabled ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                                            }`}>
                                            {user.enabled ? 'Actif' : 'Désactivé'}
                                        </span>
                                    </TableCell>
                                    <TableCell className="flex gap-2">
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() => toggleUserStatus(user)}
                                        >
                                            {user.enabled ? 'Désactiver' : 'Activer'}
                                        </Button>
                                        <Button
                                            variant="secondary"
                                            size="sm"
                                            onClick={() => {
                                                setSelectedUser(user);
                                                setAssignRoleModalOpen(true);
                                            }}
                                        >
                                            Gérer Rôles
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>


            {/* MODAL INVITE EXISTING */}
            <Dialog open={inviteModalOpen} onOpenChange={setInviteModalOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Inviter un utilisateur existant</DialogTitle>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                        <p className="text-sm text-gray-600 mb-2">Entrez l&apos;email d&apos;un utilisateur déjà inscrit sur la plateforme.</p>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="inviteEmail" className="text-right">Email</Label>
                            <Input
                                id="inviteEmail"
                                type="email"
                                value={inviteEmail}
                                onChange={(e) => setInviteEmail(e.target.value)}
                                className="col-span-3"
                                placeholder="exemple@email.com"
                            />
                        </div>
                    </div>
                    <DialogFooter>
                        <Button onClick={handleInviteUser}>Ajouter à l&apos;organisation</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* MODAL CREATION UTILISATEUR (Existing Code kept but hidden in this diff) */}
            <Dialog open={modalOpen} onOpenChange={setModalOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Ajouter un nouvel utilisateur</DialogTitle>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="username" className="text-right">Username</Label>
                            <Input
                                id="username"
                                value={newUser.username}
                                onChange={(e) => setNewUser({ ...newUser, username: e.target.value })}
                                className="col-span-3"
                            />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="email" className="text-right">Email</Label>
                            <Input
                                id="email"
                                type="email"
                                value={newUser.email}
                                onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                                className="col-span-3"
                            />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="password" className="text-right">Mot de passe</Label>
                            <Input
                                id="password"
                                type="password"
                                value={newUser.password}
                                onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
                                className="col-span-3"
                            />
                        </div>
                    </div>
                    <DialogFooter>
                        <Button onClick={handleCreateUser}>Créer et Ajouter</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* MODAL ASSIGNATION ROLE */}
            <Dialog open={assignRoleModalOpen} onOpenChange={setAssignRoleModalOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Assigner un rôle à {selectedUser?.username}</DialogTitle>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label className="text-right">Rôle</Label>
                            <Select onValueChange={setSelectedRole}>
                                <SelectTrigger className="col-span-3">
                                    <SelectValue placeholder="Sélectionner un rôle" />
                                </SelectTrigger>
                                <SelectContent>
                                    {roles.map(role => (
                                        <SelectItem key={role.id} value={role.id.toString()}>
                                            {role.name}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                        <p className="text-sm text-gray-500 text-center">
                            Le rôle sera assigné sur la ressource racine : {rootResource?.name}
                        </p>
                    </div>
                    <DialogFooter>
                        <Button onClick={handleAssignRole}>Assigner</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
