import { zodResolver } from '@hookform/resolvers/zod';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import MarkEmailReadIcon from '@mui/icons-material/MarkEmailRead';
import SyncIcon from '@mui/icons-material/Sync';
import {
  Alert,
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Grid2,
  IconButton,
  MenuItem,
  Stack,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useMemo, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { z } from 'zod';
import { PageHeader } from '../components/PageHeader';
import { RecordCard, RecordCardRow } from '../components/RecordCard';
import { ResponsiveDataView } from '../components/ResponsiveDataView';
import { useAuth } from '../hooks/useAuth';
import { useMetadata } from '../hooks/useMetadata';
import { listCompanies } from '../services/companyService';
import {
  createEmailAccount,
  deleteEmailAccount,
  listEmailAccounts,
  syncEmailAccount,
  testEmailAccount,
  updateEmailAccount,
} from '../services/emailAccountService';
import { listStores } from '../services/storeService';
import type { EmailAccount } from '../types/emailAccount';

const schema = z.object({
  companyId: z.string().min(1, 'Selecione a empresa'),
  storeId: z.string().min(1, 'Selecione a loja'),
  name: z.string().min(1, 'Informe o nome').max(120),
  host: z.string().min(1, 'Informe o host').max(180),
  port: z.coerce.number().min(1).max(65535),
  username: z.string().min(1, 'Informe o usuario').max(180),
  password: z.string(),
  protocol: z.literal('IMAP'),
  useSsl: z.boolean(),
  active: z.boolean(),
});

type FormInput = z.input<typeof schema>;
type FormValues = z.output<typeof schema>;

export function EmailAccountsPage() {
  const { hasAnyRole, user } = useAuth();
  const queryClient = useQueryClient();
  const metadata = useMetadata();
  const isAdmin = hasAnyRole(['ADMIN']);
  const [editingAccount, setEditingAccount] = useState<EmailAccount | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [feedback, setFeedback] = useState<string | null>(null);

  const emptyValues = useMemo<FormInput>(
    () => ({
      companyId: user?.companyId ?? '',
      storeId: user?.storeId ?? '',
      name: '',
      host: '',
      port: 993,
      username: '',
      password: '',
      protocol: 'IMAP',
      useSsl: true,
      active: true,
    }),
    [user?.companyId, user?.storeId],
  );

  const accountsQuery = useQuery({ queryKey: ['email-accounts'], queryFn: listEmailAccounts });
  const storesQuery = useQuery({ queryKey: ['stores'], queryFn: () => listStores() });
  const companiesQuery = useQuery({ queryKey: ['companies'], queryFn: listCompanies, enabled: isAdmin });

  const { control, formState: { errors }, handleSubmit, register, reset, setValue } = useForm<FormInput, unknown, FormValues>({
    resolver: zodResolver(schema),
    defaultValues: emptyValues,
  });

  useEffect(() => {
    if (!dialogOpen) {
      reset(emptyValues);
    }
  }, [dialogOpen, emptyValues, reset]);

  useEffect(() => {
    if (storesQuery.data?.length && !emptyValues.storeId && !editingAccount) {
      setValue('storeId', storesQuery.data[0].id);
    }
  }, [editingAccount, emptyValues.storeId, setValue, storesQuery.data]);

  const saveMutation = useMutation({
    mutationFn: (values: FormValues) => {
      const payload = { ...values, password: values.password || undefined };
      return editingAccount ? updateEmailAccount(editingAccount.id, payload) : createEmailAccount(payload);
    },
    onSuccess: async () => {
      setDialogOpen(false);
      setEditingAccount(null);
      await queryClient.invalidateQueries({ queryKey: ['email-accounts'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteEmailAccount,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['email-accounts'] });
    },
  });

  const testMutation = useMutation({
    mutationFn: testEmailAccount,
    onSuccess: async (result) => {
      setFeedback(result.message);
      await queryClient.invalidateQueries({ queryKey: ['email-accounts'] });
    },
  });

  const syncMutation = useMutation({
    mutationFn: syncEmailAccount,
    onSuccess: async (result) => {
      setFeedback(result.message);
      await queryClient.invalidateQueries({ queryKey: ['email-accounts'] });
      await queryClient.invalidateQueries({ queryKey: ['leads'] });
    },
  });

  function openCreate() {
    setEditingAccount(null);
    reset(emptyValues);
    setDialogOpen(true);
  }

  function openEdit(account: EmailAccount) {
    setEditingAccount(account);
    reset({ ...account, password: '' });
    setDialogOpen(true);
  }

  function companyName(companyId: string) {
    return companiesQuery.data?.find((company) => company.id === companyId)?.name ?? companyId;
  }

  function storeName(storeId: string) {
    return storesQuery.data?.find((store) => store.id === storeId)?.name ?? storeId;
  }

  function onSubmit(values: FormValues) {
    saveMutation.mutate(values);
  }

  const accounts = accountsQuery.data ?? [];

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <PageHeader
        action={
          <Button onClick={openCreate} startIcon={<AddIcon />} variant="contained">
            Nova conta
          </Button>
        }
        description="Captura automatica de leads via caixas IMAP por loja."
        title="Contas de E-mail"
      />

      {feedback && <Alert onClose={() => setFeedback(null)} severity="success">{feedback}</Alert>}
      {(saveMutation.isError || testMutation.isError || syncMutation.isError) && <Alert severity="error">Operacao nao concluida.</Alert>}

      <ResponsiveDataView
        cards={accounts.map((account) => (
          <RecordCard
            key={account.id}
            actions={
              <>
                <Tooltip title="Testar conexao">
                  <IconButton aria-label="Testar conexao" onClick={() => testMutation.mutate(account.id)} size="small">
                    <MarkEmailReadIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Sincronizar">
                  <IconButton aria-label="Sincronizar" onClick={() => syncMutation.mutate(account.id)} size="small">
                    <SyncIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Editar">
                  <IconButton aria-label="Editar conta" onClick={() => openEdit(account)} size="small">
                    <EditIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Excluir">
                  <IconButton aria-label="Excluir conta" onClick={() => deleteMutation.mutate(account.id)} size="small">
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              </>
            }
            status={
              <Chip
                color={metadata.color('emailAccountStatuses', account.lastSyncStatus)}
                label={metadata.label('emailAccountStatuses', account.lastSyncStatus)}
                size="small"
              />
            }
            subtitle={account.username}
            title={account.name}
          >
            <RecordCardRow label="Servidor" value={`${account.host}:${account.port}`} />
            <RecordCardRow label="Loja" value={storeName(account.storeId)} />
            {isAdmin && <RecordCardRow label="Empresa" value={companyName(account.companyId)} />}
            <RecordCardRow label="Ultima leitura" value={account.lastReadAt ? new Date(account.lastReadAt).toLocaleString('pt-BR') : '-'} />
            <RecordCardRow label="Sincronizacao" value={account.lastSyncMessage ?? 'Sem sincronizacao'} />
          </RecordCard>
        ))}
        empty={!accountsQuery.isLoading && accounts.length === 0}
        emptyMessage="Nenhuma conta cadastrada."
        error={accountsQuery.isError}
        loading={accountsQuery.isLoading}
        loadingLabel="Carregando contas de e-mail"
        table={
          <Table sx={{ minWidth: 1080 }}>
            <TableHead>
              <TableRow>
                <TableCell>Conta</TableCell>
                <TableCell>Servidor</TableCell>
                <TableCell>Loja</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Ultima leitura</TableCell>
                <TableCell align="right" sx={{ minWidth: 180 }}>
                  Acoes
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {accounts.map((account) => (
                <TableRow hover key={account.id}>
                  <TableCell>
                    <Typography variant="body2" fontWeight={700}>{account.name}</Typography>
                    <Typography variant="caption" color="text.secondary">{account.username}</Typography>
                  </TableCell>
                  <TableCell>{account.host}:{account.port}</TableCell>
                  <TableCell>
                    <Typography variant="body2">{storeName(account.storeId)}</Typography>
                    {isAdmin && <Typography variant="caption" color="text.secondary">{companyName(account.companyId)}</Typography>}
                  </TableCell>
                  <TableCell>
                    <Stack spacing={0.5}>
                      <Chip
                        color={metadata.color('emailAccountStatuses', account.lastSyncStatus)}
                        label={metadata.label('emailAccountStatuses', account.lastSyncStatus)}
                        size="small"
                      />
                      <Typography variant="caption" color="text.secondary">{account.lastSyncMessage ?? 'Sem sincronizacao'}</Typography>
                    </Stack>
                  </TableCell>
                  <TableCell>{account.lastReadAt ? new Date(account.lastReadAt).toLocaleString('pt-BR') : '-'}</TableCell>
                  <TableCell align="right">
                    <Stack direction="row" justifyContent="flex-end" spacing={1}>
                      <Tooltip title="Testar conexao">
                        <IconButton aria-label="Testar conexao" onClick={() => testMutation.mutate(account.id)}><MarkEmailReadIcon /></IconButton>
                      </Tooltip>
                      <Tooltip title="Sincronizar">
                        <IconButton aria-label="Sincronizar" onClick={() => syncMutation.mutate(account.id)}><SyncIcon /></IconButton>
                      </Tooltip>
                      <Tooltip title="Editar">
                        <IconButton aria-label="Editar conta" onClick={() => openEdit(account)}><EditIcon /></IconButton>
                      </Tooltip>
                      <Tooltip title="Excluir">
                        <IconButton aria-label="Excluir conta" onClick={() => deleteMutation.mutate(account.id)}><DeleteIcon /></IconButton>
                      </Tooltip>
                    </Stack>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        }
      />

      <Dialog fullWidth maxWidth="md" onClose={() => setDialogOpen(false)} open={dialogOpen}>
        <Box component="form" onSubmit={handleSubmit(onSubmit)}>
          <DialogTitle>{editingAccount ? 'Editar conta' : 'Nova conta'}</DialogTitle>
          <DialogContent>
            <Grid2 container spacing={2} sx={{ pt: 1 }}>
              <Grid2 size={{ xs: 12, md: 6 }}>
                <TextField fullWidth label="Nome" error={Boolean(errors.name)} helperText={errors.name?.message} {...register('name')} />
              </Grid2>
              <Grid2 size={{ xs: 12, md: 6 }}>
                <TextField fullWidth label="Usuario" error={Boolean(errors.username)} helperText={errors.username?.message} {...register('username')} />
              </Grid2>
              <Grid2 size={{ xs: 12, md: 6 }}>
                <TextField fullWidth label="Host IMAP" error={Boolean(errors.host)} helperText={errors.host?.message} {...register('host')} />
              </Grid2>
              <Grid2 size={{ xs: 12, md: 3 }}>
                <TextField fullWidth label="Porta" type="number" error={Boolean(errors.port)} helperText={errors.port?.message} {...register('port')} />
              </Grid2>
              <Grid2 size={{ xs: 12, md: 3 }}>
                <TextField fullWidth label="Protocolo" select {...register('protocol')}>
                  {metadata.options('emailProtocols').map((protocol) => (
                    <MenuItem key={protocol.code} value={protocol.code}>{protocol.label}</MenuItem>
                  ))}
                </TextField>
              </Grid2>
              <Grid2 size={{ xs: 12, md: 6 }}>
                <TextField fullWidth label={editingAccount ? 'Nova senha' : 'Senha'} type="password" error={Boolean(errors.password)} helperText={errors.password?.message} {...register('password')} />
              </Grid2>
              {isAdmin && (
                <Grid2 size={{ xs: 12, md: 6 }}>
                  <TextField fullWidth select label="Empresa" error={Boolean(errors.companyId)} helperText={errors.companyId?.message} {...register('companyId')}>
                    {companiesQuery.data?.map((company) => <MenuItem key={company.id} value={company.id}>{company.name}</MenuItem>)}
                  </TextField>
                </Grid2>
              )}
              {!isAdmin && (
                <Grid2 size={{ xs: 12, md: 6 }}>
                  <TextField fullWidth label="Empresa" value={user?.companyId ?? ''} slotProps={{ input: { readOnly: true } }} {...register('companyId')} />
                </Grid2>
              )}
              <Grid2 size={{ xs: 12, md: 6 }}>
                <TextField fullWidth select label="Loja" error={Boolean(errors.storeId)} helperText={errors.storeId?.message} {...register('storeId')}>
                  {storesQuery.data?.map((store) => <MenuItem key={store.id} value={store.id}>{store.name}</MenuItem>)}
                </TextField>
              </Grid2>
              <Grid2 size={{ xs: 12, md: 3 }}>
                <Controller control={control} name="useSsl" render={({ field }) => <FormControlLabel control={<Switch checked={field.value} onChange={(_, checked) => field.onChange(checked)} />} label="SSL" />} />
              </Grid2>
              <Grid2 size={{ xs: 12, md: 3 }}>
                <Controller control={control} name="active" render={({ field }) => <FormControlLabel control={<Switch checked={field.value} onChange={(_, checked) => field.onChange(checked)} />} label="Ativa" />} />
              </Grid2>
            </Grid2>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>Cancelar</Button>
            <Button disabled={saveMutation.isPending} type="submit" variant="contained">Salvar</Button>
          </DialogActions>
        </Box>
      </Dialog>
    </Box>
  );
}
