// typescript
import axios from 'axios';
import type { AxiosRequestConfig, AxiosError } from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8086';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Intercepteur pour injecter le JWT (sûr pour SSR et typé)
api.interceptors.request.use((config: AxiosRequestConfig) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers = {
        ...(config.headers || {}),
        Authorization: `Bearer ${token}`,
      };
    }
  }
  return config;
});

// Intercepteur pour gérer les erreurs globales (plus robuste)
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    const status = error.response?.status;
    const data = error.response?.data;

    if (status === 401 && typeof window !== 'undefined') {
      // Token expiré ou invalide
      localStorage.removeItem('access_token');
      // redirection côté client
      window.location.href = '/login';
    }

    return Promise.reject({
      status,
      message: data?.message || error.message || 'Une erreur est survenue',
      data,
      originalError: error,
    });
  }
);
