import DashboardIcon from '@mui/icons-material/Dashboard';
import BusinessIcon from '@mui/icons-material/Business';
import EventIcon from '@mui/icons-material/Event';
import GroupsIcon from '@mui/icons-material/Groups';
import LogoutIcon from '@mui/icons-material/Logout';
import PersonIcon from '@mui/icons-material/Person';
import SettingsIcon from '@mui/icons-material/Settings';
import StorefrontIcon from '@mui/icons-material/Storefront';
import TextSnippetIcon from '@mui/icons-material/TextSnippet';
import ViewKanbanIcon from '@mui/icons-material/ViewKanban';
import EmailIcon from '@mui/icons-material/Email';
import AssessmentIcon from '@mui/icons-material/Assessment';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import ChatIcon from '@mui/icons-material/Chat';
import DoneAllIcon from '@mui/icons-material/DoneAll';
import NotificationsIcon from '@mui/icons-material/Notifications';
import {
  AppBar,
  Badge,
  Box,
  Button,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Menu,
  MenuItem,
  Toolbar,
  Tooltip,
  Typography,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useMetadata } from '../hooks/useMetadata';
import {
  getUnreadNotificationCount,
  listNotifications,
  markAllNotificationsRead,
  markNotificationRead,
} from '../services/notificationService';

const drawerWidth = 248;

