// typescript
import axios from 'axios';
import type { InternalAxiosRequestConfig, AxiosError, AxiosResponse } from 'axios';

// Utiliser 127.0.0.1 au lieu de localhost pour éviter les problèmes de résolution IPv4/IPv6
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://127.0.0.1:8086';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  // Timeout de 10 secondes
  timeout: 10000,
});

// Intercepteur pour injecter le JWT (sûr pour SSR et typé)
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers.set('Authorization', `Bearer ${token}`);
    }
  }
  return config;
});

// Intercepteur pour gérer les erreurs globales (plus robuste)
api.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError) => {
    const status = error.response?.status;
    const data = error.response?.data as any; // Cast explicite pour éviter les erreurs de type

    // Gestion spécifique de l'erreur "Network Error"
    if (error.message === 'Network Error' && !error.response) {
      return Promise.reject({
        status: 0,
        message: "Impossible de contacter le serveur. Vérifiez que le backend (port 8086) est démarré.",
        originalError: error,
      });
    }

    if (status === 401 && typeof window !== 'undefined') {
      // Token expiré ou invalide
      localStorage.removeItem('access_token');
      // redirection côté client vers login si on n'y est pas déjà
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login';
      }
    }

    return Promise.reject({
      status,
      message: data?.message || error.message || 'Une erreur est survenue',
      data,
      originalError: error,
    });
  }
);
