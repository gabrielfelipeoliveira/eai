import { zodResolver } from '@hookform/resolvers/zod';
import AddIcon from '@mui/icons-material/Add';
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Chip,
  FormControlLabel,
  Grid2,
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
import { z } from 'zod';
import { useAuth } from '../hooks/useAuth';
import { createUser, listUsers } from '../services/userService';
import type { UserRole } from '../types/auth';

const roles: UserRole[] = ['ADMIN', 'MANAGER', 'SELLER', 'RECEPTIONIST', 'AUDITOR'];

const userSchema = z.object({
  name: z.string().min(1, 'Informe o nome').max(160),
  email: z.string().email('Informe um e-mail valido').max(180),
  password: z.string().min(6, 'Use ao menos 6 caracteres').max(80),
  phone: z.string().max(40).optional(),
  jobTitle: z.string().max(120).optional(),
  roles: z.array(z.enum(['ADMIN', 'MANAGER', 'SELLER', 'RECEPTIONIST', 'AUDITOR'])).min(1, 'Selecione uma role'),
});

type UserFormValues = z.infer<typeof userSchema>;

export function UsersPage() {
  const { hasAnyRole } = useAuth();
  const queryClient = useQueryClient();
  const canManageUsers = hasAnyRole(['ADMIN']);

  const usersQuery = useQuery({
    queryKey: ['users'],
    queryFn: listUsers,
  });

  const {
    control,
    formState: { errors },
    handleSubmit,
    register,
    reset,
  } = useForm<UserFormValues>({
    resolver: zodResolver(userSchema),
    defaultValues: {
      name: '',
      email: '',
      password: '',
      phone: '',
      jobTitle: '',
      roles: ['SELLER'],
    },
  });

  const createUserMutation = useMutation({
    mutationFn: createUser,
    onSuccess: async () => {
      reset();
      await queryClient.invalidateQueries({ queryKey: ['users'] });
    },
  });

  function onSubmit(values: UserFormValues) {
    createUserMutation.mutate(values);
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
                  <TableCell>Status</TableCell>
                  <TableCell>Roles</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {usersQuery.data?.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell>{user.name}</TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>{user.jobTitle ?? '-'}</TableCell>
                    <TableCell>
                      <Chip color={user.status === 'ACTIVE' ? 'success' : 'default'} label={user.status} size="small" />
                    </TableCell>
                    <TableCell>
                      <Stack direction="row" flexWrap="wrap" gap={0.75}>
                        {user.roles.map((role) => (
                          <Chip key={role} label={role} size="small" variant="outlined" />
                        ))}
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))}
                {!usersQuery.isLoading && usersQuery.data?.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={5}>Nenhum usuario encontrado.</TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </Paper>
        </Grid2>

        {canManageUsers && (
          <Grid2 size={{ xs: 12, lg: 4 }}>
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
                        label={role}
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
          </Grid2>
        )}
      </Grid2>
    </Box>
  );
}
