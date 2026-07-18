import { zodResolver } from '@hookform/resolvers/zod';
import AddBusinessIcon from '@mui/icons-material/AddBusiness';
import EditIcon from '@mui/icons-material/Edit';
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
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { useMetadata } from '../hooks/useMetadata';
import { apiErrorMessage } from '../services/api';
import { createCompany, listCompanies, updateCompany } from '../services/companyService';
import type { Company } from '../types/tenant';

const companySchema = z.object({
  name: z.string().min(1, 'Informe o nome').max(160),
  status: z.enum(['ACTIVE', 'INACTIVE']),
});

type CompanyFormValues = z.infer<typeof companySchema>;

const emptyValues: CompanyFormValues = {
  name: '',
  status: 'ACTIVE',
};

export function CompaniesPage() {
  const [editingCompany, setEditingCompany] = useState<Company | null>(null);
  const queryClient = useQueryClient();
  const metadata = useMetadata();

  const companiesQuery = useQuery({
    queryKey: ['companies'],
    queryFn: listCompanies,
  });

  const {
    formState: { errors },
    handleSubmit,
    register,
    reset,
  } = useForm<CompanyFormValues>({
    resolver: zodResolver(companySchema),
    defaultValues: emptyValues,
  });

  useEffect(() => {
    if (!editingCompany) {
      reset(emptyValues);
      return;
    }
    reset({
      name: editingCompany.name,
      status: editingCompany.status,
    });
  }, [editingCompany, reset]);

  const saveCompanyMutation = useMutation({
    mutationFn: (values: CompanyFormValues) => {
      return editingCompany ? updateCompany(editingCompany.id, values) : createCompany(values);
    },
    onSuccess: async () => {
      setEditingCompany(null);
      reset(emptyValues);
      await queryClient.invalidateQueries({ queryKey: ['companies'] });
    },
  });

  function onSubmit(values: CompanyFormValues) {
    saveCompanyMutation.mutate(values);
  }

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Box>
        <Typography component="h2" variant="h4" fontWeight={800}>
          Empresas
        </Typography>
        <Typography color="text.secondary">Cadastro das empresas que utilizam o EAI.</Typography>
      </Box>

      <Grid2 container spacing={3}>
        <Grid2 size={{ xs: 12, lg: 8 }}>
          <Paper variant="outlined" sx={{ borderRadius: 1, overflow: 'hidden' }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Nome</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Acoes</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {companiesQuery.data?.map((company) => (
                  <TableRow key={company.id}>
                    <TableCell>{company.name}</TableCell>
                    <TableCell>
                      <Chip color={metadata.color('tenantStatuses', company.status)} label={metadata.label('tenantStatuses', company.status)} size="small" />
                    </TableCell>
                    <TableCell align="right">
                      <IconButton aria-label="Editar empresa" onClick={() => setEditingCompany(company)}>
                        <EditIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
                {!companiesQuery.isLoading && companiesQuery.data?.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={3}>Nenhuma empresa encontrada.</TableCell>
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
                {editingCompany ? 'Editar empresa' : 'Nova empresa'}
              </Typography>
            </Box>

            {saveCompanyMutation.isError && (
              <Alert severity="error">{apiErrorMessage(saveCompanyMutation.error) ?? 'Nao foi possivel salvar a empresa.'}</Alert>
            )}

            <TextField label="Nome" error={Boolean(errors.name)} helperText={errors.name?.message} {...register('name')} />
            <TextField select label="Status" error={Boolean(errors.status)} helperText={errors.status?.message} {...register('status')}>
              {metadata.options('tenantStatuses').map((status) => (
                <MenuItem key={status.code} value={status.code}>
                  {status.label}
                </MenuItem>
              ))}
            </TextField>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button disabled={saveCompanyMutation.isPending} type="submit" variant="contained">
                Salvar
              </Button>
              {editingCompany && (
                <Button onClick={() => setEditingCompany(null)} type="button" variant="outlined">
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
