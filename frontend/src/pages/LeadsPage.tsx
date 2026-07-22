import { zodResolver } from '@hookform/resolvers/zod';
import AddIcon from '@mui/icons-material/Add';
import AssignmentIndIcon from '@mui/icons-material/AssignmentInd';
import AutoModeIcon from '@mui/icons-material/AutoMode';
import CloseIcon from '@mui/icons-material/Close';
import SearchIcon from '@mui/icons-material/Search';
import SyncIcon from '@mui/icons-material/Sync';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import {
  Alert,
  Box,
  Button,
  Chip,
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
import { LeadDetailDrawer } from '../features/leads/LeadDetailDrawer';
import { vehicleLabel } from '../features/leads/leadDisplay';
import { useAuth } from '../hooks/useAuth';
import { useMetadata } from '../hooks/useMetadata';
import { apiErrorMessage } from '../services/api';
import { listCompanies } from '../services/companyService';
import {
  assignLeadAutomatically,
  assignLeadToMe,
  createLead,
  distributePendingLeads,
  listLeads,
} from '../services/leadService';
import type { LeadFilters } from '../services/leadService';
import { listStores } from '../services/storeService';
import { listUsers } from '../services/userService';
import type { Lead, LeadSource, LeadStatus } from '../types/lead';

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

const sources: LeadSource[] = ['MANUAL', 'EMAIL', 'WEBSITE', 'FACEBOOK', 'INSTAGRAM', 'WEBMOTORS', 'ICARROS', 'OLX', 'API'];

const leadSchema = z.object({
  companyId: z.string().min(1, 'Selecione a empresa'),
  storeId: z.string().min(1, 'Selecione a loja'),
  customerName: z.string().min(1, 'Informe o cliente').max(160),
  customerPhone: z.string().max(40).refine((value) => {
    if (!value) {
      return true;
    }
    const trimmed = value.trim();
    const digits = trimmed.replace(/\D/g, '');
    return /^\+[1-9]\d{7,14}$/.test(trimmed) || digits.length === 10 || digits.length === 11 || (digits.startsWith('55') && digits.length >= 8 && digits.length <= 15);
  }, 'Informe telefone E.164 ou telefone brasileiro com DDD'),
  customerEmail: z.string().email('Informe um e-mail valido').max(180).or(z.literal('')),
  customerCity: z.string().max(120),
  vehicleInterest: z.string().max(180),
  itemName: z.string().max(180),
  vehicleName: z.string().max(180),
  vehicleYear: z.string(),
  vehicleModel: z.string().max(120),
  vehicleValue: z.string(),
  source: z.enum(['MANUAL', 'EMAIL', 'WEBSITE', 'FACEBOOK', 'INSTAGRAM', 'WEBMOTORS', 'ICARROS', 'OLX', 'API']),
  originalMessage: z.string(),
  saleValue: z.string(),
  saleCurrency: z.string().trim().length(3, 'Informe moeda com 3 letras').regex(/^[A-Za-z]{3}$/, 'Informe moeda ISO com 3 letras'),
});

type LeadFormValues = z.infer<typeof leadSchema>;

const defaultFilters: LeadFilters = { page: 0, size: 10 };

export function LeadsPage() {
  const { hasAnyRole, user } = useAuth();
  const metadata = useMetadata();
  const queryClient = useQueryClient();
  const [filters, setFilters] = useState<LeadFilters>(defaultFilters);
  const [draftFilters, setDraftFilters] = useState<LeadFilters>(defaultFilters);
  const [selectedLead, setSelectedLead] = useState<Lead | null>(null);
  const [drawerMode, setDrawerMode] = useState<'create' | 'detail' | null>(null);
  const canListUsers = hasAnyRole(['ADMIN', 'MANAGER', 'STORE_MANAGER']);
  const canDistribute = hasAnyRole(['ADMIN', 'MANAGER', 'STORE_MANAGER']);
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
      itemName: '',
      vehicleName: '',
      vehicleYear: '',
      vehicleModel: '',
      vehicleValue: '',
      source: 'MANUAL',
      originalMessage: '',
      saleValue: '',
      saleCurrency: 'BRL',
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

  const createLeadMutation = useMutation({
    mutationFn: (values: LeadFormValues) => {
      const vehicle =
        values.vehicleName || values.vehicleYear || values.vehicleModel || values.vehicleValue
          ? {
              name: values.vehicleName || undefined,
              year: values.vehicleYear ? Number(values.vehicleYear) : undefined,
              model: values.vehicleModel || undefined,
              value: values.vehicleValue ? Number(values.vehicleValue) : undefined,
            }
          : undefined;
      return createLead({
        companyId: values.companyId,
        storeId: values.storeId,
        customerName: values.customerName,
        customerPhone: values.customerPhone || undefined,
        customerEmail: values.customerEmail || undefined,
        customerCity: values.customerCity || undefined,
        vehicleInterest: values.vehicleInterest || undefined,
        source: values.source,
        originalMessage: values.originalMessage || undefined,
        saleValue: values.saleValue ? Number(values.saleValue) : undefined,
        saleCurrency: values.saleCurrency || 'BRL',
        item: values.itemName || vehicle ? { name: values.itemName || undefined, vehicle } : undefined,
      });
    },
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

  const assignAutomaticallyMutation = useMutation({
    mutationFn: (leadId: string) => assignLeadAutomatically(leadId),
    onSuccess: async (lead) => {
      setSelectedLead(lead);
      await invalidateLeadData(lead.id);
    },
  });

  const distributePendingMutation = useMutation({
    mutationFn: distributePendingLeads,
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['leads'] }),
        queryClient.invalidateQueries({ queryKey: ['lead-dashboard'] }),
      ]);
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

  function openCreateDrawer() {
    reset(emptyLeadValues);
    setSelectedLead(null);
    setDrawerMode('create');
  }

  function openDetailDrawer(lead: Lead) {
    setSelectedLead(lead);
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

  const slaCounts = useMemo(() => {
    const leads = leadsQuery.data?.content ?? [];
    return {
      overdueToAssign: leads.filter((lead) => lead.overdueToAssign).length,
      overdueToFirstContact: leads.filter((lead) => lead.overdueToFirstContact).length,
    };
  }, [leadsQuery.data?.content]);

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 2 }}>
        <Box>
          <Typography component="h2" variant="h4" fontWeight={800}>
            Leads
          </Typography>
          <Typography color="text.secondary">Gestao comercial de oportunidades por loja, origem e vendedor.</Typography>
        </Box>
        <Stack direction="row" spacing={1}>
          {canDistribute && (
            <Button
              disabled={distributePendingMutation.isPending}
              onClick={() => distributePendingMutation.mutate()}
              startIcon={<SyncIcon />}
              variant="outlined"
            >
              Distribuir pendentes
            </Button>
          )}
          <Button onClick={openCreateDrawer} startIcon={<AddIcon />} variant="contained">
            Novo lead
          </Button>
        </Stack>
      </Box>

      <Grid2 container spacing={1.5}>
        <Grid2 size={{ xs: 6, md: 2.4 }}>
          <Paper variant="outlined" sx={{ borderRadius: 1, p: 1.5, borderColor: slaCounts.overdueToAssign ? 'error.main' : 'divider' }}>
            <Typography variant="caption" color="text.secondary">
              SLA atribuicao
            </Typography>
            <Typography variant="h5" fontWeight={800} color={slaCounts.overdueToAssign ? 'error.main' : 'text.primary'}>
              {slaCounts.overdueToAssign}
            </Typography>
          </Paper>
        </Grid2>
        <Grid2 size={{ xs: 6, md: 2.4 }}>
          <Paper variant="outlined" sx={{ borderRadius: 1, p: 1.5, borderColor: slaCounts.overdueToFirstContact ? 'error.main' : 'divider' }}>
            <Typography variant="caption" color="text.secondary">
              SLA contato
            </Typography>
            <Typography variant="h5" fontWeight={800} color={slaCounts.overdueToFirstContact ? 'error.main' : 'text.primary'}>
              {slaCounts.overdueToFirstContact}
            </Typography>
          </Paper>
        </Grid2>
        {statuses.map((status) => (
          <Grid2 key={status} size={{ xs: 6, md: 2.4 }}>
            <Paper variant="outlined" sx={{ borderRadius: 1, p: 1.5 }}>
              <Typography variant="caption" color="text.secondary">
                {metadata.label('leadStatuses', status)}
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
                  {metadata.label('leadStatuses', status)}
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
                  {metadata.label('leadSources', source)}
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
                <TableCell>{vehicleLabel(lead)}</TableCell>
                <TableCell>
                  <Stack direction="row" flexWrap="wrap" gap={0.75}>
                    <Chip color={metadata.color('leadStatuses', lead.status)} label={metadata.label('leadStatuses', lead.status)} size="small" />
                    {lead.overdueToAssign && <Chip color="error" icon={<WarningAmberIcon />} label="Atribuicao" size="small" variant="outlined" />}
                    {lead.overdueToFirstContact && <Chip color="error" icon={<WarningAmberIcon />} label="Contato" size="small" variant="outlined" />}
                  </Stack>
                </TableCell>
                <TableCell>
                  <Chip label={metadata.label('leadSources', lead.source)} size="small" variant="outlined" />
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
                    {canDistribute && !lead.assignedToUserId && (
                      <Button onClick={() => assignAutomaticallyMutation.mutate(lead.id)} size="small" startIcon={<AutoModeIcon />} variant="outlined">
                        Auto
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

      <Drawer anchor="right" onClose={() => setDrawerMode(null)} open={drawerMode === 'create'} PaperProps={{ sx: { width: { xs: '100%', md: 560 } } }}>
        <Box sx={{ p: 3, display: 'grid', gap: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography component="h3" variant="h5" fontWeight={800}>
              Novo lead
            </Typography>
            <IconButton aria-label="Fechar" onClick={() => setDrawerMode(null)}>
              <CloseIcon />
            </IconButton>
          </Box>

          {drawerMode === 'create' && (
            <Box component="form" onSubmit={handleSubmit(onSubmit)} sx={{ display: 'grid', gap: 2 }}>
              {createLeadMutation.isError && (
                <Alert severity="error">{apiErrorMessage(createLeadMutation.error) ?? 'Nao foi possivel criar o lead.'}</Alert>
              )}
              <TextField label="Cliente" error={Boolean(errors.customerName)} helperText={errors.customerName?.message} {...register('customerName')} />
              <TextField label="Telefone" error={Boolean(errors.customerPhone)} helperText={errors.customerPhone?.message} {...register('customerPhone')} />
              <TextField label="E-mail" error={Boolean(errors.customerEmail)} helperText={errors.customerEmail?.message} {...register('customerEmail')} />
              <TextField label="Cidade" error={Boolean(errors.customerCity)} helperText={errors.customerCity?.message} {...register('customerCity')} />
              <TextField label="Veiculo de interesse" error={Boolean(errors.vehicleInterest)} helperText={errors.vehicleInterest?.message} {...register('vehicleInterest')} />
              <TextField label="Nome do item" error={Boolean(errors.itemName)} helperText={errors.itemName?.message} {...register('itemName')} />
              <Grid2 container spacing={1.5}>
                <Grid2 size={{ xs: 12, md: 6 }}>
                  <TextField fullWidth label="Nome do veiculo" error={Boolean(errors.vehicleName)} helperText={errors.vehicleName?.message} {...register('vehicleName')} />
                </Grid2>
                <Grid2 size={{ xs: 12, md: 6 }}>
                  <TextField fullWidth label="Ano" type="number" error={Boolean(errors.vehicleYear)} helperText={errors.vehicleYear?.message} {...register('vehicleYear')} />
                </Grid2>
                <Grid2 size={{ xs: 12, md: 6 }}>
                  <TextField fullWidth label="Modelo" error={Boolean(errors.vehicleModel)} helperText={errors.vehicleModel?.message} {...register('vehicleModel')} />
                </Grid2>
                <Grid2 size={{ xs: 12, md: 6 }}>
                  <TextField fullWidth label="Valor do veiculo" type="number" error={Boolean(errors.vehicleValue)} helperText={errors.vehicleValue?.message} {...register('vehicleValue')} />
                </Grid2>
              </Grid2>
              <TextField select label="Origem" defaultValue={emptyLeadValues.source} error={Boolean(errors.source)} helperText={errors.source?.message} {...register('source')}>
                {sources.map((source) => (
                  <MenuItem key={source} value={source}>
                    {metadata.label('leadSources', source)}
                  </MenuItem>
                ))}
              </TextField>
              {isAdmin && (
                <TextField select label="Empresa" defaultValue={emptyLeadValues.companyId} error={Boolean(errors.companyId)} helperText={errors.companyId?.message} {...register('companyId')}>
                  <MenuItem value="" disabled>
                    Selecione uma empresa
                  </MenuItem>
                  {companiesQuery.data?.map((company) => (
                    <MenuItem key={company.id} value={company.id}>
                      {company.name}
                    </MenuItem>
                  ))}
                </TextField>
              )}
              {!isAdmin && <TextField label="Empresa" value={user?.companyId ?? ''} slotProps={{ input: { readOnly: true } }} {...register('companyId')} />}
              <TextField select label="Loja" defaultValue={emptyLeadValues.storeId} error={Boolean(errors.storeId)} helperText={errors.storeId?.message} {...register('storeId')}>
                <MenuItem value="" disabled>
                  Selecione uma loja
                </MenuItem>
                {storesQuery.data?.map((store) => (
                  <MenuItem key={store.id} value={store.id}>
                    {store.name}
                  </MenuItem>
                ))}
              </TextField>
              <Grid2 container spacing={1.5}>
                <Grid2 size={{ xs: 12, md: 8 }}>
                  <TextField fullWidth label="Valor da venda" type="number" error={Boolean(errors.saleValue)} helperText={errors.saleValue?.message} {...register('saleValue')} />
                </Grid2>
                <Grid2 size={{ xs: 12, md: 4 }}>
                  <TextField fullWidth label="Moeda" error={Boolean(errors.saleCurrency)} helperText={errors.saleCurrency?.message} {...register('saleCurrency')} />
                </Grid2>
              </Grid2>
              <TextField label="Mensagem original" minRows={3} multiline {...register('originalMessage')} />
              <Button disabled={createLeadMutation.isPending} type="submit" variant="contained">
                Criar lead
              </Button>
            </Box>
          )}

        </Box>
      </Drawer>
      <LeadDetailDrawer
        lead={selectedLead}
        onClose={() => setDrawerMode(null)}
        onLeadChanged={setSelectedLead}
        open={drawerMode === 'detail'}
      />
    </Box>
  );
}
