import EmailIcon from '@mui/icons-material/Email';
import GroupsIcon from '@mui/icons-material/Groups';
import SaveIcon from '@mui/icons-material/Save';
import SettingsIcon from '@mui/icons-material/Settings';
import StorefrontIcon from '@mui/icons-material/Storefront';
import TextSnippetIcon from '@mui/icons-material/TextSnippet';
import {
  Alert,
  Box,
  Button,
  Chip,
  Divider,
  FormControlLabel,
  Grid2,
  LinearProgress,
  MenuItem,
  Paper,
  Stack,
  Switch,
  Tab,
  Tabs,
  TextField,
  Typography,
} from '@mui/material';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useMemo, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { z } from 'zod';
import { useAuth } from '../hooks/useAuth';
import { useMetadata } from '../hooks/useMetadata';
import {
  getSettings,
  updateSettingsCompany,
  updateSettingsDistribution,
  updateSettingsSla,
  updateSettingsStore,
} from '../services/settingsService';
import type { LeadDistributionMode } from '../types/distribution';
import type { SettingsCompanyPayload, SettingsDistributionPayload, SettingsSlaPayload, SettingsStorePayload } from '../types/settings';

const optionalText = z.string().trim().optional().or(z.literal(''));
const statusSchema = z.enum(['ACTIVE', 'INACTIVE']);
const distributionModes: LeadDistributionMode[] = ['MANUAL', 'ROUND_ROBIN', 'LEAST_BUSY'];

const companySchema = z.object({
  companyId: z.string().optional(),
  name: z.string().trim().min(1, 'Informe o nome').max(160),
  document: z.string().trim().min(1, 'Informe o documento').max(40),
  email: optionalText,
  phone: optionalText,
  status: statusSchema,
});

const storeSchema = z.object({
  storeId: z.string().optional(),
  companyId: z.string().optional(),
  name: z.string().trim().min(1, 'Informe o nome').max(160),
  document: z.string().trim().min(1, 'Informe o documento').max(40),
  email: optionalText,
  phone: optionalText,
  city: optionalText,
  state: z.string().trim().max(2, 'Use a sigla UF').optional().or(z.literal('')),
  address: optionalText,
  status: statusSchema,
});

const distributionSchema = z.object({
  companyId: z.string().optional(),
  storeId: z.string().optional(),
  mode: z.enum(['MANUAL', 'ROUND_ROBIN', 'LEAST_BUSY']),
  active: z.boolean(),
});

const slaSchema = z.object({
  companyId: z.string().optional(),
  storeId: z.string().optional(),
  minutesToAssign: z.coerce.number().min(1, 'Use pelo menos 1 minuto'),
  minutesToFirstContact: z.coerce.number().min(1, 'Use pelo menos 1 minuto'),
  active: z.boolean(),
});

type CompanyForm = z.infer<typeof companySchema>;
type StoreForm = z.infer<typeof storeSchema>;
type DistributionForm = z.infer<typeof distributionSchema>;
type SlaForm = z.infer<typeof slaSchema>;

const tabs = ['Empresa', 'Loja', 'Usuarios', 'Distribuicao', 'SLA', 'Templates', 'E-mail', 'Sistema'];

function clean<T extends Record<string, unknown>>(values: T): T {
  return Object.fromEntries(Object.entries(values).map(([key, value]) => [key, value === '' ? undefined : value])) as T;
}

