import { Box, CircularProgress } from '@mui/material';
import { lazy, Suspense } from 'react';
import type { ReactNode } from 'react';
import { createBrowserRouter } from 'react-router-dom';
import { ProtectedRoute } from '../components/ProtectedRoute';
import { MainLayout } from '../layouts/MainLayout';

const LoginPage = lazy(() => import('../pages/LoginPage').then((module) => ({ default: module.LoginPage })));
const HomePage = lazy(() => import('../pages/HomePage').then((module) => ({ default: module.HomePage })));
const LeadsPage = lazy(() => import('../pages/LeadsPage').then((module) => ({ default: module.LeadsPage })));
const UsersPage = lazy(() => import('../pages/UsersPage').then((module) => ({ default: module.UsersPage })));
const CompaniesPage = lazy(() => import('../pages/CompaniesPage').then((module) => ({ default: module.CompaniesPage })));
const StoresPage = lazy(() => import('../pages/StoresPage').then((module) => ({ default: module.StoresPage })));
const TemplatesPage = lazy(() => import('../pages/TemplatesPage').then((module) => ({ default: module.TemplatesPage })));
const EmailAccountsPage = lazy(() =>
  import('../pages/EmailAccountsPage').then((module) => ({ default: module.EmailAccountsPage })),
);
const SettingsPage = lazy(() => import('../pages/SettingsPage').then((module) => ({ default: module.SettingsPage })));
const OverdueLeadsPage = lazy(() =>
  import('../pages/OverdueLeadsPage').then((module) => ({ default: module.OverdueLeadsPage })),
);
const PipelinePage = lazy(() => import('../pages/PipelinePage').then((module) => ({ default: module.PipelinePage })));
const FollowUpsPage = lazy(() => import('../pages/FollowUpsPage').then((module) => ({ default: module.FollowUpsPage })));
const ReportsPage = lazy(() => import('../pages/ReportsPage').then((module) => ({ default: module.ReportsPage })));
const ConversationsPage = lazy(() =>
  import('../pages/ConversationsPage').then((module) => ({ default: module.ConversationsPage })),
);

function page(element: ReactNode) {
  return (
    <Suspense
      fallback={
        <Box sx={{ display: 'grid', minHeight: 240, placeItems: 'center' }}>
          <CircularProgress aria-label="Carregando" />
        </Box>
      }
    >
      {element}
    </Suspense>
  );
}

export const router = createBrowserRouter([
  {
    path: '/login',
    element: page(<LoginPage />),
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
            element: page(<HomePage />),
          },
          {
            path: 'leads',
            element: page(<LeadsPage />),
          },
          {
            path: 'pipeline',
            element: page(<PipelinePage />),
          },
          {
            path: 'follow-ups',
            element: page(<FollowUpsPage />),
          },
        {
          path: 'conversations',
          element: <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'STORE_MANAGER', 'SELLER']} />,
          children: [
              {
                index: true,
                element: page(<ConversationsPage />),
              },
            ],
          },
          {
            path: 'reports',
            element: <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'SELLER']} />,
            children: [
              {
                index: true,
                element: page(<ReportsPage />),
              },
            ],
          },
          {
            path: 'leads/overdue',
            element: <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />,
            children: [
              {
                index: true,
                element: page(<OverdueLeadsPage />),
              },
            ],
          },
          {
            path: 'users',
            element: <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />,
            children: [
              {
                index: true,
                element: page(<UsersPage />),
              },
            ],
          },
          {
            path: 'companies',
            element: <ProtectedRoute allowedRoles={['ADMIN']} />,
            children: [
              {
                index: true,
                element: page(<CompaniesPage />),
              },
            ],
          },
          {
            path: 'stores',
            element: <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />,
            children: [
              {
                index: true,
                element: page(<StoresPage />),
              },
            ],
          },
          {
            path: 'templates',
            element: <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />,
            children: [
              {
                index: true,
                element: page(<TemplatesPage />),
              },
            ],
          },
          {
            path: 'email-accounts',
            element: <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />,
            children: [
              {
                index: true,
                element: page(<EmailAccountsPage />),
              },
            ],
          },
          {
            path: 'settings',
            element: <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />,
            children: [
              {
                index: true,
                element: page(<SettingsPage />),
              },
            ],
          },
        ],
      },
    ],
  },
]);
