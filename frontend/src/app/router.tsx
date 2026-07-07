import { createBrowserRouter } from 'react-router-dom';
import { ProtectedRoute } from '../components/ProtectedRoute';
import { MainLayout } from '../layouts/MainLayout';
import { LoginPage } from '../pages/LoginPage';
import { HomePage } from '../pages/HomePage';
import { UsersPage } from '../pages/UsersPage';
import { CompaniesPage } from '../pages/CompaniesPage';
import { StoresPage } from '../pages/StoresPage';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    element: <ProtectedRoute />,
    children: [
      {
        path: '/',
        element: <MainLayout />,
        children: [
          {
            index: true,
            element: <HomePage />,
          },
          {
            path: 'leads',
            element: <HomePage title="Leads" />,
          },
          {
            path: 'users',
            element: <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />,
            children: [
              {
                index: true,
                element: <UsersPage />,
              },
            ],
          },
          {
            path: 'companies',
            element: <ProtectedRoute allowedRoles={['ADMIN']} />,
            children: [
              {
                index: true,
                element: <CompaniesPage />,
              },
            ],
          },
          {
            path: 'stores',
            element: <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />,
            children: [
              {
                index: true,
                element: <StoresPage />,
              },
            ],
          },
          {
            path: 'settings',
            element: <HomePage title="Configuracoes" />,
          },
        ],
      },
    ],
  },
]);
