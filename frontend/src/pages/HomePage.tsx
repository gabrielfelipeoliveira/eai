import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import {
  Alert,
  Box,
  Divider,
  FormControl,
  Grid2,
  InputLabel,
  LinearProgress,
  MenuItem,
  Paper,
  Select,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { listCompanies } from '../services/companyService';
import {
  getDashboardSummary,
  getLeadsBySeller,
  getLeadsBySource,
  getLeadsByStatus,
  getSalesByPeriod,
} from '../services/dashboardService';
import { listStores } from '../services/storeService';
import { useAuth } from '../hooks/useAuth';
import { useMetadata } from '../hooks/useMetadata';
import type { DashboardFilters } from '../types/dashboard';

interface HomePageProps {
  title?: string;
}

const chartColors = ['#2563eb', '#15803d', '#f59e0b', '#7c3aed', '#0891b2', '#dc2626', '#475569', '#0f766e'];

function today() {
  return new Date().toISOString().slice(0, 10);
}

function thirtyDaysAgo() {
  const date = new Date();
  date.setDate(date.getDate() - 30);
  return date.toISOString().slice(0, 10);
}

function formatMinutes(value: number) {
  if (value < 60) {
    return `${Math.round(value)} min`;
  }
  return `${(value / 60).toFixed(1)} h`;
}

export function HomePage({ title = 'Dashboard' }: HomePageProps) {
  const { hasAnyRole, user } = useAuth();
  const metadata = useMetadata();
  const canFilterTenant = hasAnyRole(['ADMIN', 'MANAGER']);
  const isAdmin = hasAnyRole(['ADMIN']);
  const [filters, setFilters] = useState<DashboardFilters>({
    companyId: isAdmin ? undefined : user?.companyId ?? undefined,
    storeId: isAdmin || user?.roles.includes('MANAGER') ? undefined : user?.storeId ?? undefined,
    dateFrom: thirtyDaysAgo(),
    dateTo: today(),
  });

  const queryFilters = useMemo(() => filters, [filters]);

  const companiesQuery = useQuery({
    queryKey: ['dashboard-companies'],
    queryFn: listCompanies,
    enabled: isAdmin,
  });

  const storesQuery = useQuery({
    queryKey: ['dashboard-stores', filters.companyId],
    queryFn: () => listStores(filters.companyId),
    enabled: canFilterTenant,
  });

  const summaryQuery = useQuery({
    queryKey: ['dashboard-summary', queryFilters],
    queryFn: () => getDashboardSummary(queryFilters),
  });

  const sourceQuery = useQuery({
    queryKey: ['dashboard-leads-by-source', queryFilters],
    queryFn: () => getLeadsBySource(queryFilters),
  });

  const statusQuery = useQuery({
    queryKey: ['dashboard-leads-by-status', queryFilters],
    queryFn: () => getLeadsByStatus(queryFilters),
  });

  const sellerQuery = useQuery({
    queryKey: ['dashboard-leads-by-seller', queryFilters],
    queryFn: () => getLeadsBySeller(queryFilters),
  });

  const salesQuery = useQuery({
    queryKey: ['dashboard-sales-by-period', queryFilters],
    queryFn: () => getSalesByPeriod(queryFilters),
  });

  const isLoading =
    summaryQuery.isLoading || sourceQuery.isLoading || statusQuery.isLoading || sellerQuery.isLoading || salesQuery.isLoading;

  const summary = summaryQuery.data;
  const sourceData = (sourceQuery.data ?? []).map((item) => ({ ...item, label: metadata.label('leadSources', item.label) }));
  const statusData = (statusQuery.data ?? []).map((item) => ({ ...item, label: metadata.label('leadStatuses', item.label) }));
  const salesData = salesQuery.data ?? [];
  const sellerData = sellerQuery.data ?? [];

  function updateFilter(name: keyof DashboardFilters, value: string) {
    setFilters((current) => ({
      ...current,
      [name]: value || undefined,
      ...(name === 'companyId' ? { storeId: undefined } : {}),
    }));
  }

  return (
    <Box sx={{ display: 'grid', gap: 2.5 }}>
      <Stack direction={{ xs: 'column', lg: 'row' }} justifyContent="space-between" spacing={2.5}>
        <Box sx={{ minWidth: 0 }}>
          <Typography component="h2" sx={{ overflowWrap: 'anywhere' }} variant="h4">
            {title}
          </Typography>
          <Typography color="text.secondary" sx={{ maxWidth: 560 }} variant="body2">
            Indicadores comerciais, conversao, origem de leads e SLA de atendimento.
          </Typography>
        </Box>

        <Paper
          aria-label="Filtros do dashboard"
          variant="outlined"
          sx={{ alignSelf: { lg: 'flex-start' }, borderRadius: 1, p: 1.5, width: { xs: '100%', lg: 700 } }}
        >
          <Stack direction={{ xs: 'column', md: 'row' }} spacing={1.25}>
            {isAdmin && (
              <FormControl size="small" fullWidth>
                <InputLabel>Empresa</InputLabel>
                <Select label="Empresa" value={filters.companyId ?? ''} onChange={(event) => updateFilter('companyId', event.target.value)}>
                  <MenuItem value="">Todas</MenuItem>
                  {(companiesQuery.data ?? []).map((company) => (
                    <MenuItem key={company.id} value={company.id}>
                      {company.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            )}
            {canFilterTenant && (
              <FormControl size="small" fullWidth>
                <InputLabel>Loja</InputLabel>
                <Select label="Loja" value={filters.storeId ?? ''} onChange={(event) => updateFilter('storeId', event.target.value)}>
                  <MenuItem value="">Todas</MenuItem>
                  {(storesQuery.data ?? []).map((store) => (
                    <MenuItem key={store.id} value={store.id}>
                      {store.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            )}
            <TextField
              fullWidth
              label="De"
              size="small"
              type="date"
              value={filters.dateFrom ?? ''}
              onChange={(event) => updateFilter('dateFrom', event.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <TextField
              fullWidth
              label="Ate"
              size="small"
              type="date"
              value={filters.dateTo ?? ''}
              onChange={(event) => updateFilter('dateTo', event.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
            />
          </Stack>
        </Paper>
      </Stack>

      {isLoading && <LinearProgress />}

      <Grid2 container spacing={2}>
        {[
          ['Leads hoje', summary?.totalLeadsToday ?? 0],
          ['Leads no mes', summary?.totalLeadsThisMonth ?? 0],
          ['Disponiveis', summary?.availableLeads ?? 0],
          ['Atribuidos', summary?.assignedLeads ?? 0],
          ['Vendidos', summary?.soldLeads ?? 0],
          ['Perdidos', summary?.lostLeads ?? 0],
          ['Conversao', `${(summary?.conversionRate ?? 0).toFixed(1)}%`],
          ['Primeira resposta', formatMinutes(summary?.averageFirstResponseTimeMinutes ?? 0)],
        ].map(([label, value]) => (
          <Grid2 key={label} size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper variant="outlined" sx={{ borderRadius: 1, minHeight: 104, p: 2 }}>
              <Typography color="text.secondary" fontWeight={700} sx={{ textTransform: 'uppercase' }} variant="caption">
                {label}
              </Typography>
              <Typography sx={{ mt: 1, overflowWrap: 'anywhere' }} variant="h5">
                {value}
              </Typography>
            </Paper>
          </Grid2>
        ))}
      </Grid2>

      <Grid2 container spacing={2}>
        <Grid2 size={{ xs: 12, md: 8 }}>
          <Alert
            severity={(summary?.overdueLeads ?? 0) > 0 ? 'warning' : 'success'}
            icon={<WarningAmberIcon />}
            variant="outlined"
            sx={{ alignItems: 'center', borderRadius: 1 }}
          >
            {summary?.overdueLeads ?? 0} leads fora do SLA no periodo filtrado.
          </Alert>
        </Grid2>
        <Grid2 size={{ xs: 12, md: 4 }}>
          <Alert severity="info" icon={<TrendingUpIcon />} variant="outlined" sx={{ alignItems: 'center', borderRadius: 1 }}>
            Conversao atual em {(summary?.conversionRate ?? 0).toFixed(1)}%.
          </Alert>
        </Grid2>
      </Grid2>

      <Grid2 container spacing={2}>
        <Grid2 size={{ xs: 12, lg: 6 }}>
          <Paper variant="outlined" sx={{ borderRadius: 1, height: 360, overflow: 'hidden' }}>
            <Box sx={{ px: 2.5, py: 2 }}>
              <Typography variant="h6">Leads por origem</Typography>
            </Box>
            <Divider />
            <Box sx={{ height: 280, p: 2 }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={sourceData} dataKey="value" nameKey="label" outerRadius={100} label>
                    {sourceData.map((entry, index) => (
                      <Cell key={entry.label} fill={chartColors[index % chartColors.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Grid2>

        <Grid2 size={{ xs: 12, lg: 6 }}>
          <Paper variant="outlined" sx={{ borderRadius: 1, height: 360, overflow: 'hidden' }}>
            <Box sx={{ px: 2.5, py: 2 }}>
              <Typography variant="h6">Leads por status</Typography>
            </Box>
            <Divider />
            <Box sx={{ height: 280, p: 2 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={statusData}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="value" fill="#2563eb" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Grid2>
      </Grid2>

      <Grid2 container spacing={2}>
        <Grid2 size={{ xs: 12, lg: 7 }}>
          <Paper variant="outlined" sx={{ borderRadius: 1, height: 360, overflow: 'hidden' }}>
            <Box sx={{ px: 2.5, py: 2 }}>
              <Typography variant="h6">Vendas por periodo</Typography>
            </Box>
            <Divider />
            <Box sx={{ height: 280, p: 2 }}>
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={salesData}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="period" tick={{ fontSize: 12 }} />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Line type="monotone" dataKey="soldLeads" stroke="#15803d" strokeWidth={3} dot={{ r: 3 }} />
                </LineChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Grid2>

        <Grid2 size={{ xs: 12, lg: 5 }}>
          <Paper variant="outlined" sx={{ borderRadius: 1, minHeight: 360, overflow: 'hidden' }}>
            <Box sx={{ px: 2.5, py: 2 }}>
              <Typography variant="h6">Ranking de vendedores</Typography>
            </Box>
            <Divider />
            <Box sx={{ display: 'grid', p: 2 }}>
              {sellerData.map((item, index) => (
                <Box
                  key={item.sellerId}
                  sx={{
                    alignItems: 'center',
                    borderBottom: index === sellerData.length - 1 ? 0 : 1,
                    borderColor: 'divider',
                    display: 'grid',
                    gap: 1.5,
                    gridTemplateColumns: '32px 1fr auto',
                    py: 1.25,
                  }}
                >
                  <Typography color="text.secondary" fontWeight={800}>
                    {index + 1}
                  </Typography>
                  <Box sx={{ minWidth: 0 }}>
                    <Typography fontWeight={700} noWrap>
                      {item.sellerName}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {item.leadCount} leads - {item.soldLeads} vendas
                    </Typography>
                  </Box>
                  <Typography fontWeight={800}>{item.conversionRate.toFixed(1)}%</Typography>
                </Box>
              ))}
              {!isLoading && sellerData.length === 0 && (
                <Typography color="text.secondary">Nenhum vendedor com leads no periodo.</Typography>
              )}
            </Box>
          </Paper>
        </Grid2>
      </Grid2>
    </Box>
  );
}