export function SettingsPage() {
  const { hasAnyRole, user } = useAuth();
  const queryClient = useQueryClient();
  const metadata = useMetadata();
  const isAdmin = hasAnyRole(['ADMIN']);
  const [tab, setTab] = useState(0);
  const [selectedCompanyId, setSelectedCompanyId] = useState(user?.companyId);
  const [selectedStoreId, setSelectedStoreId] = useState(user?.storeId);

  const settingsQuery = useQuery({
    queryKey: ['settings', selectedCompanyId, selectedStoreId],
    queryFn: () => getSettings({ companyId: selectedCompanyId, storeId: selectedStoreId }),
    enabled: Boolean(selectedCompanyId && selectedStoreId),
  });

  const companyForm = useForm<CompanyForm>({ resolver: zodResolver(companySchema) });
  const storeForm = useForm<StoreForm>({ resolver: zodResolver(storeSchema) });
  const distributionForm = useForm<DistributionForm>({ resolver: zodResolver(distributionSchema) });
  const slaForm = useForm<SlaForm>({ resolver: zodResolver(slaSchema) });

  useEffect(() => {
    if (!settingsQuery.data) {
      return;
    }
    const { company, distribution, store } = settingsQuery.data;
    companyForm.reset({
      companyId: company.id,
      name: company.name,
      document: company.document,
      email: company.email ?? '',
      phone: company.phone ?? '',
      status: company.status,
    });
    storeForm.reset({
      storeId: store.id,
      companyId: store.companyId,
      name: store.name,
      document: store.document,
      email: store.email ?? '',
      phone: store.phone ?? '',
      city: store.city ?? '',
      state: store.state ?? '',
      address: store.address ?? '',
      status: store.status,
    });
    distributionForm.reset({
      companyId: distribution.companyId,
      storeId: distribution.storeId,
      mode: distribution.mode,
      active: distribution.active,
    });
    slaForm.reset({
      companyId: distribution.companyId,
      storeId: distribution.storeId,
      minutesToAssign: distribution.minutesToAssign,
      minutesToFirstContact: distribution.minutesToFirstContact,
      active: distribution.slaActive,
    });
  }, [companyForm, distributionForm, settingsQuery.data, slaForm, storeForm]);

  const invalidateSettings = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['settings'] }),
      queryClient.invalidateQueries({ queryKey: ['distribution-config'] }),
      queryClient.invalidateQueries({ queryKey: ['lead-dashboard'] }),
    ]);
  };

  const companyMutation = useMutation({
    mutationFn: (payload: SettingsCompanyPayload) => updateSettingsCompany(payload),
    onSuccess: invalidateSettings,
  });
  const storeMutation = useMutation({
    mutationFn: (payload: SettingsStorePayload) => updateSettingsStore(payload),
    onSuccess: invalidateSettings,
  });
  const distributionMutation = useMutation({
    mutationFn: (payload: SettingsDistributionPayload) => updateSettingsDistribution(payload),
    onSuccess: invalidateSettings,
  });
  const slaMutation = useMutation({
    mutationFn: (payload: SettingsSlaPayload) => updateSettingsSla(payload),
    onSuccess: invalidateSettings,
  });

  const activeUsers = useMemo(() => settingsQuery.data?.users.filter((item) => item.status === 'ACTIVE').length ?? 0, [settingsQuery.data]);
  const activeTemplates = useMemo(() => settingsQuery.data?.templates.filter((item) => item.active).length ?? 0, [settingsQuery.data]);
  const activeEmailAccounts = useMemo(() => settingsQuery.data?.emailAccounts.filter((item) => item.active).length ?? 0, [settingsQuery.data]);

  function handleStoreChange(storeId: string) {
    const store = settingsQuery.data?.availableStores.find((item) => item.id === storeId);
    setSelectedStoreId(storeId);
    if (store) {
      setSelectedCompanyId(store.companyId);
    }
  }

  function handleCompanyChange(companyId: string) {
    setSelectedCompanyId(companyId);
    const firstStore = settingsQuery.data?.availableStores.find((store) => store.companyId === companyId);
    if (firstStore) {
      setSelectedStoreId(firstStore.id);
    }
  }

  if (user && !hasAnyRole(['ADMIN', 'MANAGER'])) {
    return <Alert severity="error">Voce nao tem acesso as configuracoes administrativas.</Alert>;
  }

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Box>
        <Typography component="h2" variant="h4" fontWeight={800}>
          Configuracoes
        </Typography>
        <Typography color="text.secondary">Central administrativa de empresa, loja, distribuicao, SLA e canais operacionais.</Typography>
      </Box>

      {settingsQuery.isLoading && <LinearProgress />}
      {settingsQuery.isError && <Alert severity="error">Nao foi possivel carregar as configuracoes.</Alert>}
      {companyMutation.isSuccess && <Alert severity="success">Dados da empresa salvos.</Alert>}
      {storeMutation.isSuccess && <Alert severity="success">Dados da loja salvos.</Alert>}
      {distributionMutation.isSuccess && <Alert severity="success">Distribuicao salva.</Alert>}
      {slaMutation.isSuccess && <Alert severity="success">SLA salvo.</Alert>}
      {(companyMutation.isError || storeMutation.isError || distributionMutation.isError || slaMutation.isError) && (
        <Alert severity="error">Nao foi possivel salvar a configuracao.</Alert>
      )}

      {settingsQuery.data && (
        <>
          <Paper variant="outlined" sx={{ borderRadius: 1, p: 2 }}>
            <Grid2 container spacing={2}>
              <Grid2 size={{ xs: 12, md: 6 }}>
                <TextField
                  disabled={!isAdmin}
                  fullWidth
                  label="Empresa"
                  onChange={(event) => handleCompanyChange(event.target.value)}
                  select
                  value={selectedCompanyId ?? ''}
                >
                  {settingsQuery.data.availableCompanies.map((company) => (
                    <MenuItem key={company.id} value={company.id}>
                      {company.name}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid2>
              <Grid2 size={{ xs: 12, md: 6 }}>
                <TextField fullWidth label="Loja" onChange={(event) => handleStoreChange(event.target.value)} select value={selectedStoreId ?? ''}>
                  {settingsQuery.data.availableStores
                    .filter((store) => !selectedCompanyId || store.companyId === selectedCompanyId)
                    .map((store) => (
                      <MenuItem key={store.id} value={store.id}>
                        {store.name}
                      </MenuItem>
                    ))}
                </TextField>
              </Grid2>
            </Grid2>
          </Paper>

          <Paper variant="outlined" sx={{ borderRadius: 1 }}>
            <Tabs onChange={(_, value) => setTab(value)} value={tab} variant="scrollable" scrollButtons="auto">
              {tabs.map((item) => (
                <Tab key={item} label={item} />
              ))}
            </Tabs>
          </Paper>

          {tab === 0 && (
            <Paper component="form" onSubmit={companyForm.handleSubmit((values) => companyMutation.mutate(clean(values)))} variant="outlined" sx={{ borderRadius: 1, p: 3 }}>
              <Stack spacing={2.5}>
                <SectionTitle title="Empresa" description={isAdmin ? 'Dados cadastrais da empresa selecionada.' : 'Somente administradores alteram dados da empresa.'} />
                <Grid2 container spacing={2}>
                  <Grid2 size={{ xs: 12, md: 6 }}>
                    <TextField disabled={!isAdmin} fullWidth label="Nome" {...companyForm.register('name')} error={Boolean(companyForm.formState.errors.name)} helperText={companyForm.formState.errors.name?.message} />
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 6 }}>
                    <TextField disabled={!isAdmin} fullWidth label="Documento" {...companyForm.register('document')} error={Boolean(companyForm.formState.errors.document)} helperText={companyForm.formState.errors.document?.message} />
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 4 }}>
                    <TextField disabled={!isAdmin} fullWidth label="E-mail" {...companyForm.register('email')} />
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 4 }}>
                    <TextField disabled={!isAdmin} fullWidth label="Telefone" {...companyForm.register('phone')} />
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 4 }}>
                    <TextField disabled={!isAdmin} fullWidth label="Status" select {...companyForm.register('status')}>
                      {metadata.options('tenantStatuses').map((status) => (
                        <MenuItem key={status.code} value={status.code}>
                          {status.label}
                        </MenuItem>
                      ))}
                    </TextField>
                  </Grid2>
                </Grid2>
                <SaveButton disabled={!isAdmin || companyMutation.isPending} />
              </Stack>
            </Paper>
          )}

          {tab === 1 && (
            <Paper component="form" onSubmit={storeForm.handleSubmit((values) => storeMutation.mutate(clean(values)))} variant="outlined" sx={{ borderRadius: 1, p: 3 }}>
              <Stack spacing={2.5}>
                <SectionTitle title="Loja" description="Dados comerciais e endereco da loja selecionada." />
                <Grid2 container spacing={2}>
                  <Grid2 size={{ xs: 12, md: 6 }}>
                    <TextField fullWidth label="Nome" {...storeForm.register('name')} error={Boolean(storeForm.formState.errors.name)} helperText={storeForm.formState.errors.name?.message} />
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 6 }}>
                    <TextField fullWidth label="Documento" {...storeForm.register('document')} error={Boolean(storeForm.formState.errors.document)} helperText={storeForm.formState.errors.document?.message} />
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 4 }}>
                    <TextField fullWidth label="E-mail" {...storeForm.register('email')} />
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 4 }}>
                    <TextField fullWidth label="Telefone" {...storeForm.register('phone')} />
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 4 }}>
                    <TextField fullWidth label="Status" select {...storeForm.register('status')}>
                      {metadata.options('tenantStatuses').map((status) => (
                        <MenuItem key={status.code} value={status.code}>
                          {status.label}
                        </MenuItem>
                      ))}
                    </TextField>
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 5 }}>
                    <TextField fullWidth label="Cidade" {...storeForm.register('city')} />
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 2 }}>
                    <TextField fullWidth label="UF" {...storeForm.register('state')} error={Boolean(storeForm.formState.errors.state)} helperText={storeForm.formState.errors.state?.message} />
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 5 }}>
                    <TextField fullWidth label="Endereco" {...storeForm.register('address')} />
                  </Grid2>
                </Grid2>
                <SaveButton disabled={storeMutation.isPending} />
              </Stack>
            </Paper>
          )}

          {tab === 2 && (
            <SummaryPanel
              icon={<GroupsIcon />}
              title="Usuarios"
              metrics={[
                ['Total', settingsQuery.data.users.length],
                ['Ativos', activeUsers],
                ['Inativos', settingsQuery.data.users.length - activeUsers],
              ]}
              empty={settingsQuery.data.users.length === 0}
              emptyText="Nenhum usuario encontrado para a loja selecionada."
            />
          )}

          {tab === 3 && (
            <Paper component="form" onSubmit={distributionForm.handleSubmit((values) => distributionMutation.mutate(clean(values)))} variant="outlined" sx={{ borderRadius: 1, p: 3 }}>
              <Stack spacing={2.5}>
                <SectionTitle title="Distribuicao" description="Controle de atribuicao automatica de leads da loja." />
                <Grid2 container spacing={2}>
                  <Grid2 size={{ xs: 12, md: 6 }}>
                    <TextField fullWidth label="Modo" select {...distributionForm.register('mode')}>
                      {distributionModes.map((mode) => (
                        <MenuItem key={mode} value={mode}>
                          {metadata.label('leadDistributionModes', mode)}
                        </MenuItem>
                      ))}
                    </TextField>
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 6 }}>
                    <Controller
                      control={distributionForm.control}
                      name="active"
                      render={({ field }) => (
                        <FormControlLabel control={<Switch checked={field.value} onChange={(event) => field.onChange(event.target.checked)} />} label="Distribuicao automatica ativa" />
                      )}
                    />
                  </Grid2>
                </Grid2>
                <SaveButton disabled={distributionMutation.isPending} />
              </Stack>
            </Paper>
          )}

          {tab === 4 && (
            <Paper component="form" onSubmit={slaForm.handleSubmit((values) => slaMutation.mutate(clean(values)))} variant="outlined" sx={{ borderRadius: 1, p: 3 }}>
              <Stack spacing={2.5}>
                <SectionTitle title="SLA" description="Prazos de resposta usados nos indicadores de leads atrasados." />
                <Grid2 container spacing={2}>
                  <Grid2 size={{ xs: 12, md: 4 }}>
                    <TextField fullWidth label="Minutos para atribuir" type="number" {...slaForm.register('minutesToAssign')} error={Boolean(slaForm.formState.errors.minutesToAssign)} helperText={slaForm.formState.errors.minutesToAssign?.message} />
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 4 }}>
                    <TextField fullWidth label="Minutos para primeiro contato" type="number" {...slaForm.register('minutesToFirstContact')} error={Boolean(slaForm.formState.errors.minutesToFirstContact)} helperText={slaForm.formState.errors.minutesToFirstContact?.message} />
                  </Grid2>
                  <Grid2 size={{ xs: 12, md: 4 }}>
                    <Controller
                      control={slaForm.control}
                      name="active"
                      render={({ field }) => (
                        <FormControlLabel control={<Switch checked={field.value} onChange={(event) => field.onChange(event.target.checked)} />} label="Controle de SLA ativo" />
                      )}
                    />
                  </Grid2>
                </Grid2>
                <SaveButton disabled={slaMutation.isPending} />
              </Stack>
            </Paper>
          )}

          {tab === 5 && (
            <SummaryPanel
              icon={<TextSnippetIcon />}
              title="Templates"
              metrics={[
                ['Total', settingsQuery.data.templates.length],
                ['Ativos', activeTemplates],
                ['Inativos', settingsQuery.data.templates.length - activeTemplates],
              ]}
              empty={settingsQuery.data.templates.length === 0}
              emptyText="Nenhum template cadastrado para a loja selecionada."
            />
          )}

          {tab === 6 && (
            <SummaryPanel
              icon={<EmailIcon />}
              title="E-mail"
              metrics={[
                ['Contas', settingsQuery.data.emailAccounts.length],
                ['Ativas', activeEmailAccounts],
                ['Inativas', settingsQuery.data.emailAccounts.length - activeEmailAccounts],
              ]}
              empty={settingsQuery.data.emailAccounts.length === 0}
              emptyText="Nenhuma conta de e-mail cadastrada para a loja selecionada."
            />
          )}

          {tab === 7 && (
            <SummaryPanel
              icon={<SettingsIcon />}
              title="Sistema"
              metrics={[
                ['Fuso horario', settingsQuery.data.system.timezone],
                ['Idioma', settingsQuery.data.system.locale],
                ['Formato de data', settingsQuery.data.system.dateFormat],
                ['Notificacoes', settingsQuery.data.system.notificationsEnabled ? 'Ativas' : 'Inativas'],
              ]}
              empty={false}
              emptyText=""
            />
          )}
        </>
      )}
    </Box>
  );
}

