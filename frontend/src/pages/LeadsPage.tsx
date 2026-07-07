import { zodResolver } from '@hookform/resolvers/zod';
import AddIcon from '@mui/icons-material/Add';
import AssignmentIndIcon from '@mui/icons-material/AssignmentInd';
import CloseIcon from '@mui/icons-material/Close';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import NotesIcon from '@mui/icons-material/Notes';
import SearchIcon from '@mui/icons-material/Search';
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
  Table,
  TableBody,
  TableCell,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { useAuth } from '../hooks/useAuth';
import { listCompanies } from '../services/companyService';
import {
  addLeadNote,
  addLeadTag,
  assignLeadToMe,
  changeLeadStatus,
  createLead,
  deleteLeadTag,
  listLeadHistory,
  listLeadNotes,
  listLeads,
  listLeadTags,
} from '../services/leadService';
import type { LeadFilters } from '../services/leadService';
import { listStores } from '../services/storeService';
import { generateWhatsappLink, listActiveTemplates, listLeadCommunications } from '../services/templateService';
import { listUsers } from '../services/userService';
import type { Lead, LeadSource, LeadStatus } from '../types/lead';
import type { MessageTemplate } from '../types/message';

const statuses: LeadStatus[] = [
  'NEW',
  'AVAILABLE',
  'ASSIGNED',
  'FIRST_CONTACT',
  'IN_NEGOTIATION',
  'VISIT_SCHEDULED',
  'PROPOSAL_SENT',
  'SOLD',
  'LOST',
  'DUPLICATED',
];

const sources: LeadSource[] = ['MANUAL', 'EMAIL', 'WEBSITE', 'FACEBOOK', 'INSTAGRAM', 'WEBMOTORS', 'ICARROS', 'OLX', 'API'];

const statusColors: Record<LeadStatus, 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'error' | 'info'> = {
  NEW: 'info',
  AVAILABLE: 'primary',
  ASSIGNED: 'secondary',
  FIRST_CONTACT: 'warning',
  IN_NEGOTIATION: 'warning',
  VISIT_SCHEDULED: 'info',
  PROPOSAL_SENT: 'secondary',
  SOLD: 'success',
  LOST: 'error',
  DUPLICATED: 'default',
};

const leadSchema = z.object({
  companyId: z.string().min(1, 'Selecione a empresa'),
  storeId: z.string().min(1, 'Selecione a loja'),
  customerName: z.string().min(1, 'Informe o cliente').max(160),
  customerPhone: z.string().max(40),
  customerEmail: z.string().email('Informe um e-mail valido').max(180).or(z.literal('')),
  customerCity: z.string().max(120),
  vehicleInterest: z.string().max(180),
  source: z.enum(['MANUAL', 'EMAIL', 'WEBSITE', 'FACEBOOK', 'INSTAGRAM', 'WEBMOTORS', 'ICARROS', 'OLX', 'API']),
  originalMessage: z.string(),
  saleValue: z.string(),
});

type LeadFormValues = z.infer<typeof leadSchema>;

const defaultFilters: LeadFilters = { page: 0, size: 10 };

