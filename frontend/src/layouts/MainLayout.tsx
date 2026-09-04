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
import MenuIcon from '@mui/icons-material/Menu';
import NotificationsIcon from '@mui/icons-material/Notifications';
import {
  AppBar,
  Avatar,
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
import { NavLink, Outlet, useNavigate } from 'react-router';
import { useAuth } from '../hooks/useAuth';
import { useMetadata } from '../hooks/useMetadata';
import {
  getUnreadNotificationCount,
  listNotifications,
  markAllNotificationsRead,
  markNotificationRead,
} from '../services/notificationService';

const drawerWidth = 264;
const sidebarColor = '#0f172a';
const sidebarMutedColor = '#94a3b8';

export function MainLayout() {
  const { hasAnyRole, logout, user } = useAuth();
  const metadata = useMetadata();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
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

  const navigation = (
    <>
      <Box sx={{ px: 2.5, py: 2.25 }}>
        <Typography color="common.white" variant="h6" fontWeight={800} letterSpacing={0}>
          EAI
        </Typography>
        <Typography variant="caption" sx={{ color: sidebarMutedColor, display: 'block', lineHeight: 1.25 }}>
          Automotive Lead Intelligence
        </Typography>
      </Box>
      <Divider sx={{ borderColor: 'rgba(148, 163, 184, 0.2)' }} />
      <List sx={{ px: 1.25, py: 1.5 }}>
        {menuItems.map((item) => (
          <ListItemButton
            key={item.path}
            component={NavLink}
            to={item.path}
            end={item.path === '/'}
            onClick={() => setMobileNavOpen(false)}
            sx={{
              borderRadius: 1,
              color: sidebarMutedColor,
              minHeight: 42,
              mb: 0.25,
              px: 1.25,
              '& .MuiListItemIcon-root': {
                color: 'inherit',
              },
              '&.active': {
                bgcolor: 'primary.main',
                color: 'primary.contrastText',
                '& .MuiListItemIcon-root': {
                  color: 'inherit',
                },
              },
              '&:hover': {
                bgcolor: 'rgba(148, 163, 184, 0.12)',
                color: 'common.white',
              },
            }}
          >
            <ListItemIcon sx={{ minWidth: 34 }}>{item.icon}</ListItemIcon>
            <ListItemText primary={item.label} primaryTypographyProps={{ fontSize: 14, fontWeight: 700 }} />
          </ListItemButton>
        ))}
      </List>
    </>
  );

  const unreadCount = unreadCountQuery.data?.count ?? 0;

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default', display: 'flex' }}>
      <AppBar
        color="inherit"
        elevation={0}
        position="fixed"
        sx={{
          bgcolor: 'background.paper',
          borderBottom: 1,
          borderColor: 'divider',
          ml: { md: `${drawerWidth}px` },
          width: { xs: '100%', md: `calc(100% - ${drawerWidth}px)` },
        }}
      >
        <Toolbar sx={{ gap: 2, minHeight: { xs: 64, md: 68 }, justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, minWidth: 0 }}>
            <Tooltip title="Abrir menu">
              <IconButton
                aria-label="Abrir menu"
                edge="start"
                onClick={() => setMobileNavOpen(true)}
                sx={{ display: { md: 'none' } }}
              >
                <MenuIcon />
              </IconButton>
            </Tooltip>
            <Box sx={{ minWidth: 0 }}>
              <Typography component="span" variant="caption" color="text.secondary" fontWeight={700}>
                EAI
              </Typography>
              <Typography component="h1" variant="h6" noWrap>
                Operacao comercial
              </Typography>
            </Box>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
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
            <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main', fontSize: 14, fontWeight: 700 }}>
              {user?.name?.charAt(0).toUpperCase() ?? <PersonIcon fontSize="small" />}
            </Avatar>
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
        onClose={() => setMobileNavOpen(false)}
        open={mobileNavOpen}
        variant="temporary"
        ModalProps={{ keepMounted: true }}
        PaperProps={{
          sx: {
            width: drawerWidth,
            borderRight: 1,
            borderColor: 'divider',
            bgcolor: sidebarColor,
          },
        }}
        sx={{ display: { xs: 'block', md: 'none' } }}
      >
        {navigation}
      </Drawer>

      <Drawer
        open
        variant="permanent"
        PaperProps={{
          sx: {
            width: drawerWidth,
            borderRight: 1,
            borderColor: 'divider',
            bgcolor: sidebarColor,
          },
        }}
        sx={{ display: { xs: 'none', md: 'block' } }}
      >
        {navigation}
      </Drawer>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          minWidth: 0,
          ml: { md: `${drawerWidth}px` },
          maxWidth: { md: `calc(100vw - ${drawerWidth}px)` },
          px: { xs: 2, sm: 3, md: 4 },
          pb: { xs: 3, md: 5 },
          pt: { xs: 9.5, md: 11 },
        }}
      >
        <Outlet />
      </Box>
    </Box>
  );
}