function SectionTitle({ description, title }: { description: string; title: string }) {
  return (
    <Box>
      <Typography variant="h6" fontWeight={800}>
        {title}
      </Typography>
      <Typography color="text.secondary">{description}</Typography>
    </Box>
  );
}

function SaveButton({ disabled }: { disabled: boolean }) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
      <Button disabled={disabled} startIcon={<SaveIcon />} type="submit" variant="contained">
        Salvar
      </Button>
    </Box>
  );
}

function SummaryPanel({
  empty,
  emptyText,
  icon,
  metrics,
  title,
}: {
  empty: boolean;
  emptyText: string;
  icon: React.ReactNode;
  metrics: Array<[string, string | number]>;
  title: string;
}) {
  return (
    <Paper variant="outlined" sx={{ borderRadius: 1, p: 3 }}>
      <Stack spacing={2.5}>
        <Stack alignItems="center" direction="row" spacing={1.5}>
          {icon}
          <Typography variant="h6" fontWeight={800}>
            {title}
          </Typography>
        </Stack>
        {empty ? (
          <Alert severity="info">{emptyText}</Alert>
        ) : (
          <Grid2 container spacing={2}>
            {metrics.map(([label, value]) => (
              <Grid2 key={label} size={{ xs: 12, md: 3 }}>
                <Box sx={{ border: 1, borderColor: 'divider', borderRadius: 1, p: 2, minHeight: 88 }}>
                  <Typography color="text.secondary" variant="body2">
                    {label}
                  </Typography>
                  <Typography variant="h6" fontWeight={800}>
                    {value}
                  </Typography>
                </Box>
              </Grid2>
            ))}
          </Grid2>
        )}
        <Divider />
        <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
          <Chip label="ADMIN: acesso completo" size="small" />
          <Chip label="MANAGER: loja" size="small" />
          <Chip label="SELLER: sem acesso" size="small" />
        </Stack>
      </Stack>
    </Paper>
  );
}
