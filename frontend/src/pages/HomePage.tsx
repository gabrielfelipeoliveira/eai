import { Box, Grid2, LinearProgress, Paper, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { getLeadDashboardMetrics } from '../services/distributionService';

interface HomePageProps {
  title?: string;
}

export function HomePage({ title = 'Dashboard' }: HomePageProps) {
  const metricsQuery = useQuery({
    queryKey: ['lead-dashboard'],
    queryFn: getLeadDashboardMetrics,
  });

  const metrics = metricsQuery.data;

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Box>
        <Typography component="h2" variant="h4" fontWeight={800}>
          {title}
        </Typography>
        <Typography color="text.secondary" variant="body1">
          Visao operacional para leads, atendimento e desempenho comercial.
        </Typography>
      </Box>

      {metricsQuery.isLoading && <LinearProgress />}

      <Grid2 container spacing={2}>
        {[
          ['Leads sem responsavel', metrics?.unassignedLeads ?? 0],
          ['Leads atrasados', metrics?.overdueLeads ?? 0],
          ['Vendedores com carteira', metrics?.leadsBySeller.filter((item) => item.leadCount > 0).length ?? 0],
          ['Leads distribuidos', metrics?.leadsBySeller.reduce((total, item) => total + item.leadCount, 0) ?? 0],
        ].map(([label, value]) => (
          <Grid2 key={label} size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper variant="outlined" sx={{ p: 2, borderRadius: 1 }}>
              <Typography variant="body2" color="text.secondary">
                {label}
              </Typography>
              <Typography variant="h5" fontWeight={800}>
                {value}
              </Typography>
            </Paper>
          </Grid2>
        ))}
      </Grid2>

      <Paper variant="outlined" sx={{ p: 3, borderRadius: 1 }}>
        <Typography variant="h6" fontWeight={700}>
          Leads por vendedor
        </Typography>
        <Box sx={{ display: 'grid', gap: 1.5, mt: 2 }}>
          {metrics?.leadsBySeller.map((item) => (
            <Box key={item.sellerId} sx={{ display: 'grid', gridTemplateColumns: '1fr auto', alignItems: 'center', gap: 2 }}>
              <Typography>{item.sellerName}</Typography>
              <Typography fontWeight={800}>{item.leadCount}</Typography>
            </Box>
          ))}
          {!metricsQuery.isLoading && metrics?.leadsBySeller.length === 0 && (
            <Typography color="text.secondary">Nenhum vendedor ativo encontrado para a loja.</Typography>
          )}
        </Box>
      </Paper>
    </Box>
  );
}
