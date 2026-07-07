import { createBrowserRouter } from 'react-router-dom';
import { ProtectedRoute } from '../components/ProtectedRoute';
import { MainLayout } from '../layouts/MainLayout';
import { LoginPage } from '../pages/LoginPage';
import { HomePage } from '../pages/HomePage';
import { LeadsPage } from '../pages/LeadsPage';
import { UsersPage } from '../pages/UsersPage';
import { CompaniesPage } from '../pages/CompaniesPage';
import { StoresPage } from '../pages/StoresPage';
import { TemplatesPage } from '../pages/TemplatesPage';
import { EmailAccountsPage } from '../pages/EmailAccountsPage';
import { DistributionSettingsPage } from '../pages/DistributionSettingsPage';
import { OverdueLeadsPage } from '../pages/OverdueLeadsPage';
import { PipelinePage } from '../pages/PipelinePage';
import { FollowUpsPage } from '../pages/FollowUpsPage';
import { ReportsPage } from '../pages/ReportsPage';

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
            element: <LeadsPage />,
          },
          {
            path: 'pipeline',
            element: <PipelinePage />,
          },
          {
            path: 'follow-ups',
            element: <FollowUpsPage />,
          },
          {
            path: 'reports',
            element: <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'SELLER', 'AUDITOR']} />,
            children: [
              {
                index: true,
                element: <ReportsPage />,
              },
            ],
          },
          {
            path: 'leads/overdue',
            element: <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />,
            children: [
              {
                index: true,
                element: <OverdueLeadsPage />,
              },
            ],
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
            path: 'templates',
            element: <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />,
            children: [
              {
                index: true,
                element: <TemplatesPage />,
              },
            ],
          },
          {
            path: 'email-accounts',
            element: <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />,
            children: [
              {
                index: true,
                element: <EmailAccountsPage />,
              },
            ],
          },
          {
            path: 'settings',
            element: <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />,
            children: [
              {
                index: true,
                element: <DistributionSettingsPage />,
              },
            ],
          },
        ],
      },
    ],
  },
]);
