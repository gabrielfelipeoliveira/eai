import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import EventIcon from '@mui/icons-material/Event';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import {
  Box,
  Button,
  Chip,
  Grid2,
  Paper,
  Stack,
  Tab,
  Tabs,
  Typography,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import { useMetadata } from '../hooks/useMetadata';
import { completeFollowUpTask, listFollowUps, listMyFollowUps } from '../services/leadService';
import type { FollowUpTask } from '../types/lead';

export function FollowUpsPage() {
  const queryClient = useQueryClient();
  const metadata = useMetadata();
  const [tab, setTab] = useState<'my' | 'overdue' | 'all'>('my');

  const myQuery = useQuery({
    queryKey: ['follow-ups', 'my'],
    queryFn: listMyFollowUps,
  });

  const allQuery = useQuery({
    queryKey: ['follow-ups'],
    queryFn: listFollowUps,
  });

  const completeMutation = useMutation({
    mutationFn: (id: string) => completeFollowUpTask(id),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['follow-ups'] }),
        queryClient.invalidateQueries({ queryKey: ['lead-history'] }),
      ]);
    },
  });

  const tasks = useMemo(() => {
    if (tab === 'my') {
      return myQuery.data ?? [];
    }
    const source = allQuery.data ?? [];
    if (tab === 'overdue') {
      return source.filter((task) => task.status === 'OVERDUE');
    }
    return source;
  }, [allQuery.data, myQuery.data, tab]);

  const counters = useMemo(() => {
    const mine = myQuery.data ?? [];
    const all = allQuery.data ?? [];
    return {
      my: mine.filter((task) => task.status === 'PENDING' || task.status === 'OVERDUE').length,
      overdue: all.filter((task) => task.status === 'OVERDUE').length,
      all: all.length,
    };
  }, [allQuery.data, myQuery.data]);

  function renderTask(task: FollowUpTask) {
    return (
      <Paper key={task.id} variant="outlined" sx={{ borderRadius: 1, p: 1.5 }}>
        <Stack direction={{ xs: 'column', md: 'row' }} justifyContent="space-between" spacing={1.5}>
          <Box>
            <Stack direction="row" alignItems="center" flexWrap="wrap" gap={1} sx={{ mb: 0.75 }}>
              <Typography fontWeight={800}>{task.title}</Typography>
              <Chip color={metadata.color('followUpStatuses', task.status)} label={metadata.label('followUpStatuses', task.status)} size="small" />
            </Stack>
            <Typography color="text.secondary" variant="body2">
              {task.description ?? 'Sem descricao'}
            </Typography>
            <Typography color="text.secondary" variant="caption">
              Lead {task.leadId} - vencimento {new Date(task.dueAt).toLocaleString('pt-BR')}
            </Typography>
          </Box>
          <Button
            disabled={task.status === 'DONE' || task.status === 'CANCELED' || completeMutation.isPending}
            onClick={() => completeMutation.mutate(task.id)}
            startIcon={<CheckCircleIcon />}
            variant="outlined"
          >
            Concluir
          </Button>
        </Stack>
      </Paper>
    );
  }

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Box>
        <Typography component="h2" variant="h4" fontWeight={800}>
          Agenda
        </Typography>
        <Typography color="text.secondary">Follow-ups, minhas tarefas e itens atrasados.</Typography>
      </Box>

      <Grid2 container spacing={1.5}>
        <Grid2 size={{ xs: 12, md: 4 }}>
          <Paper variant="outlined" sx={{ borderRadius: 1, p: 1.5 }}>
            <Stack direction="row" alignItems="center" spacing={1}>
              <EventIcon color="primary" />
              <Box>
                <Typography variant="caption" color="text.secondary">Minhas tarefas abertas</Typography>
                <Typography variant="h5" fontWeight={800}>{counters.my}</Typography>
              </Box>
            </Stack>
          </Paper>
        </Grid2>
        <Grid2 size={{ xs: 12, md: 4 }}>
          <Paper variant="outlined" sx={{ borderColor: counters.overdue ? 'error.main' : 'divider', borderRadius: 1, p: 1.5 }}>
            <Stack direction="row" alignItems="center" spacing={1}>
              <WarningAmberIcon color={counters.overdue ? 'error' : 'disabled'} />
              <Box>
                <Typography variant="caption" color="text.secondary">Tarefas atrasadas</Typography>
                <Typography color={counters.overdue ? 'error.main' : 'text.primary'} variant="h5" fontWeight={800}>{counters.overdue}</Typography>
              </Box>
            </Stack>
          </Paper>
        </Grid2>
        <Grid2 size={{ xs: 12, md: 4 }}>
          <Paper variant="outlined" sx={{ borderRadius: 1, p: 1.5 }}>
            <Typography variant="caption" color="text.secondary">Agenda total</Typography>
            <Typography variant="h5" fontWeight={800}>{counters.all}</Typography>
          </Paper>
        </Grid2>
      </Grid2>

      <Paper variant="outlined" sx={{ borderRadius: 1 }}>
        <Tabs onChange={(_, value) => setTab(value)} value={tab}>
          <Tab label="Minhas tarefas" value="my" />
          <Tab label="Atrasadas" value="overdue" />
          <Tab label="Todas" value="all" />
        </Tabs>
      </Paper>

      <Stack spacing={1.25}>
        {tasks.map(renderTask)}
        {!myQuery.isLoading && !allQuery.isLoading && tasks.length === 0 && (
          <Paper variant="outlined" sx={{ borderRadius: 1, p: 2 }}>
            <Typography color="text.secondary">Nenhum follow-up encontrado.</Typography>
          </Paper>
        )}
      </Stack>
    </Box>
  );
}
