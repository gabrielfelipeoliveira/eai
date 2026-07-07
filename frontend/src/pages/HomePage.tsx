import { Box, Typography } from '@mui/material';

export function HomePage() {
  return (
    <Box sx={{ display: 'grid', gap: 2 }}>
      <Typography component="h1" variant="h3" fontWeight={700}>
        EAI - Automotive Lead Intelligence
      </Typography>
      <Typography color="text.secondary" variant="body1">
        Foundation for dealership lead management, sales operations, service, and reporting.
      </Typography>
    </Box>
  );
}
