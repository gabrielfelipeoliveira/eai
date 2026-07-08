import SaveIcon from '@mui/icons-material/Save';
import { Alert, Box, Button, Grid2, LinearProgress, MenuItem, Paper, Stack, Switch, TextField, Typography } from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { useMetadata } from '../hooks/useMetadata';
import { getDistributionConfig, updateDistributionConfig } from '../services/distributionService';
import type { LeadDistributionMode } from '../types/distribution';

const modes: LeadDistributionMode[] = ['MANUAL', 'ROUND_ROBIN', 'LEAST_BUSY'];

export function DistributionSettingsPage() {
  const queryClient = useQueryClient();
  const metadata = useMetadata();
  const [mode, setMode] = useState<LeadDistributionMode>('MANUAL');
  const [active, setActive] = useState(false);
  const [minutesToAssign, setMinutesToAssign] = useState(15);
  const [minutesToFirstContact, setMinutesToFirstContact] = useState(30);
  const [slaActive, setSlaActive] = useState(false);

  const configQuery = useQuery({
    queryKey: ['distribution-config'],
    queryFn: getDistributionConfig,
  });

  useEffect(() => {
    if (configQuery.data) {
      setMode(configQuery.data.mode);
      setActive(configQuery.data.active);
      setMinutesToAssign(configQuery.data.minutesToAssign);
      setMinutesToFirstContact(configQuery.data.minutesToFirstContact);
      setSlaActive(configQuery.data.slaActive);
    }
  }, [configQuery.data]);

  const updateMutation = useMutation({
    mutationFn: () =>
      updateDistributionConfig({
        companyId: configQuery.data?.companyId,
        storeId: configQuery.data?.storeId,
        mode,
        active,
        minutesToAssign,
        minutesToFirstContact,
        slaActive,
      }),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['distribution-config'] }),
        queryClient.invalidateQueries({ queryKey: ['leads'] }),
        queryClient.invalidateQueries({ queryKey: ['lead-dashboard'] }),
      ]);
    },
  });

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Box>
        <Typography component="h2" variant="h4" fontWeight={800}>
          Configuracoes
        </Typography>
        <Typography color="text.secondary">Distribuicao de leads e prazos de SLA da loja atual.</Typography>
      </Box>

      {configQuery.isLoading && <LinearProgress />}
      {configQuery.isError && <Alert severity="error">Nao foi possivel carregar a configuracao.</Alert>}
      {updateMutation.isSuccess && <Alert severity="success">Configuracao salva.</Alert>}
      {updateMutation.isError && <Alert severity="error">Nao foi possivel salvar a configuracao.</Alert>}

      <Paper variant="outlined" sx={{ borderRadius: 1, p: 3 }}>
        <Grid2 container spacing={2.5}>
          <Grid2 size={{ xs: 12, md: 6 }}>
            <Typography variant="h6" fontWeight={800} sx={{ mb: 2 }}>
              Distribuicao
            </Typography>
            <Stack spacing={2}>
              <TextField label="Modo" onChange={(event) => setMode(event.target.value as LeadDistributionMode)} select value={mode}>
                {modes.map((item) => (
                  <MenuItem key={item} value={item}>
                    {metadata.label('leadDistributionModes', item)}
                  </MenuItem>
                ))}
              </TextField>
              <Stack alignItems="center" direction="row" justifyContent="space-between">
                <Typography>Ativar distribuicao automatica</Typography>
                <Switch checked={active} onChange={(event) => setActive(event.target.checked)} />
              </Stack>
            </Stack>
          </Grid2>

          <Grid2 size={{ xs: 12, md: 6 }}>
            <Typography variant="h6" fontWeight={800} sx={{ mb: 2 }}>
              SLA
            </Typography>
            <Stack spacing={2}>
              <TextField
                label="Minutos para atribuir"
                onChange={(event) => setMinutesToAssign(Math.max(1, Number(event.target.value)))}
                type="number"
                value={minutesToAssign}
              />
              <TextField
                label="Minutos para primeiro contato"
                onChange={(event) => setMinutesToFirstContact(Math.max(1, Number(event.target.value)))}
                type="number"
                value={minutesToFirstContact}
              />
              <Stack alignItems="center" direction="row" justifyContent="space-between">
                <Typography>Ativar controle de SLA</Typography>
                <Switch checked={slaActive} onChange={(event) => setSlaActive(event.target.checked)} />
              </Stack>
            </Stack>
          </Grid2>
        </Grid2>

        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 3 }}>
          <Button disabled={updateMutation.isPending || !configQuery.data} onClick={() => updateMutation.mutate()} startIcon={<SaveIcon />} variant="contained">
            Salvar
          </Button>
        </Box>
      </Paper>
    </Box>
  );
}
