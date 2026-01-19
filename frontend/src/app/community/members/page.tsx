// src/app/community/members/page.tsx
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Search } from 'lucide-react';

const mockMembers = [
  {
    id: '1',
    name: 'Adam Johnson',
    email: 'adam@hello.org',
    role: 'Administrateur',
    department: 'Direction',
    avatar: '',
  },
  {
    id: '2',
    name: 'Marie Dupont',
    email: 'marie@hello.org',
    role: 'Manager',
    department: 'Ressources Humaines',
    avatar: '',
  },
  {
    id: '3',
    name: 'Jean Martin',
    email: 'jean@hello.org',
    role: 'Manager',
    department: 'Marketing',
    avatar: '',
  },
  {
    id: '4',
    name: 'Sophie Bernard',
    email: 'sophie@hello.org',
    role: 'Manager',
    department: 'Recrutement',
    avatar: '',
  },
  {
    id: '5',
    name: 'Lucas Moreau',
    email: 'lucas@hello.org',
    role: 'Membre',
    department: 'IT',
    avatar: '',
  },
];

export default function MembersPage() {
  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-4xl font-bold text-gray-900">Membres</h1>
        <p className="text-xl text-gray-600 mt-4">
          Découvrez tous les membres de votre organisation
        </p>
      </div>

      {/* Search Bar */}
      <div className="max-w-md">
        <div className="relative">
          <Search className="absolute left-3 top-3 h-5 w-5 text-gray-400" />
          <Input 
            placeholder="Rechercher un membre..." 
            className="pl-10"
          />
        </div>
      </div>

      {/* Members Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {mockMembers.map((member) => (
          <Card key={member.id} className="hover:shadow-lg transition">
            <CardContent className="pt-6">
              <div className="flex items-center gap-4">
                <Avatar className="h-16 w-16">
                  <AvatarImage src={member.avatar} />
                  <AvatarFallback className="text-xl">
                    {member.name.split(' ').map(n => n[0]).join('')}
                  </AvatarFallback>
                </Avatar>
                <div className="flex-1">
                  <h3 className="text-lg font-semibold">{member.name}</h3>
                  <p className="text-sm text-gray-600">{member.email}</p>
                  <div className="mt-2">
                    <p className="text-sm font-medium text-blue-600">{member.role}</p>
                    <p className="text-sm text-gray-500">{member.department}</p>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}