export function LeadsPage() {
  const { hasAnyRole, user } = useAuth();
  const queryClient = useQueryClient();
  const [filters, setFilters] = useState<LeadFilters>(defaultFilters);
  const [draftFilters, setDraftFilters] = useState<LeadFilters>(defaultFilters);
  const [selectedLead, setSelectedLead] = useState<Lead | null>(null);
  const [drawerMode, setDrawerMode] = useState<'create' | 'detail' | null>(null);
  const [noteText, setNoteText] = useState('');
  const [tagText, setTagText] = useState('');
  const [selectedTemplateId, setSelectedTemplateId] = useState('');
  const canListUsers = hasAnyRole(['ADMIN', 'MANAGER']);
  const isAdmin = hasAnyRole(['ADMIN']);

  const emptyLeadValues = useMemo<LeadFormValues>(
    () => ({
      companyId: user?.companyId ?? '',
      storeId: user?.storeId ?? '',
      customerName: '',
      customerPhone: '',
      customerEmail: '',
      customerCity: '',
      vehicleInterest: '',
      source: 'MANUAL',
      originalMessage: '',
      saleValue: '',
    }),
    [user?.companyId, user?.storeId],
  );

  const leadsQuery = useQuery({
    queryKey: ['leads', filters],
    queryFn: () => listLeads(filters),
  });

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
    enabled: Boolean(selectedLead?.id && drawerMode === 'detail'),
  });

  const notesQuery = useQuery({
    queryKey: ['lead-notes', selectedLead?.id],
    queryFn: () => listLeadNotes(selectedLead!.id),
    enabled: Boolean(selectedLead?.id && drawerMode === 'detail'),
  });

  const tagsQuery = useQuery({
    queryKey: ['lead-tags', selectedLead?.id],
    queryFn: () => listLeadTags(selectedLead!.id),
    enabled: Boolean(selectedLead?.id && drawerMode === 'detail'),
  });

  const activeTemplatesQuery = useQuery({
    queryKey: ['active-templates'],
    queryFn: listActiveTemplates,
    enabled: drawerMode === 'detail',
  });

  const communicationsQuery = useQuery({
    queryKey: ['lead-communications', selectedLead?.id],
    queryFn: () => listLeadCommunications(selectedLead!.id),
    enabled: Boolean(selectedLead?.id && drawerMode === 'detail'),
  });

  const {
    formState: { errors },
    handleSubmit,
    register,
    reset,
    setValue,
  } = useForm<LeadFormValues>({
    resolver: zodResolver(leadSchema),
    defaultValues: emptyLeadValues,
  });

  useEffect(() => {
    reset(emptyLeadValues);
  }, [emptyLeadValues, reset]);

  useEffect(() => {
    if (storesQuery.data?.length && !emptyLeadValues.storeId) {
      setValue('storeId', storesQuery.data[0].id);
    }
  }, [emptyLeadValues.storeId, setValue, storesQuery.data]);

  useEffect(() => {
    if (drawerMode === 'detail' && !selectedTemplateId && activeTemplatesQuery.data?.length) {
      setSelectedTemplateId(activeTemplatesQuery.data[0].id);
    }
  }, [activeTemplatesQuery.data, drawerMode, selectedTemplateId]);

  const createLeadMutation = useMutation({
    mutationFn: (values: LeadFormValues) =>
      createLead({
        ...values,
        customerPhone: values.customerPhone || undefined,
        customerEmail: values.customerEmail || undefined,
        customerCity: values.customerCity || undefined,
        vehicleInterest: values.vehicleInterest || undefined,
        originalMessage: values.originalMessage || undefined,
        saleValue: values.saleValue ? Number(values.saleValue) : undefined,
      }),
    onSuccess: async () => {
      setDrawerMode(null);
      reset(emptyLeadValues);
      await queryClient.invalidateQueries({ queryKey: ['leads'] });
    },
  });

  const assignToMeMutation = useMutation({
    mutationFn: (leadId: string) => assignLeadToMe(leadId),
    onSuccess: async (lead) => {
      setSelectedLead(lead);
      await invalidateLeadData(lead.id);
    },
  });

  const changeStatusMutation = useMutation({
    mutationFn: ({ leadId, status }: { leadId: string; status: LeadStatus }) => changeLeadStatus(leadId, status, 'Alteracao feita no CRM'),
    onSuccess: async (lead) => {
      setSelectedLead(lead);
      await invalidateLeadData(lead.id);
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
      queryClient.invalidateQueries({ queryKey: ['lead-history', leadId] }),
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

  function whatsappSellerName(lead: Lead) {
    if (!lead.assignedToUserId || lead.assignedToUserId === user?.id) {
      return user?.name ?? '';
    }
    return usersQuery.data?.find((item) => item.id === lead.assignedToUserId)?.name ?? userName(lead.assignedToUserId);
  }

  function renderTemplate(template: MessageTemplate | undefined, lead: Lead) {
    if (!template) {
      return '';
    }
    return [
      ['{cliente}', lead.customerName ?? ''],
      ['{telefone}', lead.customerPhone ?? ''],
      ['{veiculo}', lead.vehicleInterest ?? ''],
      ['{vendedor}', whatsappSellerName(lead)],
      ['{loja}', storeName(lead.storeId)],
      ['{cidade}', lead.customerCity ?? ''],
    ].reduce((message, [placeholder, value]) => message.split(placeholder).join(value), template.content);
  }

  function openCreateDrawer() {
    reset(emptyLeadValues);
    setSelectedLead(null);
    setDrawerMode('create');
  }

  function openDetailDrawer(lead: Lead) {
    setSelectedLead(lead);
    setSelectedTemplateId('');
    setDrawerMode('detail');
  }

  function applyFilters() {
    setFilters({ ...draftFilters, page: 0, size: filters.size ?? 10 });
  }

  function setDateFilter(field: 'createdFrom' | 'createdTo', value: string) {
    setDraftFilters((current) => ({
      ...current,
      [field]: value ? `${value}T${field === 'createdFrom' ? '00:00:00.000' : '23:59:59.999'}Z` : undefined,
    }));
  }

  function onSubmit(values: LeadFormValues) {
    createLeadMutation.mutate(values);
  }

  const statusCounts = useMemo(() => {
    const counts = new Map<LeadStatus, number>();
    leadsQuery.data?.content.forEach((lead) => counts.set(lead.status, (counts.get(lead.status) ?? 0) + 1));
    return counts;
  }, [leadsQuery.data?.content]);

  const selectedTemplate = activeTemplatesQuery.data?.find((template) => template.id === selectedTemplateId);
  const whatsappPreview = selectedLead ? renderTemplate(selectedTemplate, selectedLead) : '';

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 2 }}>
        <Box>
          <Typography component="h2" variant="h4" fontWeight={800}>
            Leads
          </Typography>
          <Typography color="text.secondary">Gestao comercial de oportunidades por loja, origem e vendedor.</Typography>
        </Box>
        <Button onClick={openCreateDrawer} startIcon={<AddIcon />} variant="contained">
          Novo lead
        </Button>
      </Box>

      <Grid2 container spacing={1.5}>
        {statuses.map((status) => (
          <Grid2 key={status} size={{ xs: 6, md: 2.4 }}>
            <Paper variant="outlined" sx={{ borderRadius: 1, p: 1.5 }}>
              <Typography variant="caption" color="text.secondary">
                {status}
              </Typography>
              <Typography variant="h5" fontWeight={800}>
                {statusCounts.get(status) ?? 0}
              </Typography>
            </Paper>
          </Grid2>
        ))}
      </Grid2>

      <Paper variant="outlined" sx={{ borderRadius: 1, p: 2 }}>
        <Grid2 container spacing={2}>
          <Grid2 size={{ xs: 12, md: 3 }}>
            <TextField
              fullWidth
              label="Texto livre"
              onChange={(event) => setDraftFilters((current) => ({ ...current, text: event.target.value }))}
              size="small"
              value={draftFilters.text ?? ''}
            />
          </Grid2>
          <Grid2 size={{ xs: 12, md: 2 }}>
            <TextField
              fullWidth
              label="Telefone"
              onChange={(event) => setDraftFilters((current) => ({ ...current, phone: event.target.value }))}
              size="small"
              value={draftFilters.phone ?? ''}
            />
          </Grid2>
          <Grid2 size={{ xs: 12, md: 2 }}>
            <TextField
              fullWidth
              label="Veiculo"
              onChange={(event) => setDraftFilters((current) => ({ ...current, vehicle: event.target.value }))}
              size="small"
              value={draftFilters.vehicle ?? ''}
            />
          </Grid2>
          <Grid2 size={{ xs: 12, md: 2 }}>
            <TextField
              fullWidth
              label="Status"
              onChange={(event) => setDraftFilters((current) => ({ ...current, status: event.target.value as LeadStatus | undefined }))}
              select
              size="small"
              value={draftFilters.status ?? ''}
            >
              <MenuItem value="">Todos</MenuItem>
              {statuses.map((status) => (
                <MenuItem key={status} value={status}>
                  {status}
                </MenuItem>
              ))}
            </TextField>
          </Grid2>
          <Grid2 size={{ xs: 12, md: 2 }}>
            <TextField
              fullWidth
              label="Origem"
              onChange={(event) => setDraftFilters((current) => ({ ...current, source: event.target.value as LeadSource | undefined }))}
              select
              size="small"
              value={draftFilters.source ?? ''}
            >
              <MenuItem value="">Todas</MenuItem>
              {sources.map((source) => (
                <MenuItem key={source} value={source}>
                  {source}
                </MenuItem>
              ))}
            </TextField>
          </Grid2>
          <Grid2 size={{ xs: 12, md: 2 }}>
            <TextField
              fullWidth
              label="Loja"
              onChange={(event) => setDraftFilters((current) => ({ ...current, storeId: event.target.value || undefined }))}
              select
              size="small"
              value={draftFilters.storeId ?? ''}
            >
              <MenuItem value="">Todas</MenuItem>
              {storesQuery.data?.map((store) => (
                <MenuItem key={store.id} value={store.id}>
                  {store.name}
                </MenuItem>
              ))}
            </TextField>
          </Grid2>
          <Grid2 size={{ xs: 12, md: 2 }}>
            <TextField
              fullWidth
              label="Vendedor"
              onChange={(event) => setDraftFilters((current) => ({ ...current, assignedToUserId: event.target.value || undefined }))}
              select
              size="small"
              value={draftFilters.assignedToUserId ?? ''}
            >
              <MenuItem value="">Todos</MenuItem>
              {user && (
                <MenuItem value={user.id}>
                  Eu
                </MenuItem>
              )}
              {usersQuery.data
                ?.filter((item) => item.id !== user?.id)
                .map((item) => (
                  <MenuItem key={item.id} value={item.id}>
                    {item.name}
                  </MenuItem>
                ))}
            </TextField>
          </Grid2>
          <Grid2 size={{ xs: 12, md: 2 }}>
            <TextField
              fullWidth
              label="Inicio"
              onChange={(event) => setDateFilter('createdFrom', event.target.value)}
              size="small"
              slotProps={{ inputLabel: { shrink: true } }}
              type="date"
              value={draftFilters.createdFrom?.slice(0, 10) ?? ''}
            />
          </Grid2>
          <Grid2 size={{ xs: 12, md: 2 }}>
            <TextField
              fullWidth
              label="Fim"
              onChange={(event) => setDateFilter('createdTo', event.target.value)}
              size="small"
              slotProps={{ inputLabel: { shrink: true } }}
              type="date"
              value={draftFilters.createdTo?.slice(0, 10) ?? ''}
            />
          </Grid2>
          <Grid2 size={{ xs: 12, md: 1 }}>
            <Button fullWidth onClick={applyFilters} startIcon={<SearchIcon />} variant="outlined">
              Filtrar
            </Button>
          </Grid2>
        </Grid2>
      </Paper>

      <Paper variant="outlined" sx={{ borderRadius: 1, overflow: 'hidden' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Cliente</TableCell>
              <TableCell>Veiculo</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Origem</TableCell>
              <TableCell>Vendedor</TableCell>
              <TableCell>Loja</TableCell>
              <TableCell>Criado em</TableCell>
              <TableCell align="right">Acoes</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {leadsQuery.data?.content.map((lead) => (
              <TableRow hover key={lead.id}>
                <TableCell>
                  <Typography variant="body2" fontWeight={700}>
                    {lead.customerName}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {lead.customerPhone ?? lead.customerEmail ?? '-'}
                  </Typography>
                </TableCell>
                <TableCell>{lead.vehicleInterest ?? '-'}</TableCell>
                <TableCell>
                  <Chip color={statusColors[lead.status]} label={lead.status} size="small" />
                </TableCell>
                <TableCell>
                  <Chip label={lead.source} size="small" variant="outlined" />
                </TableCell>
                <TableCell>{userName(lead.assignedToUserId)}</TableCell>
                <TableCell>{storeName(lead.storeId)}</TableCell>
                <TableCell>{new Date(lead.createdAt).toLocaleDateString('pt-BR')}</TableCell>
                <TableCell align="right">
                  <Stack direction="row" justifyContent="flex-end" spacing={1}>
                    {!lead.assignedToUserId && (
                      <Button onClick={() => assignToMeMutation.mutate(lead.id)} size="small" startIcon={<AssignmentIndIcon />} variant="outlined">
                        Assumir
                      </Button>
                    )}
                    <Button onClick={() => openDetailDrawer(lead)} size="small" variant="contained">
                      Detalhe
                    </Button>
                  </Stack>
                </TableCell>
              </TableRow>
            ))}
            {!leadsQuery.isLoading && leadsQuery.data?.content.length === 0 && (
              <TableRow>
                <TableCell colSpan={8}>Nenhum lead encontrado.</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
        <TablePagination
          component="div"
          count={leadsQuery.data?.totalElements ?? 0}
          onPageChange={(_, page) => setFilters((current) => ({ ...current, page }))}
          onRowsPerPageChange={(event) => setFilters((current) => ({ ...current, page: 0, size: Number(event.target.value) }))}
          page={filters.page ?? 0}
          rowsPerPage={filters.size ?? 10}
          rowsPerPageOptions={[10, 20, 50]}
        />
      </Paper>

      <Drawer anchor="right" onClose={() => setDrawerMode(null)} open={drawerMode !== null} PaperProps={{ sx: { width: { xs: '100%', md: 560 } } }}>
        <Box sx={{ p: 3, display: 'grid', gap: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography component="h3" variant="h5" fontWeight={800}>
              {drawerMode === 'create' ? 'Novo lead' : selectedLead?.customerName}
            </Typography>
            <IconButton aria-label="Fechar" onClick={() => setDrawerMode(null)}>
              <CloseIcon />
            </IconButton>
          </Box>

          {drawerMode === 'create' && (
            <Box component="form" onSubmit={handleSubmit(onSubmit)} sx={{ display: 'grid', gap: 2 }}>
              {createLeadMutation.isError && <Alert severity="error">Nao foi possivel criar o lead.</Alert>}
              <TextField label="Cliente" error={Boolean(errors.customerName)} helperText={errors.customerName?.message} {...register('customerName')} />
              <TextField label="Telefone" error={Boolean(errors.customerPhone)} helperText={errors.customerPhone?.message} {...register('customerPhone')} />
              <TextField label="E-mail" error={Boolean(errors.customerEmail)} helperText={errors.customerEmail?.message} {...register('customerEmail')} />
              <TextField label="Cidade" error={Boolean(errors.customerCity)} helperText={errors.customerCity?.message} {...register('customerCity')} />
              <TextField label="Veiculo de interesse" error={Boolean(errors.vehicleInterest)} helperText={errors.vehicleInterest?.message} {...register('vehicleInterest')} />
              <TextField select label="Origem" error={Boolean(errors.source)} helperText={errors.source?.message} {...register('source')}>
                {sources.map((source) => (
                  <MenuItem key={source} value={source}>
                    {source}
                  </MenuItem>
                ))}
              </TextField>
              {isAdmin && (
                <TextField select label="Empresa" error={Boolean(errors.companyId)} helperText={errors.companyId?.message} {...register('companyId')}>
                  {companiesQuery.data?.map((company) => (
                    <MenuItem key={company.id} value={company.id}>
                      {company.name}
                    </MenuItem>
                  ))}
                </TextField>
              )}
              {!isAdmin && <TextField label="Empresa" value={user?.companyId ?? ''} slotProps={{ input: { readOnly: true } }} {...register('companyId')} />}
              <TextField select label="Loja" error={Boolean(errors.storeId)} helperText={errors.storeId?.message} {...register('storeId')}>
                {storesQuery.data?.map((store) => (
                  <MenuItem key={store.id} value={store.id}>
                    {store.name}
                  </MenuItem>
                ))}
              </TextField>
              <TextField label="Valor da venda" type="number" error={Boolean(errors.saleValue)} helperText={errors.saleValue?.message} {...register('saleValue')} />
              <TextField label="Mensagem original" minRows={3} multiline {...register('originalMessage')} />
              <Button disabled={createLeadMutation.isPending} type="submit" variant="contained">
                Criar lead
              </Button>
            </Box>
          )}

          {drawerMode === 'detail' && selectedLead && (
            <Stack spacing={2.5}>
              <Stack direction="row" flexWrap="wrap" gap={1}>
                <Chip color={statusColors[selectedLead.status]} label={selectedLead.status} />
                <Chip label={selectedLead.source} variant="outlined" />
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
                  <Typography>{selectedLead.vehicleInterest ?? '-'}</Typography>
                </Grid2>
              </Grid2>

              <Stack direction="row" spacing={1}>
                <Button onClick={() => assignToMeMutation.mutate(selectedLead.id)} startIcon={<AssignmentIndIcon />} variant="outlined">
                  Assumir lead
                </Button>
                <TextField
                  label="Status"
                  onChange={(event) => changeStatusMutation.mutate({ leadId: selectedLead.id, status: event.target.value as LeadStatus })}
                  select
                  size="small"
                  value={selectedLead.status}
                >
                  {statuses.map((status) => (
                    <MenuItem key={status} value={status}>
                      {status}
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
                  {whatsappLinkMutation.isError && <Alert severity="error">Nao foi possivel gerar o link do WhatsApp.</Alert>}
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
                        {history.previousStatus ?? 'CRIADO'} {'>'} {history.newStatus}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {history.description ?? 'Sem descricao'}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {userName(history.userId)} - {new Date(history.createdAt).toLocaleString('pt-BR')}
                      </Typography>
                    </Paper>
                  ))}
                </Stack>
              </Box>
            </Stack>
          )}
        </Box>
      </Drawer>
    </Box>
  );
}
