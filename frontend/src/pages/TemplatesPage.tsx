import { zodResolver } from '@hookform/resolvers/zod';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Grid2,
  IconButton,
  MenuItem,
  Paper,
  Stack,
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
import { useAuth } from '../hooks/useAuth';
import { useMetadata } from '../hooks/useMetadata';
import { listCompanies } from '../services/companyService';
import { listStores } from '../services/storeService';
import { createTemplate, deleteTemplate, listTemplates, updateTemplate } from '../services/templateService';
import type { MessageTemplate, MessageTemplateType } from '../types/message';

const templateTypes: MessageTemplateType[] = ['FIRST_CONTACT', 'FOLLOW_UP', 'VISIT_INVITE', 'PROPOSAL', 'NO_RESPONSE', 'SOLD', 'LOST'];

const templateSchema = z.object({
  companyId: z.string().min(1, 'Selecione a empresa'),
  storeId: z.string().min(1, 'Selecione a loja'),
  name: z.string().min(1, 'Informe o nome').max(120),
  type: z.enum(['FIRST_CONTACT', 'FOLLOW_UP', 'VISIT_INVITE', 'PROPOSAL', 'NO_RESPONSE', 'SOLD', 'LOST']),
  content: z.string().min(1, 'Informe a mensagem'),
  active: z.boolean(),
});

type TemplateFormValues = z.infer<typeof templateSchema>;

