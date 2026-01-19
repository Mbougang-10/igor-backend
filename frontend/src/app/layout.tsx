// src/app/layout.tsx
import './globals.css';

export const metadata = {
  title: 'Gestionnaire de Communauté',
  description: 'Système de gestion multi-tenant hiérarchique',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="fr">
      <body>{children}</body>
    </html>
  );
}