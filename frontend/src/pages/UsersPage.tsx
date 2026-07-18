import { zodResolver } from '@hookform/resolvers/zod';
import AddIcon from '@mui/icons-material/Add';
import LinkIcon from '@mui/icons-material/Link';
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Chip,
  FormControlLabel,
  Grid2,
  MenuItem,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Controller, useForm } from 'react-hook-form';
import { useEffect, useMemo } from 'react';
import { z } from 'zod';
import { useAuth } from '../hooks/useAuth';
import { useMetadata } from '../hooks/useMetadata';
import { listCompanies } from '../services/companyService';
import { listStores } from '../services/storeService';
import { assignUserTenant, createUser, listUsers } from '../services/userService';
import type { AuthUser, UserRole } from '../types/auth';

const roles: UserRole[] = ['ADMIN', 'MANAGER', 'STORE_MANAGER', 'SELLER', 'PRE_SALES', 'F_AND_I', 'AVALIADOR'];

const userSchema = z.object({
  name: z.string().min(1, 'Informe o nome').max(160),
  email: z.string().email('Informe um e-mail valido').max(180),
  password: z.string().min(6, 'Use ao menos 6 caracteres').max(80),
  phone: z.string().max(40).optional(),
  jobTitle: z.string().max(120).optional(),
  companyId: z.string().min(1, 'Selecione a empresa'),
  storeId: z.string().min(1, 'Selecione a loja'),
  roles: z.array(z.enum(['ADMIN', 'MANAGER', 'STORE_MANAGER', 'SELLER', 'PRE_SALES', 'F_AND_I', 'AVALIADOR'])).min(1, 'Selecione uma role'),
});

const tenantSchema = z.object({
  userId: z.string().min(1, 'Selecione o usuario'),
  companyId: z.string().min(1, 'Selecione a empresa'),
  storeId: z.string().min(1, 'Selecione a loja'),
});

type UserFormValues = z.infer<typeof userSchema>;
type TenantFormValues = z.infer<typeof tenantSchema>;

