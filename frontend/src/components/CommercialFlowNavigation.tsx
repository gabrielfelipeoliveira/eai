import ChatIcon from '@mui/icons-material/Chat';
import FormatListBulletedIcon from '@mui/icons-material/FormatListBulleted';
import ViewKanbanIcon from '@mui/icons-material/ViewKanban';
import { Button, Stack } from '@mui/material';
import type { ReactElement } from 'react';
import { Link as RouterLink } from 'react-router';

type CommercialFlowSection = 'conversations' | 'leads' | 'pipeline';

interface CommercialFlowNavigationProps {
  current: CommercialFlowSection;
}

const sections = [
  { id: 'leads', label: 'Leads', path: '/leads', icon: <FormatListBulletedIcon /> },
  { id: 'pipeline', label: 'Pipeline', path: '/pipeline', icon: <ViewKanbanIcon /> },
  { id: 'conversations', label: 'Conversas', path: '/conversations', icon: <ChatIcon /> },
] satisfies Array<{ id: CommercialFlowSection; icon: ReactElement; label: string; path: string }>;

export function CommercialFlowNavigation({ current }: CommercialFlowNavigationProps) {
  return (
    <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
      {sections
        .filter((section) => section.id !== current)
        .map((section) => (
          <Button
            key={section.id}
            component={RouterLink}
            startIcon={section.icon}
            to={section.path}
            variant="outlined"
          >
            {section.label}
          </Button>
        ))}
    </Stack>
  );
}
