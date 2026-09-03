import { Box, Typography } from '@mui/material';
import type { ReactNode } from 'react';

interface PageHeaderProps {
  action?: ReactNode;
  description: string;
  title: string;
}

export function PageHeader({ action, description, title }: PageHeaderProps) {
  return (
    <Box
      sx={{
        alignItems: { xs: 'stretch', sm: 'flex-start' },
        display: 'flex',
        flexDirection: { xs: 'column', sm: 'row' },
        gap: 1.5,
        justifyContent: 'space-between',
        minWidth: 0,
      }}
    >
      <Box sx={{ minWidth: 0 }}>
        <Typography
          component="h2"
          fontWeight={700}
          sx={{ fontSize: { xs: '1.5rem', md: '1.75rem' }, lineHeight: 1.2, overflowWrap: 'anywhere' }}
          variant="h4"
        >
          {title}
        </Typography>
        <Typography color="text.secondary" sx={{ maxWidth: 720, overflowWrap: 'anywhere' }} variant="body2">
          {description}
        </Typography>
      </Box>
      {action && <Box sx={{ flexShrink: 0 }}>{action}</Box>}
    </Box>
  );
}
