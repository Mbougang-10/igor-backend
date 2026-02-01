export function verifyAuth(): boolean {
  if (typeof window === 'undefined') return false;

  const token = localStorage.getItem('access_token');
  return !!token;
}

export function getAuthToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('access_token');
}

export function clearAuth(): void {
  if (typeof window === 'undefined') return;
  localStorage.removeItem('access_token');
  localStorage.removeItem('user_role');
}