export function UsersPage() {
  const { hasAnyRole } = useAuth();
  const queryClient = useQueryClient();
  const metadata = useMetadata();
  const canManageUsers = hasAnyRole(['ADMIN']);

  const usersQuery = useQuery({
    queryKey: ['users'],
    queryFn: listUsers,
  });

  const companiesQuery = useQuery({
    queryKey: ['companies'],
    queryFn: listCompanies,
    enabled: canManageUsers,
  });

  const {
    control,
    formState: { errors },
    handleSubmit,
    register,
    reset,
    setValue,
    watch,
  } = useForm<UserFormValues>({
    resolver: zodResolver(userSchema),
    defaultValues: {
      name: '',
      email: '',
      password: '',
      phone: '',
      jobTitle: '',
      companyId: '',
      storeId: '',
      roles: ['SELLER'],
    },
  });

  const {
    formState: { errors: tenantErrors },
    handleSubmit: handleTenantSubmit,
    register: registerTenant,
    reset: resetTenant,
    setValue: setTenantValue,
    watch: watchTenant,
  } = useForm<TenantFormValues>({
    resolver: zodResolver(tenantSchema),
    defaultValues: {
      userId: '',
      companyId: '',
      storeId: '',
    },
  });

  const selectedCompanyId = watch('companyId');
  const selectedTenantCompanyId = watchTenant('companyId');

  const storesQuery = useQuery({
    queryKey: ['stores', selectedCompanyId],
    queryFn: () => listStores(selectedCompanyId),
    enabled: canManageUsers && Boolean(selectedCompanyId),
  });

  const tenantStoresQuery = useQuery({
    queryKey: ['stores', selectedTenantCompanyId],
    queryFn: () => listStores(selectedTenantCompanyId),
    enabled: canManageUsers && Boolean(selectedTenantCompanyId),
  });

  useEffect(() => {
    const firstCompany = companiesQuery.data?.[0];
    if (firstCompany && !selectedCompanyId) {
      setValue('companyId', firstCompany.id);
    }
  }, [companiesQuery.data, selectedCompanyId, setValue]);

  useEffect(() => {
    if (storesQuery.data?.length) {
      setValue('storeId', storesQuery.data[0].id);
    }
  }, [selectedCompanyId, setValue, storesQuery.data]);

  useEffect(() => {
    if (tenantStoresQuery.data?.length) {
      setTenantValue('storeId', tenantStoresQuery.data[0].id);
    }
  }, [selectedTenantCompanyId, setTenantValue, tenantStoresQuery.data]);

  const createUserMutation = useMutation({
    mutationFn: createUser,
    onSuccess: async () => {
      reset({
        name: '',
        email: '',
        password: '',
        phone: '',
        jobTitle: '',
        companyId: selectedCompanyId,
        storeId: storesQuery.data?.[0]?.id ?? '',
        roles: ['SELLER'],
      });
      await queryClient.invalidateQueries({ queryKey: ['users'] });
    },
  });

  const assignTenantMutation = useMutation({
    mutationFn: (values: TenantFormValues) =>
      assignUserTenant(values.userId, {
        companyId: values.companyId,
        storeId: values.storeId,
      }),
    onSuccess: async () => {
      resetTenant();
      await queryClient.invalidateQueries({ queryKey: ['users'] });
    },
  });

  const knownStores = useMemo(
    () => [...(storesQuery.data ?? []), ...(tenantStoresQuery.data ?? [])],
    [storesQuery.data, tenantStoresQuery.data],
  );

  function companyName(companyId: string) {
    return companiesQuery.data?.find((company) => company.id === companyId)?.name ?? companyId;
  }

  function storeName(storeId: string) {
    return knownStores.find((store) => store.id === storeId)?.name ?? storeId;
  }

  function startTenantAssignment(user: AuthUser) {
    resetTenant({
      userId: user.id,
      companyId: user.companyId,
      storeId: user.storeId,
    });
  }

  function onSubmit(values: UserFormValues) {
    createUserMutation.mutate(values);
  }

  function onTenantSubmit(values: TenantFormValues) {
    assignTenantMutation.mutate(values);
  }

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Box>
        <Typography component="h2" variant="h4" fontWeight={800}>
          Usuarios
        </Typography>
        <Typography color="text.secondary">Controle de acesso da equipe comercial.</Typography>
      </Box>

      <Grid2 container spacing={3}>
        <Grid2 size={{ xs: 12, lg: canManageUsers ? 8 : 12 }}>
          <Paper variant="outlined" sx={{ borderRadius: 1, overflow: 'hidden' }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Nome</TableCell>
                  <TableCell>E-mail</TableCell>
                  <TableCell>Cargo</TableCell>
                  <TableCell>Empresa</TableCell>
                  <TableCell>Loja</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Roles</TableCell>
                  {canManageUsers && <TableCell align="right">Acoes</TableCell>}
                </TableRow>
              </TableHead>
              <TableBody>
                {usersQuery.data?.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell>{user.name}</TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>{user.jobTitle ?? '-'}</TableCell>
                    <TableCell>{companyName(user.companyId)}</TableCell>
                    <TableCell>{storeName(user.storeId)}</TableCell>
                    <TableCell>
                      <Chip color={metadata.color('userStatuses', user.status)} label={metadata.label('userStatuses', user.status)} size="small" />
                    </TableCell>
                    <TableCell>
                      <Stack direction="row" flexWrap="wrap" gap={0.75}>
                        {user.roles.map((role) => (
                          <Chip key={role} label={metadata.label('userRoles', role)} size="small" variant="outlined" />
                        ))}
                      </Stack>
                    </TableCell>
                    {canManageUsers && (
                      <TableCell align="right">
                        <Button onClick={() => startTenantAssignment(user)} size="small" startIcon={<LinkIcon />} variant="outlined">
                          Vincular
                        </Button>
                      </TableCell>
                    )}
                  </TableRow>
                ))}
                {!usersQuery.isLoading && usersQuery.data?.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={canManageUsers ? 8 : 7}>Nenhum usuario encontrado.</TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </Paper>
        </Grid2>

        {canManageUsers && (
          <Grid2 size={{ xs: 12, lg: 4 }}>
            <Stack spacing={3}>
            <Paper
              component="form"
              onSubmit={handleSubmit(onSubmit)}
              variant="outlined"
              sx={{ p: 3, borderRadius: 1, display: 'grid', gap: 2 }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <AddIcon color="primary" />
                <Typography component="h3" variant="h6" fontWeight={700}>
                  Novo usuario
                </Typography>
              </Box>

              {createUserMutation.isError && <Alert severity="error">Nao foi possivel criar o usuario.</Alert>}

              <TextField label="Nome" error={Boolean(errors.name)} helperText={errors.name?.message} {...register('name')} />
              <TextField
                label="E-mail"
                type="email"
                error={Boolean(errors.email)}
                helperText={errors.email?.message}
                {...register('email')}
              />
              <TextField
                label="Senha"
                type="password"
                error={Boolean(errors.password)}
                helperText={errors.password?.message}
                {...register('password')}
              />
              <TextField label="Telefone" error={Boolean(errors.phone)} helperText={errors.phone?.message} {...register('phone')} />
              <TextField
                label="Cargo"
                error={Boolean(errors.jobTitle)}
                helperText={errors.jobTitle?.message}
                {...register('jobTitle')}
              />
              <TextField select label="Empresa" error={Boolean(errors.companyId)} helperText={errors.companyId?.message} {...register('companyId')}>
                {companiesQuery.data?.map((company) => (
                  <MenuItem key={company.id} value={company.id}>
                    {company.name}
                  </MenuItem>
                ))}
              </TextField>
              <TextField select label="Loja" error={Boolean(errors.storeId)} helperText={errors.storeId?.message} {...register('storeId')}>
                {storesQuery.data?.map((store) => (
                  <MenuItem key={store.id} value={store.id}>
                    {store.name}
                  </MenuItem>
                ))}
              </TextField>
              <Controller
                control={control}
                name="roles"
                render={({ field }) => (
                  <Box>
                    <Typography variant="body2" fontWeight={700} sx={{ mb: 0.5 }}>
                      Roles
                    </Typography>
                    {roles.map((role) => (
                      <FormControlLabel
                        key={role}
                        control={
                          <Checkbox
                            checked={field.value.includes(role)}
                            onChange={(event) => {
                              const nextValue = event.target.checked
                                ? [...field.value, role]
                                : field.value.filter((value) => value !== role);
                              field.onChange(nextValue);
                            }}
                          />
                        }
                        label={metadata.label('userRoles', role)}
                      />
                    ))}
                    {errors.roles && (
                      <Typography color="error" variant="caption">
                        {errors.roles.message}
                      </Typography>
                    )}
                  </Box>
                )}
              />
              <Button disabled={createUserMutation.isPending} type="submit" variant="contained">
                Criar usuario
              </Button>
            </Paper>

            <Paper
              component="form"
              onSubmit={handleTenantSubmit(onTenantSubmit)}
              variant="outlined"
              sx={{ p: 3, borderRadius: 1, display: 'grid', gap: 2 }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <LinkIcon color="primary" />
                <Typography component="h3" variant="h6" fontWeight={700}>
                  Vincular loja
                </Typography>
              </Box>

              {assignTenantMutation.isError && <Alert severity="error">Nao foi possivel vincular o usuario.</Alert>}

              <TextField select label="Usuario" error={Boolean(tenantErrors.userId)} helperText={tenantErrors.userId?.message} {...registerTenant('userId')}>
                {usersQuery.data?.map((user) => (
                  <MenuItem key={user.id} value={user.id}>
                    {user.name}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                select
                label="Empresa"
                error={Boolean(tenantErrors.companyId)}
                helperText={tenantErrors.companyId?.message}
                {...registerTenant('companyId')}
              >
                {companiesQuery.data?.map((company) => (
                  <MenuItem key={company.id} value={company.id}>
                    {company.name}
                  </MenuItem>
                ))}
              </TextField>
              <TextField select label="Loja" error={Boolean(tenantErrors.storeId)} helperText={tenantErrors.storeId?.message} {...registerTenant('storeId')}>
                {tenantStoresQuery.data?.map((store) => (
                  <MenuItem key={store.id} value={store.id}>
                    {store.name}
                  </MenuItem>
                ))}
              </TextField>
              <Button disabled={assignTenantMutation.isPending} type="submit" variant="contained">
                Salvar vinculo
              </Button>
            </Paper>
            </Stack>
          </Grid2>
        )}
      </Grid2>
    </Box>
  );
}
