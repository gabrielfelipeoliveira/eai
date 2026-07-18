import AssignmentIndIcon from '@mui/icons-material/AssignmentInd';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import {
  Alert,
  Box,
  Chip,
  LinearProgress,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { isAxiosError } from 'axios';
import { useMemo, useState } from 'react';
import { LeadDetailDrawer } from '../features/leads/LeadDetailDrawer';
import { useMetadata } from '../hooks/useMetadata';
import { changeLeadStatus, getPipeline, listLeads } from '../services/leadService';
import type { Lead, LeadStatus, PipelineResponse } from '../types/lead';

const statuses: LeadStatus[] = [
  'NEW',
  'AVAILABLE',
  'ASSIGNED',
  'FIRST_CONTACT',
  'IN_NEGOTIATION',
  'VISIT_SCHEDULED',
  'SIMULATING',
  'PROPOSAL_APPROVED',
  'PROPOSAL_SENT',
  'SOLD',
  'LOST',
  'DUPLICATED',
];

export function PipelinePage() {
  const queryClient = useQueryClient();
  const metadata = useMetadata();
  const [draggedLead, setDraggedLead] = useState<Lead | null>(null);
  const [dragOverStatus, setDragOverStatus] = useState<LeadStatus | null>(null);
  const [selectedLead, setSelectedLead] = useState<Lead | null>(null);

  const pipelineQuery = useQuery({
    queryKey: ['pipeline'],
    queryFn: getPipeline,
  });

  const fallbackLeadsQuery = useQuery({
    queryKey: ['pipeline-leads-fallback'],
    queryFn: () => listLeads({ page: 0, size: 100 }),
  });

  const changeStatusMutation = useMutation({
    mutationFn: ({ leadId, status }: { leadId: string; status: LeadStatus }) => changeLeadStatus(leadId, status, 'Alteracao feita no Kanban'),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['pipeline'] }),
        queryClient.invalidateQueries({ queryKey: ['leads'] }),
        queryClient.invalidateQueries({ queryKey: ['lead-dashboard'] }),
      ]);
    },
  });

  function dropOnStatus(status: LeadStatus) {
    if (!draggedLead || draggedLead.status === status) {
      setDraggedLead(null);
      setDragOverStatus(null);
      return;
    }
    changeStatusMutation.mutate({ leadId: draggedLead.id, status });
    setDraggedLead(null);
    setDragOverStatus(null);
  }

  function openLeadDetail(lead: Lead) {
    setSelectedLead(lead);
  }

  const groupedLeads = useMemo(() => {
    const grouped = emptyPipeline();
    const pipelineLeads = statuses.flatMap((status) => pipelineQuery.data?.[status] ?? []);
    const sourceLeads = pipelineLeads.length > 0 ? pipelineLeads : fallbackLeadsQuery.data?.content ?? [];
    sourceLeads.forEach((lead) => grouped[lead.status].push(lead));
    return grouped;
  }, [fallbackLeadsQuery.data?.content, pipelineQuery.data]);

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Box>
        <Typography component="h2" variant="h4" fontWeight={800}>
          Pipeline
        </Typography>
        <Typography color="text.secondary">Kanban operacional por etapa do funil.</Typography>
      </Box>

      {(pipelineQuery.isLoading || fallbackLeadsQuery.isLoading) && <LinearProgress />}
      {pipelineQuery.isError && (
        <Alert severity="warning">
          {pipelineErrorMessage(pipelineQuery.error)} Exibindo leads pela listagem operacional.
        </Alert>
      )}

      <Box sx={{ display: 'flex', gap: 2, overflowX: 'auto', pb: 1 }}>
        {statuses.map((status) => {
          const leads = groupedLeads[status];
          const isOver = dragOverStatus === status;
          return (
            <Paper
              key={status}
              onDragOver={(event) => {
                event.preventDefault();
                setDragOverStatus(status);
              }}
              onDrop={() => dropOnStatus(status)}
              variant="outlined"
              sx={{
                borderColor: isOver ? 'primary.main' : 'divider',
                borderRadius: 1,
                display: 'grid',
                flex: '0 0 300px',
                gap: 1.25,
                p: 1.5,
                bgcolor: isOver ? 'action.hover' : 'background.paper',
                minHeight: 520,
              }}
            >
              <Stack direction="row" alignItems="center" justifyContent="space-between">
                <Chip color={metadata.color('leadStatuses', status)} label={metadata.label('leadStatuses', status)} size="small" />
                <Typography variant="caption" color="text.secondary">
                  {leads.length}
                </Typography>
              </Stack>

              <Stack spacing={1}>
                {leads.map((lead) => (
                  <Paper
                    draggable
                    key={lead.id}
                    onClick={() => openLeadDetail(lead)}
                    onDragEnd={() => {
                      setDraggedLead(null);
                      setDragOverStatus(null);
                    }}
                    onDragStart={() => setDraggedLead(lead)}
                    onKeyDown={(event) => {
                      if (event.key === 'Enter' || event.key === ' ') {
                        event.preventDefault();
                        openLeadDetail(lead);
                      }
                    }}
                    role="button"
                    tabIndex={0}
                    variant="outlined"
                    sx={{ borderRadius: 1, cursor: 'pointer', p: 1.25 }}
                  >
                    <Typography variant="body2" fontWeight={800}>
                      {lead.customerName}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" display="block">
                      {lead.vehicleInterest ?? 'Sem veiculo'}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" display="block">
                      {lead.customerPhone ?? lead.customerEmail ?? 'Sem contato'}
                    </Typography>
                    <Stack direction="row" flexWrap="wrap" gap={0.75} sx={{ mt: 1 }}>
                      {lead.assignedToUserId && <Chip icon={<AssignmentIndIcon />} label="Atribuido" size="small" variant="outlined" />}
                      {(lead.overdueToAssign || lead.overdueToFirstContact) && (
                        <Chip color="error" icon={<WarningAmberIcon />} label="SLA" size="small" variant="outlined" />
                      )}
                    </Stack>
                  </Paper>
                ))}
                {!pipelineQuery.isLoading && !fallbackLeadsQuery.isLoading && leads.length === 0 && (
                  <Typography color="text.secondary" variant="body2">
                    Sem leads nesta etapa.
                  </Typography>
                )}
              </Stack>
            </Paper>
          );
        })}
      </Box>
      <LeadDetailDrawer
        lead={selectedLead}
        onClose={() => setSelectedLead(null)}
        onLeadChanged={setSelectedLead}
        open={Boolean(selectedLead)}
      />
    </Box>
  );
}

function emptyPipeline(): PipelineResponse {
  return statuses.reduce((result, status) => {
    result[status] = [];
    return result;
  }, {} as PipelineResponse);
}

function pipelineErrorMessage(error: unknown) {
  if (isAxiosError(error)) {
    const status = error.response?.status;
    if (status === 404) {
      return 'Endpoint /api/pipeline nao encontrado. Reinicie o backend com a versao atual.';
    }
    if (status === 401) {
      return 'Endpoint /api/pipeline recusou o token de acesso.';
    }
    if (status === 403) {
      return 'Usuario sem permissao para acessar /api/pipeline.';
    }
    if (status) {
      return `Endpoint /api/pipeline retornou erro ${status}.`;
    }
    return 'Nao foi possivel conectar ao endpoint /api/pipeline.';
  }
  return 'Nao foi possivel carregar o endpoint /api/pipeline.';
}
