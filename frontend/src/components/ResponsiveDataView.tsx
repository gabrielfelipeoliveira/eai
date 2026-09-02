import { Alert, Box, CircularProgress, Paper, Stack, TableContainer, Typography, useMediaQuery, useTheme } from '@mui/material';
import type { ReactNode } from 'react';

interface ResponsiveDataViewProps {
  cards: ReactNode;
  empty: boolean;
  emptyMessage: string;
  error?: boolean;
  errorMessage?: string;
  loading?: boolean;
  loadingLabel?: string;
  table: ReactNode;
}

export function ResponsiveDataView({
  cards,
  empty,
  emptyMessage,
  error = false,
  errorMessage = 'Nao foi possivel carregar os dados.',
  loading = false,
  loadingLabel = 'Carregando dados',
  table,
}: ResponsiveDataViewProps) {
  const theme = useTheme();
  const isDesktop = useMediaQuery(theme.breakpoints.up('md'), { noSsr: true });

  if (loading) {
    return (
      <Paper variant="outlined" sx={{ borderRadius: 1, p: 3 }}>
        <Stack alignItems="center" spacing={1.5}>
          <CircularProgress aria-label={loadingLabel} size={28} />
          <Typography color="text.secondary" variant="body2">
            {loadingLabel}
          </Typography>
        </Stack>
      </Paper>
    );
  }

  if (error) {
    return <Alert severity="error">{errorMessage}</Alert>;
  }

  if (empty) {
    return (
      <Paper variant="outlined" sx={{ borderRadius: 1, p: 3 }}>
        <Typography color="text.secondary" textAlign="center">
          {emptyMessage}
        </Typography>
      </Paper>
    );
  }

  return (
    <>
      {isDesktop ? (
        <Paper variant="outlined" sx={{ borderRadius: 1, overflow: 'hidden' }}>
          <TableContainer>{table}</TableContainer>
        </Paper>
      ) : (
        <Box sx={{ display: 'grid', gap: 1.5 }}>{cards}</Box>
      )}
    </>
  );
}
