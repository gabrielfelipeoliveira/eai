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
        gap: 2,
        justifyContent: 'space-between',
      }}
    >
      <Box sx={{ minWidth: 0 }}>
        <Typography
          component="h2"
          fontWeight={800}
          sx={{ fontSize: { xs: '1.75rem', md: '2.125rem' }, lineHeight: 1.15 }}
          variant="h4"
        >
          {title}
        </Typography>
        <Typography color="text.secondary">{description}</Typography>
      </Box>
      {action && <Box sx={{ flexShrink: 0 }}>{action}</Box>}
    </Box>
  );
}