export function MainLayout() {
  const { hasAnyRole, logout, user } = useAuth();
  const metadata = useMetadata();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [notificationsAnchor, setNotificationsAnchor] = useState<HTMLElement | null>(null);
  const isAdmin = hasAnyRole(['ADMIN']);

  const unreadCountQuery = useQuery({
    queryKey: ['notifications', 'unread-count'],
    queryFn: getUnreadNotificationCount,
    enabled: isAdmin,
    refetchInterval: 60000,
  });

  const notificationsQuery = useQuery({
    queryKey: ['notifications', 'latest'],
    queryFn: () => listNotifications(true, 20),
    enabled: isAdmin && Boolean(notificationsAnchor),
  });

  const refreshNotifications = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['notifications', 'unread-count'] }),
      queryClient.invalidateQueries({ queryKey: ['notifications', 'latest'] }),
    ]);
  };

  const markReadMutation = useMutation({
    mutationFn: markNotificationRead,
    onSuccess: refreshNotifications,
  });

  const markAllReadMutation = useMutation({
    mutationFn: markAllNotificationsRead,
    onSuccess: refreshNotifications,
  });

  const menuItems = [
    { label: 'Dashboard', path: '/', icon: <DashboardIcon /> },
    { label: 'Leads', path: '/leads', icon: <ViewKanbanIcon /> },
    { label: 'Pipeline', path: '/pipeline', icon: <ViewKanbanIcon /> },
    { label: 'Agenda', path: '/follow-ups', icon: <EventIcon /> },
    ...(hasAnyRole(['ADMIN', 'MANAGER', 'STORE_MANAGER', 'SELLER']) ? [{ label: 'Conversas', path: '/conversations', icon: <ChatIcon /> }] : []),
    ...(hasAnyRole(['ADMIN', 'MANAGER', 'SELLER']) ? [{ label: 'Relatorios', path: '/reports', icon: <AssessmentIcon /> }] : []),
    ...(hasAnyRole(['ADMIN', 'MANAGER']) ? [{ label: 'Atrasados', path: '/leads/overdue', icon: <WarningAmberIcon /> }] : []),
    ...(hasAnyRole(['ADMIN', 'MANAGER'])
      ? [{ label: 'Usuarios', path: '/users', icon: <GroupsIcon /> }]
      : []),
    ...(hasAnyRole(['ADMIN']) ? [{ label: 'Empresas', path: '/companies', icon: <BusinessIcon /> }] : []),
    ...(hasAnyRole(['ADMIN', 'MANAGER']) ? [{ label: 'Lojas', path: '/stores', icon: <StorefrontIcon /> }] : []),
    ...(hasAnyRole(['ADMIN', 'MANAGER']) ? [{ label: 'Templates', path: '/templates', icon: <TextSnippetIcon /> }] : []),
    ...(hasAnyRole(['ADMIN', 'MANAGER']) ? [{ label: 'E-mails', path: '/email-accounts', icon: <EmailIcon /> }] : []),
    ...(hasAnyRole(['ADMIN', 'MANAGER']) ? [{ label: 'Configuracoes', path: '/settings', icon: <SettingsIcon /> }] : []),
  ];

  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }

  const unreadCount = unreadCountQuery.data?.count ?? 0;

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default', display: 'flex' }}>
      <AppBar
        color="inherit"
        elevation={0}
        position="fixed"
        sx={{ borderBottom: 1, borderColor: 'divider', ml: `${drawerWidth}px`, width: `calc(100% - ${drawerWidth}px)` }}
      >
        <Toolbar sx={{ justifyContent: 'space-between' }}>
          <Box>
            <Typography component="span" variant="subtitle2" color="text.secondary">
              EAI
            </Typography>
            <Typography component="h1" variant="h6" fontWeight={700}>
              Operacao comercial
            </Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            {isAdmin && (
              <>
                <Tooltip title="Notificacoes">
                  <IconButton
                    aria-label="Notificacoes"
                    onClick={(event) => setNotificationsAnchor(event.currentTarget)}
                  >
                    <Badge badgeContent={unreadCount} color="error" max={99}>
                      <NotificationsIcon />
                    </Badge>
                  </IconButton>
                </Tooltip>
                <Menu
                  anchorEl={notificationsAnchor}
                  open={Boolean(notificationsAnchor)}
                  onClose={() => setNotificationsAnchor(null)}
                  PaperProps={{ sx: { width: 380, maxWidth: 'calc(100vw - 32px)' } }}
                >
                  <Box sx={{ px: 2, py: 1.25, display: 'flex', justifyContent: 'space-between', gap: 1 }}>
                    <Typography variant="subtitle2" fontWeight={700}>
                      Notificacoes
                    </Typography>
                    <Tooltip title="Marcar todas como lidas">
                      <span>
                        <IconButton
                          aria-label="Marcar todas como lidas"
                          disabled={unreadCount === 0 || markAllReadMutation.isPending}
                          onClick={() => markAllReadMutation.mutate()}
                          size="small"
                        >
                          <DoneAllIcon fontSize="small" />
                        </IconButton>
                      </span>
                    </Tooltip>
                  </Box>
                  <Divider />
                  {notificationsQuery.data?.length ? (
                    notificationsQuery.data.map((notification) => (
                      <MenuItem
                        key={notification.id}
                        onClick={() => markReadMutation.mutate(notification.id)}
                        sx={{ alignItems: 'flex-start', whiteSpace: 'normal', py: 1.25 }}
                      >
                        <Box sx={{ minWidth: 0 }}>
                          <Typography variant="body2" fontWeight={700}>
                            {notification.title}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {notification.message}
                          </Typography>
                        </Box>
                      </MenuItem>
                    ))
                  ) : (
                    <Box sx={{ px: 2, py: 2 }}>
                      <Typography variant="body2" color="text.secondary">
                        Nenhuma notificacao pendente
                      </Typography>
                    </Box>
                  )}
                  {unreadCount > 0 && (
                    <>
                      <Divider />
                      <Box sx={{ p: 1 }}>
                        <Button
                          fullWidth
                          size="small"
                          onClick={() => markAllReadMutation.mutate()}
                          disabled={markAllReadMutation.isPending}
                        >
                          Marcar todas como lidas
                        </Button>
                      </Box>
                    </>
                  )}
                </Menu>
              </>
            )}
            <PersonIcon color="action" />
            <Box sx={{ display: { xs: 'none', sm: 'block' }, minWidth: 0 }}>
              <Typography variant="body2" fontWeight={700} noWrap>
                {user?.name}
              </Typography>
              <Typography variant="caption" color="text.secondary" noWrap>
                {user?.roles.map((role) => metadata.label('userRoles', role)).join(', ')}
              </Typography>
            </Box>
            <Tooltip title="Sair">
              <IconButton aria-label="Sair" onClick={handleLogout}>
                <LogoutIcon />
              </IconButton>
            </Tooltip>
          </Box>
        </Toolbar>
      </AppBar>

      <Drawer
        open
        variant="permanent"
        PaperProps={{
          sx: {
            width: drawerWidth,
            borderRight: 1,
            borderColor: 'divider',
            bgcolor: 'background.paper',
          },
        }}
      >
        <Box sx={{ px: 3, py: 2.5 }}>
          <Typography variant="h6" fontWeight={800}>
            EAI
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Automotive Lead Intelligence
          </Typography>
        </Box>
        <Divider />
        <List sx={{ px: 1.5, py: 2 }}>
          {menuItems.map((item) => (
            <ListItemButton
              key={item.path}
              component={NavLink}
              to={item.path}
              end={item.path === '/'}
              sx={{
                borderRadius: 1,
                mb: 0.5,
                '&.active': {
                  bgcolor: 'primary.main',
                  color: 'primary.contrastText',
                  '& .MuiListItemIcon-root': {
                    color: 'inherit',
                  },
                },
              }}
            >
              <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItemButton>
          ))}
        </List>
      </Drawer>

      <Box component="main" sx={{ flexGrow: 1, pt: 12, px: 4, pb: 5, ml: `${drawerWidth}px` }}>
        <Outlet />
      </Box>
    </Box>
  );
}
