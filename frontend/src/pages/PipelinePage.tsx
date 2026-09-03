import AssignmentIndIcon from '@mui/icons-material/AssignmentInd';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import {
  Alert,
  Box,
  Chip,
  Divider,
  LinearProgress,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { isAxiosError } from 'axios';
import { useMemo, useState } from 'react';
import { CommercialFlowNavigation } from '../components/CommercialFlowNavigation';
import { PageHeader } from '../components/PageHeader';
import { LeadDetailDrawer } from '../features/leads/LeadDetailDrawer';
import { vehicleLabel } from '../features/leads/leadDisplay';
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
      <PageHeader
        action={<CommercialFlowNavigation current="pipeline" />}
        description="Kanban operacional por etapa do funil."
        title="Pipeline"
      />

      {(pipelineQuery.isLoading || fallbackLeadsQuery.isLoading) && <LinearProgress />}
      {pipelineQuery.isError && (
        <Alert severity="warning">
          {pipelineErrorMessage(pipelineQuery.error)} Exibindo leads pela listagem operacional.
        </Alert>
      )}

      <Box
        sx={{
          alignItems: 'stretch',
          display: 'grid',
          gap: 2,
          gridAutoColumns: { xs: 'minmax(280px, 86vw)', md: 320 },
          gridAutoFlow: 'column',
          overflowX: 'auto',
          pb: 1,
          scrollPaddingInline: 16,
          scrollSnapType: 'x proximity',
        }}
      >
        {statuses.map((status) => {
          const leads = groupedLeads[status];
          const isOver = dragOverStatus === status;
          const statusLabel = metadata.label('leadStatuses', status);
          return (
            <Paper
              key={status}
              aria-label={`Etapa ${statusLabel}`}
              component="section"
              onDragOver={(event) => {
                event.preventDefault();
                setDragOverStatus(status);
              }}
              onDrop={() => dropOnStatus(status)}
              variant="outlined"
              sx={{
                bgcolor: isOver ? 'action.hover' : 'background.paper',
                borderColor: isOver ? 'primary.main' : 'divider',
                borderStyle: isOver ? 'dashed' : 'solid',
                borderRadius: 1,
                display: 'grid',
                gap: 1.25,
                gridTemplateRows: 'auto 1fr',
                minHeight: { xs: 440, md: 520 },
                p: 1,
                scrollSnapAlign: 'start',
              }}
            >
              <Box
                sx={{
                  alignItems: 'center',
                  bgcolor: 'background.paper',
                  borderBottom: 1,
                  borderColor: 'divider',
                  display: 'flex',
                  gap: 1,
                  justifyContent: 'space-between',
                  mx: -1,
                  px: 1.25,
                  pb: 1,
                  position: 'sticky',
                  top: 0,
                  zIndex: 1,
                }}
              >
                <Chip color={metadata.color('leadStatuses', status)} label={statusLabel} size="small" />
                <Chip label={leads.length} size="small" variant="outlined" />
              </Box>

              <Stack spacing={1} sx={{ minWidth: 0 }}>
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
                    sx={{
                      borderColor: draggedLead?.id === lead.id ? 'primary.main' : 'divider',
                      borderRadius: 1,
                      cursor: 'pointer',
                      p: 1.25,
                      transition: 'border-color 120ms ease, box-shadow 120ms ease, transform 120ms ease',
                      '&:hover': {
                        boxShadow: 2,
                        transform: 'translateY(-1px)',
                      },
                    }}
                  >
                    <Typography sx={{ overflowWrap: 'anywhere' }} variant="body2" fontWeight={800}>
                      {lead.customerName}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" display="block" sx={{ overflowWrap: 'anywhere' }}>
                      {vehicleLabel(lead)}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" display="block" sx={{ overflowWrap: 'anywhere' }}>
                      {lead.customerPhone ?? lead.customerEmail ?? 'Sem contato'}
                    </Typography>
                    <Divider sx={{ my: 1 }} />
                    <Stack direction="row" flexWrap="wrap" gap={0.75} sx={{ mt: 1 }}>
                      <Chip label={metadata.label('leadSources', lead.source)} size="small" variant="outlined" />
                      {lead.assignedToUserId && <Chip icon={<AssignmentIndIcon />} label="Atribuido" size="small" variant="outlined" />}
                      {(lead.overdueToAssign || lead.overdueToFirstContact) && (
                        <Chip color="error" icon={<WarningAmberIcon />} label="SLA" size="small" variant="outlined" />
                      )}
                    </Stack>
                  </Paper>
                ))}
                {!pipelineQuery.isLoading && !fallbackLeadsQuery.isLoading && leads.length === 0 && (
                  <Box
                    sx={{
                      alignItems: 'center',
                      border: 1,
                      borderColor: 'divider',
                      borderRadius: 1,
                      borderStyle: 'dashed',
                      display: 'flex',
                      minHeight: 96,
                      px: 2,
                    }}
                  >
                    <Typography color="text.secondary" variant="body2">
                      Sem leads nesta etapa.
                    </Typography>
                  </Box>
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
