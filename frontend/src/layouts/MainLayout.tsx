import DashboardIcon from '@mui/icons-material/Dashboard';
import BusinessIcon from '@mui/icons-material/Business';
import GroupsIcon from '@mui/icons-material/Groups';
import LogoutIcon from '@mui/icons-material/Logout';
import PersonIcon from '@mui/icons-material/Person';
import SettingsIcon from '@mui/icons-material/Settings';
import StorefrontIcon from '@mui/icons-material/Storefront';
import TextSnippetIcon from '@mui/icons-material/TextSnippet';
import ViewKanbanIcon from '@mui/icons-material/ViewKanban';
import EmailIcon from '@mui/icons-material/Email';
import {
  AppBar,
  Box,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Tooltip,
  Typography,
} from '@mui/material';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

const drawerWidth = 248;

export function MainLayout() {
  const { hasAnyRole, logout, user } = useAuth();
  const navigate = useNavigate();

  const menuItems = [
    { label: 'Dashboard', path: '/', icon: <DashboardIcon /> },
    { label: 'Leads', path: '/leads', icon: <ViewKanbanIcon /> },
    ...(hasAnyRole(['ADMIN', 'MANAGER'])
      ? [{ label: 'Usuarios', path: '/users', icon: <GroupsIcon /> }]
      : []),
    ...(hasAnyRole(['ADMIN']) ? [{ label: 'Empresas', path: '/companies', icon: <BusinessIcon /> }] : []),
    ...(hasAnyRole(['ADMIN', 'MANAGER']) ? [{ label: 'Lojas', path: '/stores', icon: <StorefrontIcon /> }] : []),
    ...(hasAnyRole(['ADMIN', 'MANAGER']) ? [{ label: 'Templates', path: '/templates', icon: <TextSnippetIcon /> }] : []),
    ...(hasAnyRole(['ADMIN', 'MANAGER']) ? [{ label: 'E-mails', path: '/email-accounts', icon: <EmailIcon /> }] : []),
    { label: 'Configuracoes', path: '/settings', icon: <SettingsIcon /> },
  ];

  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }

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
            <PersonIcon color="action" />
            <Box sx={{ display: { xs: 'none', sm: 'block' }, minWidth: 0 }}>
              <Typography variant="body2" fontWeight={700} noWrap>
                {user?.name}
              </Typography>
              <Typography variant="caption" color="text.secondary" noWrap>
                {user?.roles.join(', ')}
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
