import { Box, Divider, Paper, Stack, Typography } from '@mui/material';
import type { ReactNode } from 'react';

interface RecordCardProps {
  actions?: ReactNode;
  children?: ReactNode;
  status?: ReactNode;
  subtitle?: ReactNode;
  title: ReactNode;
}

interface RecordCardRowProps {
  label: string;
  value: ReactNode;
}

export function RecordCard({ actions, children, status, subtitle, title }: RecordCardProps) {
  return (
    <Paper variant="outlined" sx={{ borderRadius: 1, p: 2 }}>
      <Stack spacing={1.5}>
        <Box sx={{ alignItems: 'flex-start', display: 'flex', gap: 1.5, justifyContent: 'space-between' }}>
          <Box sx={{ minWidth: 0 }}>
            <Typography component="div" fontWeight={700} sx={{ overflowWrap: 'anywhere' }} variant="body1">
              {title}
            </Typography>
            {subtitle && (
              <Typography color="text.secondary" component="div" sx={{ overflowWrap: 'anywhere' }} variant="body2">
                {subtitle}
              </Typography>
            )}
          </Box>
          {status && <Box sx={{ flexShrink: 0 }}>{status}</Box>}
        </Box>
        {children && (
          <>
            <Divider />
            <Stack spacing={1}>{children}</Stack>
          </>
        )}
        {actions && (
          <>
            <Divider />
            <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>{actions}</Box>
          </>
        )}
      </Stack>
    </Paper>
  );
}

export function RecordCardRow({ label, value }: RecordCardRowProps) {
  return (
    <Box sx={{ display: 'grid', gap: 0.25 }}>
      <Typography color="text.secondary" variant="caption">
        {label}
      </Typography>
      <Typography sx={{ overflowWrap: 'anywhere' }} variant="body2">
        {value}
      </Typography>
    </Box>
  );
}
