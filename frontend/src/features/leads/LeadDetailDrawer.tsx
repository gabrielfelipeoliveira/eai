import { zodResolver } from '@hookform/resolvers/zod';
import AssignmentIndIcon from '@mui/icons-material/AssignmentInd';
import AutoModeIcon from '@mui/icons-material/AutoMode';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CloseIcon from '@mui/icons-material/Close';
import EventIcon from '@mui/icons-material/Event';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import NotesIcon from '@mui/icons-material/Notes';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import WhatsAppIcon from '@mui/icons-material/WhatsApp';
import {
  Alert,
  Box,
  Button,
  Chip,
  Divider,
  Drawer,
  Grid2,
  IconButton,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { vehicleLabel } from './leadDisplay';
import { useAuth } from '../../hooks/useAuth';
import { useMetadata } from '../../hooks/useMetadata';
import { apiErrorMessage } from '../../services/api';
import { listCompanies } from '../../services/companyService';
import {
  addLeadNote,
  addLeadTag,
  assignLeadAutomatically,
  assignLeadToMe,
  changeLeadStatus,
  completeFollowUpTask,
  createFollowUpTask,
  deleteLeadTag,
  listLeadFollowUps,
  listLeadHistory,
  listLeadNotes,
  listLeadTags,
} from '../../services/leadService';
import { listStores } from '../../services/storeService';
import { generateWhatsappLink, listActiveTemplates, listLeadCommunications } from '../../services/templateService';
import { listUsers } from '../../services/userService';
import type { Lead, LeadStatus } from '../../types/lead';
import type { MessageTemplate } from '../../types/message';

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

const followUpSchema = z.object({
  title: z.string().min(1, 'Informe o titulo').max(160),
  description: z.string().max(1000),
  dueAt: z.string().min(1, 'Informe o vencimento'),
});

type FollowUpFormValues = z.infer<typeof followUpSchema>;

interface LeadDetailDrawerProps {
  lead: Lead | null;
  onClose: () => void;
  onLeadChanged?: (lead: Lead) => void;
  open: boolean;
}

export function LeadDetailDrawer({ lead, onClose, onLeadChanged, open }: LeadDetailDrawerProps) {
  const { hasAnyRole, user } = useAuth();
  const metadata = useMetadata();
  const queryClient = useQueryClient();
  const [selectedLead, setSelectedLead] = useState<Lead | null>(lead);
  const [noteText, setNoteText] = useState('');
  const [tagText, setTagText] = useState('');
  const [selectedTemplateId, setSelectedTemplateId] = useState('');
  const canListUsers = hasAnyRole(['ADMIN', 'MANAGER']);
  const canDistribute = hasAnyRole(['ADMIN', 'MANAGER']);
  const isAdmin = hasAnyRole(['ADMIN']);

  useEffect(() => {
    setSelectedLead(lead);
    setSelectedTemplateId('');
    setNoteText('');
    setTagText('');
  }, [lead]);

  const storesQuery = useQuery({
    queryKey: ['stores'],
    queryFn: () => listStores(),
  });

  const companiesQuery = useQuery({
    queryKey: ['companies'],
    queryFn: listCompanies,
    enabled: isAdmin,
  });

  const usersQuery = useQuery({
    queryKey: ['users'],
    queryFn: listUsers,
    enabled: canListUsers,
  });

  const historyQuery = useQuery({
    queryKey: ['lead-history', selectedLead?.id],
    queryFn: () => listLeadHistory(selectedLead!.id),
    enabled: Boolean(open && selectedLead?.id),
  });

  const notesQuery = useQuery({
    queryKey: ['lead-notes', selectedLead?.id],
    queryFn: () => listLeadNotes(selectedLead!.id),
    enabled: Boolean(open && selectedLead?.id),
  });

  const tagsQuery = useQuery({
    queryKey: ['lead-tags', selectedLead?.id],
    queryFn: () => listLeadTags(selectedLead!.id),
    enabled: Boolean(open && selectedLead?.id),
  });

  const followUpsQuery = useQuery({
    queryKey: ['lead-follow-ups', selectedLead?.id],
    queryFn: () => listLeadFollowUps(selectedLead!.id),
    enabled: Boolean(open && selectedLead?.id),
  });

  const activeTemplatesQuery = useQuery({
    queryKey: ['active-templates'],
    queryFn: listActiveTemplates,
    enabled: open,
  });

  const communicationsQuery = useQuery({
    queryKey: ['lead-communications', selectedLead?.id],
    queryFn: () => listLeadCommunications(selectedLead!.id),
    enabled: Boolean(open && selectedLead?.id),
  });

  const {
    formState: { errors: followUpErrors },
    handleSubmit: handleFollowUpSubmit,
    register: registerFollowUp,
    reset: resetFollowUp,
  } = useForm<FollowUpFormValues>({
    resolver: zodResolver(followUpSchema),
    defaultValues: { title: '', description: '', dueAt: '' },
  });

  useEffect(() => {
    if (open && !selectedTemplateId && activeTemplatesQuery.data?.length) {
      setSelectedTemplateId(activeTemplatesQuery.data[0].id);
    }
  }, [activeTemplatesQuery.data, open, selectedTemplateId]);

  const assignToMeMutation = useMutation({
    mutationFn: (leadId: string) => assignLeadToMe(leadId),
    onSuccess: async (updatedLead) => {
      setSelectedLead(updatedLead);
      onLeadChanged?.(updatedLead);
      await invalidateLeadData(updatedLead.id);
    },
  });

  const assignAutomaticallyMutation = useMutation({
    mutationFn: (leadId: string) => assignLeadAutomatically(leadId),
    onSuccess: async (updatedLead) => {
      setSelectedLead(updatedLead);
      onLeadChanged?.(updatedLead);
      await invalidateLeadData(updatedLead.id);
    },
  });

  const changeStatusMutation = useMutation({
    mutationFn: ({ leadId, status }: { leadId: string; status: LeadStatus }) => changeLeadStatus(leadId, status, 'Alteracao feita no CRM'),
    onSuccess: async (updatedLead) => {
      setSelectedLead(updatedLead);
      onLeadChanged?.(updatedLead);
      await invalidateLeadData(updatedLead.id);
    },
  });

  const addNoteMutation = useMutation({
    mutationFn: ({ leadId, note }: { leadId: string; note: string }) => addLeadNote(leadId, note),
    onSuccess: async (_, variables) => {
      setNoteText('');
      await queryClient.invalidateQueries({ queryKey: ['lead-notes', variables.leadId] });
    },
  });

  const addTagMutation = useMutation({
    mutationFn: ({ leadId, name }: { leadId: string; name: string }) => addLeadTag(leadId, name),
    onSuccess: async (_, variables) => {
      setTagText('');
      await queryClient.invalidateQueries({ queryKey: ['lead-tags', variables.leadId] });
    },
  });

  const deleteTagMutation = useMutation({
    mutationFn: ({ leadId, tagId }: { leadId: string; tagId: string }) => deleteLeadTag(leadId, tagId),
    onSuccess: async (_, variables) => {
      await queryClient.invalidateQueries({ queryKey: ['lead-tags', variables.leadId] });
    },
  });

  const createFollowUpMutation = useMutation({
    mutationFn: ({ leadId, values }: { leadId: string; values: FollowUpFormValues }) =>
      createFollowUpTask(leadId, {
        title: values.title,
        description: values.description || undefined,
        dueAt: new Date(values.dueAt).toISOString(),
      }),
    onSuccess: async (_, variables) => {
      resetFollowUp({ title: '', description: '', dueAt: '' });
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['lead-follow-ups', variables.leadId] }),
        queryClient.invalidateQueries({ queryKey: ['follow-ups'] }),
        queryClient.invalidateQueries({ queryKey: ['lead-history', variables.leadId] }),
      ]);
    },
  });

  const completeFollowUpMutation = useMutation({
    mutationFn: ({ taskId }: { taskId: string; leadId: string }) => completeFollowUpTask(taskId),
    onSuccess: async (_, variables) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['lead-follow-ups', variables.leadId] }),
        queryClient.invalidateQueries({ queryKey: ['follow-ups'] }),
        queryClient.invalidateQueries({ queryKey: ['lead-history', variables.leadId] }),
      ]);
    },
  });

  const whatsappLinkMutation = useMutation({
    mutationFn: ({ leadId, templateId }: { leadId: string; templateId: string }) => generateWhatsappLink(leadId, templateId),
    onSuccess: async (result) => {
      window.open(result.url, '_blank', 'noopener,noreferrer');
      await queryClient.invalidateQueries({ queryKey: ['lead-communications', result.leadId] });
    },
  });

  async function invalidateLeadData(leadId: string) {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['leads'] }),
      queryClient.invalidateQueries({ queryKey: ['pipeline'] }),
      queryClient.invalidateQueries({ queryKey: ['pipeline-leads-fallback'] }),
      queryClient.invalidateQueries({ queryKey: ['lead-history', leadId] }),
      queryClient.invalidateQueries({ queryKey: ['lead-dashboard'] }),
    ]);
  }

  function companyName(companyId: string) {
    return companiesQuery.data?.find((company) => company.id === companyId)?.name ?? companyId;
  }

  function storeName(storeId: string) {
    return storesQuery.data?.find((store) => store.id === storeId)?.name ?? storeId;
  }

  function userName(userId: string | null) {
    if (!userId) {
      return 'Sem vendedor';
    }
    if (userId === user?.id) {
      return 'Eu';
    }
    return usersQuery.data?.find((item) => item.id === userId)?.name ?? userId;
  }

  function whatsappSellerName(currentLead: Lead) {
    if (!currentLead.assignedToUserId || currentLead.assignedToUserId === user?.id) {
      return user?.name ?? '';
    }
    return usersQuery.data?.find((item) => item.id === currentLead.assignedToUserId)?.name ?? userName(currentLead.assignedToUserId);
  }

  function renderTemplate(template: MessageTemplate | undefined, currentLead: Lead) {
    if (!template) {
      return '';
    }
    return [
      ['{cliente}', currentLead.customerName ?? ''],
      ['{telefone}', currentLead.customerPhone ?? ''],
      ['{veiculo}', vehicleLabel(currentLead)],
      ['{vendedor}', whatsappSellerName(currentLead)],
      ['{loja}', storeName(currentLead.storeId)],
      ['{cidade}', currentLead.customerCity ?? ''],
    ].reduce((message, [placeholder, value]) => message.split(placeholder).join(value), template.content);
  }

  function onFollowUpSubmit(values: FollowUpFormValues) {
    if (!selectedLead) {
      return;
    }
    createFollowUpMutation.mutate({ leadId: selectedLead.id, values });
  }

  const selectedTemplate = activeTemplatesQuery.data?.find((template) => template.id === selectedTemplateId);
  const whatsappPreview = selectedLead ? renderTemplate(selectedTemplate, selectedLead) : '';

  return (
    <Drawer anchor="right" onClose={onClose} open={open} PaperProps={{ sx: { width: { xs: '100%', md: 560 } } }}>
      <Box sx={{ p: 3, display: 'grid', gap: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography component="h3" variant="h5" fontWeight={800}>
            {selectedLead?.customerName}
          </Typography>
          <IconButton aria-label="Fechar" onClick={onClose}>
            <CloseIcon />
          </IconButton>
        </Box>

        {selectedLead && (
          <Stack spacing={2.5}>
            <Stack direction="row" flexWrap="wrap" gap={1}>
              <Chip color={metadata.color('leadStatuses', selectedLead.status)} label={metadata.label('leadStatuses', selectedLead.status)} />
              <Chip label={metadata.label('leadSources', selectedLead.source)} variant="outlined" />
              {selectedLead.overdueToAssign && <Chip color="error" icon={<WarningAmberIcon />} label="Atrasado para atribuir" variant="outlined" />}
              {selectedLead.overdueToFirstContact && <Chip color="error" icon={<WarningAmberIcon />} label="Atrasado para contato" variant="outlined" />}
              <Chip label={storeName(selectedLead.storeId)} variant="outlined" />
              {isAdmin && <Chip label={companyName(selectedLead.companyId)} variant="outlined" />}
            </Stack>

            <Grid2 container spacing={1.5}>
              <Grid2 size={6}>
                <Typography variant="caption" color="text.secondary">
                  Telefone
                </Typography>
                <Typography>{selectedLead.customerPhone ?? '-'}</Typography>
              </Grid2>
              <Grid2 size={6}>
                <Typography variant="caption" color="text.secondary">
                  Vendedor
                </Typography>
                <Typography>{userName(selectedLead.assignedToUserId)}</Typography>
              </Grid2>
              <Grid2 size={12}>
                <Typography variant="caption" color="text.secondary">
                  Veiculo
                </Typography>
                <Typography>{vehicleLabel(selectedLead)}</Typography>
              </Grid2>
              <Grid2 size={6}>
                <Typography variant="caption" color="text.secondary">
                  Moeda
                </Typography>
                <Typography>{selectedLead.saleCurrency}</Typography>
              </Grid2>
            </Grid2>

            <Stack direction="row" spacing={1}>
              <Button onClick={() => assignToMeMutation.mutate(selectedLead.id)} startIcon={<AssignmentIndIcon />} variant="outlined">
                Assumir lead
              </Button>
              {canDistribute && (
                <Button onClick={() => assignAutomaticallyMutation.mutate(selectedLead.id)} startIcon={<AutoModeIcon />} variant="outlined">
                  Atribuir auto
                </Button>
              )}
              <TextField
                label="Status"
                onChange={(event) => changeStatusMutation.mutate({ leadId: selectedLead.id, status: event.target.value as LeadStatus })}
                select
                size="small"
                value={selectedLead.status}
              >
                {statuses.map((status) => (
                  <MenuItem key={status} value={status}>
                    {metadata.label('leadStatuses', status)}
                  </MenuItem>
                ))}
              </TextField>
            </Stack>

            <Divider />

            <Box>
              <Typography variant="subtitle1" fontWeight={800} sx={{ mb: 1 }}>
                Enviar WhatsApp
              </Typography>
              <Stack spacing={1.5}>
                <TextField
                  disabled={!activeTemplatesQuery.data?.length}
                  label="Template"
                  onChange={(event) => setSelectedTemplateId(event.target.value)}
                  select
                  size="small"
                  value={selectedTemplateId}
                >
                  {activeTemplatesQuery.data?.map((template) => (
                    <MenuItem key={template.id} value={template.id}>
                      {template.name}
                    </MenuItem>
                  ))}
                </TextField>
                <TextField label="Pre-visualizacao" minRows={4} multiline slotProps={{ input: { readOnly: true } }} value={whatsappPreview} />
                {whatsappLinkMutation.isError && (
                  <Alert severity="error">{apiErrorMessage(whatsappLinkMutation.error) ?? 'Nao foi possivel gerar o link do WhatsApp.'}</Alert>
                )}
                <Button
                  disabled={!selectedTemplateId || !selectedLead.customerPhone || whatsappLinkMutation.isPending}
                  onClick={() => whatsappLinkMutation.mutate({ leadId: selectedLead.id, templateId: selectedTemplateId })}
                  startIcon={<WhatsAppIcon />}
                  variant="contained"
                >
                  Abrir WhatsApp
                </Button>
              </Stack>
            </Box>

            <Divider />

            <Box>
              <Typography variant="subtitle1" fontWeight={800} sx={{ mb: 1 }}>
                Follow-ups
              </Typography>
              <Box component="form" onSubmit={handleFollowUpSubmit(onFollowUpSubmit)} sx={{ display: 'grid', gap: 1.25, mb: 1.5 }}>
                <TextField
                  label="Titulo"
                  size="small"
                  error={Boolean(followUpErrors.title)}
                  helperText={followUpErrors.title?.message}
                  {...registerFollowUp('title')}
                />
                <TextField
                  label="Descricao"
                  minRows={2}
                  multiline
                  size="small"
                  error={Boolean(followUpErrors.description)}
                  helperText={followUpErrors.description?.message}
                  {...registerFollowUp('description')}
                />
                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                  <TextField
                    fullWidth
                    label="Vencimento"
                    size="small"
                    slotProps={{ inputLabel: { shrink: true } }}
                    type="datetime-local"
                    error={Boolean(followUpErrors.dueAt)}
                    helperText={followUpErrors.dueAt?.message}
                    {...registerFollowUp('dueAt')}
                  />
                  <Button disabled={createFollowUpMutation.isPending} startIcon={<EventIcon />} type="submit" variant="outlined">
                    Criar
                  </Button>
                </Stack>
              </Box>
              <Stack spacing={1}>
                {followUpsQuery.data?.map((task) => (
                  <Paper key={task.id} variant="outlined" sx={{ borderRadius: 1, p: 1.5 }}>
                    <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" spacing={1}>
                      <Box>
                        <Stack direction="row" flexWrap="wrap" gap={0.75} sx={{ mb: 0.5 }}>
                          <Typography variant="body2" fontWeight={800}>
                            {task.title}
                          </Typography>
                          <Chip color={metadata.color('followUpStatuses', task.status)} label={metadata.label('followUpStatuses', task.status)} size="small" />
                        </Stack>
                        <Typography variant="body2" color="text.secondary">
                          {task.description ?? 'Sem descricao'}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {new Date(task.dueAt).toLocaleString('pt-BR')}
                        </Typography>
                      </Box>
                      <Button
                        disabled={task.status === 'DONE' || task.status === 'CANCELED' || completeFollowUpMutation.isPending}
                        onClick={() => completeFollowUpMutation.mutate({ taskId: task.id, leadId: selectedLead.id })}
                        startIcon={<CheckCircleIcon />}
                        variant="outlined"
                      >
                        Concluir
                      </Button>
                    </Stack>
                  </Paper>
                ))}
                {!followUpsQuery.isLoading && followUpsQuery.data?.length === 0 && (
                  <Typography color="text.secondary" variant="body2">
                    Nenhum follow-up cadastrado.
                  </Typography>
                )}
              </Stack>
            </Box>

            <Divider />

            <Box>
              <Typography variant="subtitle1" fontWeight={800} sx={{ mb: 1 }}>
                Tags
              </Typography>
              <Stack direction="row" flexWrap="wrap" gap={1} sx={{ mb: 1.5 }}>
                {tagsQuery.data?.map((tag) => (
                  <Chip
                    key={tag.id}
                    icon={<LocalOfferIcon />}
                    label={tag.name}
                    onDelete={() => deleteTagMutation.mutate({ leadId: selectedLead.id, tagId: tag.id })}
                    size="small"
                  />
                ))}
              </Stack>
              <Stack direction="row" spacing={1}>
                <TextField fullWidth label="Nova tag" onChange={(event) => setTagText(event.target.value)} size="small" value={tagText} />
                <Button disabled={!tagText.trim()} onClick={() => addTagMutation.mutate({ leadId: selectedLead.id, name: tagText })} variant="outlined">
                  Adicionar
                </Button>
              </Stack>
            </Box>

            <Box>
              <Typography variant="subtitle1" fontWeight={800} sx={{ mb: 1 }}>
                Observacoes
              </Typography>
              <Stack direction="row" spacing={1} sx={{ mb: 1.5 }}>
                <TextField fullWidth label="Nova observacao" onChange={(event) => setNoteText(event.target.value)} size="small" value={noteText} />
                <Button disabled={!noteText.trim()} onClick={() => addNoteMutation.mutate({ leadId: selectedLead.id, note: noteText })} startIcon={<NotesIcon />} variant="outlined">
                  Salvar
                </Button>
              </Stack>
              <Stack spacing={1}>
                {notesQuery.data?.map((note) => (
                  <Paper key={note.id} variant="outlined" sx={{ borderRadius: 1, p: 1.5 }}>
                    <Typography variant="body2">{note.note}</Typography>
                    <Typography variant="caption" color="text.secondary">
                      {userName(note.userId)} - {new Date(note.createdAt).toLocaleString('pt-BR')}
                    </Typography>
                  </Paper>
                ))}
              </Stack>
            </Box>

            <Box>
              <Typography variant="subtitle1" fontWeight={800} sx={{ mb: 1 }}>
                Comunicacoes
              </Typography>
              <Stack spacing={1}>
                {communicationsQuery.data?.map((communication) => (
                  <Paper key={communication.id} variant="outlined" sx={{ borderRadius: 1, p: 1.5 }}>
                    <Typography variant="body2" fontWeight={700}>
                      {communication.channel}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {communication.message}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {userName(communication.userId)} - {new Date(communication.createdAt).toLocaleString('pt-BR')}
                    </Typography>
                  </Paper>
                ))}
                {!communicationsQuery.isLoading && communicationsQuery.data?.length === 0 && (
                  <Typography color="text.secondary" variant="body2">
                    Nenhuma comunicacao registrada.
                  </Typography>
                )}
              </Stack>
            </Box>

            <Box>
              <Typography variant="subtitle1" fontWeight={800} sx={{ mb: 1 }}>
                Historico
              </Typography>
              <Stack spacing={1}>
                {historyQuery.data?.map((history) => (
                  <Paper key={history.id} variant="outlined" sx={{ borderRadius: 1, p: 1.5 }}>
                    <Typography variant="body2" fontWeight={700}>
                      {history.previousStatus ? metadata.label('leadStatuses', history.previousStatus) : 'Criado'} {'>'}{' '}
                      {metadata.label('leadStatuses', history.newStatus)}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {history.description ?? 'Sem descricao'}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {history.userId ? userName(history.userId) : 'Sistema'} - {new Date(history.createdAt).toLocaleString('pt-BR')}
                    </Typography>
                  </Paper>
                ))}
              </Stack>
            </Box>
          </Stack>
        )}
      </Box>
    </Drawer>
  );
}
