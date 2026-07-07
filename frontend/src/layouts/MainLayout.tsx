import { Box, Container, Typography } from '@mui/material';
import { Outlet } from 'react-router-dom';

export function MainLayout() {
  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <Box component="header" sx={{ borderBottom: 1, borderColor: 'divider', bgcolor: 'background.paper' }}>
        <Container maxWidth="lg" sx={{ py: 2 }}>
          <Typography component="span" variant="subtitle1" fontWeight={700}>
            EAI
          </Typography>
        </Container>
      </Box>
      <Container component="main" maxWidth="lg" sx={{ py: 6 }}>
        <Outlet />
      </Container>
    </Box>
  );
}
