// src/hooks/useRole.ts
'use client';

import { useState, useEffect } from 'react';

export type Role = 'admin' | 'manager' | 'member';

export function useRole(): Role {
  const [role, setRole] = useState<Role>('member');

  useEffect(() => {
    const stored = localStorage.getItem('userRole') as Role;
    if (stored && ['admin', 'manager', 'member'].includes(stored)) {
      setRole(stored);
    }
  }, []);

  return role;
}