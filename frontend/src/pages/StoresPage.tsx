import { zodResolver } from '@hookform/resolvers/zod';
import AddBusinessIcon from '@mui/icons-material/AddBusiness';
import EditIcon from '@mui/icons-material/Edit';
import StorefrontIcon from '@mui/icons-material/Storefront';
import {
  Alert,
  Box,
  Button,
  Chip,
  Grid2,
  IconButton,
  MenuItem,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { useAuth } from '../hooks/useAuth';
import { useMetadata } from '../hooks/useMetadata';
import { apiErrorMessage } from '../services/api';
import { listCompanies } from '../services/companyService';
import { createStore, listStores, updateStore } from '../services/storeService';
import type { Store } from '../types/tenant';

const storeSchema = z.object({
  companyId: z.string().min(1, 'Selecione a empresa'),
  name: z.string().min(1, 'Informe o nome').max(160),
  document: z.string().min(1, 'Informe o documento').max(40),
  email: z.string().email('Informe um e-mail valido').max(180).or(z.literal('')),
  phone: z.string().max(40),
  city: z.string().max(120),
  state: z.string().max(2),
  address: z.string().max(240),
  status: z.enum(['ACTIVE', 'INACTIVE']),
});

type StoreFormValues = z.infer<typeof storeSchema>;

export function StoresPage() {
  const { hasAnyRole, user } = useAuth();
  const [editingStore, setEditingStore] = useState<Store | null>(null);
  const queryClient = useQueryClient();
  const metadata = useMetadata();
  const isAdmin = hasAnyRole(['ADMIN']);

  const emptyValues = useMemo<StoreFormValues>(
    () => ({
      companyId: user?.companyId ?? '',
      name: '',
      document: '',
      email: '',
      phone: '',
      city: '',
      state: '',
      address: '',
      status: 'ACTIVE',
    }),
    [user?.companyId],
  );

  const companiesQuery = useQuery({
    queryKey: ['companies'],
    queryFn: listCompanies,
    enabled: isAdmin,
  });

  const storesQuery = useQuery({
    queryKey: ['stores'],
    queryFn: () => listStores(),
  });

  const {
    formState: { errors },
    handleSubmit,
    register,
    reset,
  } = useForm<StoreFormValues>({
    resolver: zodResolver(storeSchema),
    defaultValues: emptyValues,
  });

  useEffect(() => {
    if (!editingStore) {
      reset(emptyValues);
      return;
    }
    reset({
      companyId: editingStore.companyId,
      name: editingStore.name,
      document: editingStore.document,
      email: editingStore.email ?? '',
      phone: editingStore.phone ?? '',
      city: editingStore.city ?? '',
      state: editingStore.state ?? '',
      address: editingStore.address ?? '',
      status: editingStore.status,
    });
  }, [editingStore, emptyValues, reset]);

  const saveStoreMutation = useMutation({
    mutationFn: (values: StoreFormValues) => {
      const payload = {
        ...values,
        email: values.email || undefined,
        phone: values.phone || undefined,
        city: values.city || undefined,
        state: values.state || undefined,
        address: values.address || undefined,
      };
      return editingStore ? updateStore(editingStore.id, payload) : createStore(payload);
    },
    onSuccess: async () => {
      setEditingStore(null);
      reset(emptyValues);
      await queryClient.invalidateQueries({ queryKey: ['stores'] });
    },
  });

  function companyName(companyId: string) {
    return companiesQuery.data?.find((company) => company.id === companyId)?.name ?? companyId;
  }

  function onSubmit(values: StoreFormValues) {
    saveStoreMutation.mutate(values);
  }

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Box>
        <Typography component="h2" variant="h4" fontWeight={800}>
          Lojas
        </Typography>
        <Typography color="text.secondary">Cadastro das lojas vinculadas as empresas.</Typography>
      </Box>

      <Grid2 container spacing={3}>
        <Grid2 size={{ xs: 12, lg: 8 }}>
          <Paper variant="outlined" sx={{ borderRadius: 1, overflow: 'hidden' }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Loja</TableCell>
                  {isAdmin && <TableCell>Empresa</TableCell>}
                  <TableCell>Cidade</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Acoes</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {storesQuery.data?.map((store) => (
                  <TableRow key={store.id}>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <StorefrontIcon color="action" fontSize="small" />
                        {store.name}
                      </Box>
                    </TableCell>
                    {isAdmin && <TableCell>{companyName(store.companyId)}</TableCell>}
                    <TableCell>{[store.city, store.state].filter(Boolean).join(' / ') || '-'}</TableCell>
                    <TableCell>
                      <Chip color={metadata.color('tenantStatuses', store.status)} label={metadata.label('tenantStatuses', store.status)} size="small" />
                    </TableCell>
                    <TableCell align="right">
                      <IconButton aria-label="Editar loja" onClick={() => setEditingStore(store)}>
                        <EditIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
                {!storesQuery.isLoading && storesQuery.data?.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={isAdmin ? 5 : 4}>Nenhuma loja encontrada.</TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </Paper>
        </Grid2>

        <Grid2 size={{ xs: 12, lg: 4 }}>
          <Paper component="form" onSubmit={handleSubmit(onSubmit)} variant="outlined" sx={{ p: 3, borderRadius: 1, display: 'grid', gap: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <AddBusinessIcon color="primary" />
              <Typography component="h3" variant="h6" fontWeight={700}>
                {editingStore ? 'Editar loja' : 'Nova loja'}
              </Typography>
            </Box>

            {saveStoreMutation.isError && (
              <Alert severity="error">{apiErrorMessage(saveStoreMutation.error) ?? 'Nao foi possivel salvar a loja.'}</Alert>
            )}

            {isAdmin ? (
              <TextField select label="Empresa" error={Boolean(errors.companyId)} helperText={errors.companyId?.message} {...register('companyId')}>
                {companiesQuery.data?.map((company) => (
                  <MenuItem key={company.id} value={company.id}>
                    {company.name}
                  </MenuItem>
                ))}
              </TextField>
            ) : (
              <TextField label="Empresa" value={user?.companyId ?? ''} slotProps={{ input: { readOnly: true } }} {...register('companyId')} />
            )}
            <TextField label="Nome" error={Boolean(errors.name)} helperText={errors.name?.message} {...register('name')} />
            <TextField label="Documento" error={Boolean(errors.document)} helperText={errors.document?.message} {...register('document')} />
            <TextField label="E-mail" type="email" error={Boolean(errors.email)} helperText={errors.email?.message} {...register('email')} />
            <TextField label="Telefone" error={Boolean(errors.phone)} helperText={errors.phone?.message} {...register('phone')} />
            <TextField label="Cidade" error={Boolean(errors.city)} helperText={errors.city?.message} {...register('city')} />
            <TextField label="UF" error={Boolean(errors.state)} helperText={errors.state?.message} {...register('state')} />
            <TextField label="Endereco" error={Boolean(errors.address)} helperText={errors.address?.message} {...register('address')} />
            <TextField select label="Status" error={Boolean(errors.status)} helperText={errors.status?.message} {...register('status')}>
              {metadata.options('tenantStatuses').map((status) => (
                <MenuItem key={status.code} value={status.code}>
                  {status.label}
                </MenuItem>
              ))}
            </TextField>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button disabled={saveStoreMutation.isPending} type="submit" variant="contained">
                Salvar
              </Button>
              {editingStore && (
                <Button onClick={() => setEditingStore(null)} type="button" variant="outlined">
                  Cancelar
                </Button>
              )}
            </Box>
          </Paper>
        </Grid2>
      </Grid2>
    </Box>
  );
}