export function TemplatesPage() {
  const { hasAnyRole, user } = useAuth();
  const queryClient = useQueryClient();
  const metadata = useMetadata();
  const [editingTemplate, setEditingTemplate] = useState<MessageTemplate | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const isAdmin = hasAnyRole(['ADMIN']);

  const defaultValues = useMemo<TemplateFormValues>(
    () => ({
      companyId: user?.companyId ?? '',
      storeId: user?.storeId ?? '',
      name: '',
      type: 'FIRST_CONTACT',
      content: '',
      active: true,
    }),
    [user?.companyId, user?.storeId],
  );

  const templatesQuery = useQuery({ queryKey: ['templates'], queryFn: listTemplates });
  const storesQuery = useQuery({ queryKey: ['stores'], queryFn: () => listStores() });
  const companiesQuery = useQuery({ queryKey: ['companies'], queryFn: listCompanies, enabled: isAdmin });

  const {
    control,
    formState: { errors },
    handleSubmit,
    register,
    reset,
    setValue,
  } = useForm<TemplateFormValues>({
    resolver: zodResolver(templateSchema),
    defaultValues,
  });

  useEffect(() => {
    reset(defaultValues);
  }, [defaultValues, reset]);

  useEffect(() => {
    if (storesQuery.data?.length && !defaultValues.storeId) {
      setValue('storeId', storesQuery.data[0].id);
    }
  }, [defaultValues.storeId, setValue, storesQuery.data]);

  const saveMutation = useMutation({
    mutationFn: (values: TemplateFormValues) => (editingTemplate ? updateTemplate(editingTemplate.id, values) : createTemplate(values)),
    onSuccess: async () => {
      setDialogOpen(false);
      setEditingTemplate(null);
      reset(defaultValues);
      await queryClient.invalidateQueries({ queryKey: ['templates'] });
      await queryClient.invalidateQueries({ queryKey: ['active-templates'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => deleteTemplate(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['templates'] });
      await queryClient.invalidateQueries({ queryKey: ['active-templates'] });
    },
  });

  function openCreateDialog() {
    setEditingTemplate(null);
    reset(defaultValues);
    setDialogOpen(true);
  }

  function openEditDialog(template: MessageTemplate) {
    setEditingTemplate(template);
    reset({
      companyId: template.companyId,
      storeId: template.storeId,
      name: template.name,
      type: template.type,
      content: template.content,
      active: template.active,
    });
    setDialogOpen(true);
  }

  function storeName(storeId: string) {
    return storesQuery.data?.find((store) => store.id === storeId)?.name ?? storeId;
  }

  function onSubmit(values: TemplateFormValues) {
    saveMutation.mutate(values);
  }

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 2 }}>
        <Box>
          <Typography component="h2" variant="h4" fontWeight={800}>
            Templates
          </Typography>
          <Typography color="text.secondary">Mensagens padronizadas para contatos comerciais por WhatsApp.</Typography>
        </Box>
        <Button onClick={openCreateDialog} startIcon={<AddIcon />} variant="contained">
          Novo template
        </Button>
      </Box>

      <Paper variant="outlined" sx={{ borderRadius: 1, overflow: 'hidden' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Nome</TableCell>
              <TableCell>Tipo</TableCell>
              <TableCell>Loja</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Mensagem</TableCell>
              <TableCell align="right">Acoes</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {templatesQuery.data?.map((template) => (
              <TableRow hover key={template.id}>
                <TableCell>
                  <Typography fontWeight={700}>{template.name}</Typography>
                </TableCell>
                <TableCell>{metadata.label('messageTemplateTypes', template.type)}</TableCell>
                <TableCell>{storeName(template.storeId)}</TableCell>
                <TableCell>{template.active ? 'Ativo' : 'Inativo'}</TableCell>
                <TableCell sx={{ maxWidth: 420 }}>
                  <Typography noWrap variant="body2">
                    {template.content}
                  </Typography>
                </TableCell>
                <TableCell align="right">
                  <Stack direction="row" justifyContent="flex-end" spacing={1}>
                    <Tooltip title="Editar">
                      <IconButton aria-label="Editar" onClick={() => openEditDialog(template)} size="small">
                        <EditIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Excluir">
                      <IconButton aria-label="Excluir" onClick={() => deleteMutation.mutate(template.id)} size="small">
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </Stack>
                </TableCell>
              </TableRow>
            ))}
            {!templatesQuery.isLoading && templatesQuery.data?.length === 0 && (
              <TableRow>
                <TableCell colSpan={6}>Nenhum template encontrado.</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Paper>

      <Dialog fullWidth maxWidth="md" onClose={() => setDialogOpen(false)} open={dialogOpen}>
        <Box component="form" onSubmit={handleSubmit(onSubmit)}>
          <DialogTitle>{editingTemplate ? 'Editar template' : 'Novo template'}</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ pt: 1 }}>
              {saveMutation.isError && <Alert severity="error">Nao foi possivel salvar o template.</Alert>}
              <Grid2 container spacing={2}>
                <Grid2 size={{ xs: 12, md: 6 }}>
                  <TextField fullWidth label="Nome" error={Boolean(errors.name)} helperText={errors.name?.message} {...register('name')} />
                </Grid2>
                <Grid2 size={{ xs: 12, md: 6 }}>
                  <TextField fullWidth select label="Tipo" error={Boolean(errors.type)} helperText={errors.type?.message} {...register('type')}>
                    {templateTypes.map((type) => (
                      <MenuItem key={type} value={type}>
                        {metadata.label('messageTemplateTypes', type)}
                      </MenuItem>
                    ))}
                  </TextField>
                </Grid2>
                {isAdmin && (
                  <Grid2 size={{ xs: 12, md: 6 }}>
                    <TextField fullWidth select label="Empresa" error={Boolean(errors.companyId)} helperText={errors.companyId?.message} {...register('companyId')}>
                      {companiesQuery.data?.map((company) => (
                        <MenuItem key={company.id} value={company.id}>
                          {company.name}
                        </MenuItem>
                      ))}
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
                    {storesQuery.data?.map((store) => (
                      <MenuItem key={store.id} value={store.id}>
                        {store.name}
                      </MenuItem>
                    ))}
                  </TextField>
                </Grid2>
              </Grid2>
              <TextField fullWidth label="Conteudo" minRows={5} multiline error={Boolean(errors.content)} helperText={errors.content?.message} {...register('content')} />
              <Controller
                control={control}
                name="active"
                render={({ field }) => (
                  <FormControlLabel control={<Checkbox checked={field.value} onChange={(event) => field.onChange(event.target.checked)} />} label="Ativo" />
                )}
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>Cancelar</Button>
            <Button disabled={saveMutation.isPending} type="submit" variant="contained">
              Salvar
            </Button>
          </DialogActions>
        </Box>
      </Dialog>
    </Box>
  );
}
