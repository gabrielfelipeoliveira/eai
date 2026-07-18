import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import {
  Alert,
  Box,
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

const chartColors = ['#1976d2', '#2e7d32', '#ed6c02', '#9c27b0', '#00838f', '#d32f2f', '#455a64', '#7b1fa2'];

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
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Stack direction={{ xs: 'column', lg: 'row' }} justifyContent="space-between" spacing={2}>
        <Box>
          <Typography component="h2" variant="h4" fontWeight={800}>
            {title}
          </Typography>
          <Typography color="text.secondary" variant="body1">
            Indicadores comerciais, conversao, origem de leads e SLA de atendimento.
          </Typography>
        </Box>

        <Stack direction={{ xs: 'column', md: 'row' }} spacing={1.5} sx={{ minWidth: { lg: 680 } }}>
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
            label="De"
            size="small"
            type="date"
            value={filters.dateFrom ?? ''}
            onChange={(event) => updateFilter('dateFrom', event.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
          />
          <TextField
            label="Ate"
            size="small"
            type="date"
            value={filters.dateTo ?? ''}
            onChange={(event) => updateFilter('dateTo', event.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
          />
        </Stack>
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
            <Paper variant="outlined" sx={{ p: 2, borderRadius: 1, minHeight: 104 }}>
              <Typography variant="body2" color="text.secondary">
                {label}
              </Typography>
              <Typography variant="h5" fontWeight={800} sx={{ mt: 1 }}>
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
            sx={{ borderRadius: 1, alignItems: 'center' }}
          >
            {summary?.overdueLeads ?? 0} leads fora do SLA no periodo filtrado.
          </Alert>
        </Grid2>
        <Grid2 size={{ xs: 12, md: 4 }}>
          <Alert severity="info" icon={<TrendingUpIcon />} sx={{ borderRadius: 1, alignItems: 'center' }}>
            Conversao atual em {(summary?.conversionRate ?? 0).toFixed(1)}%.
          </Alert>
        </Grid2>
      </Grid2>

      <Grid2 container spacing={2}>
        <Grid2 size={{ xs: 12, lg: 6 }}>
          <Paper variant="outlined" sx={{ p: 3, borderRadius: 1, height: 360 }}>
            <Typography variant="h6" fontWeight={700}>
              Leads por origem
            </Typography>
            <Box sx={{ height: 280, mt: 2 }}>
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
          <Paper variant="outlined" sx={{ p: 3, borderRadius: 1, height: 360 }}>
            <Typography variant="h6" fontWeight={700}>
              Leads por status
            </Typography>
            <Box sx={{ height: 280, mt: 2 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={statusData}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="value" fill="#1976d2" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Grid2>
      </Grid2>

      <Grid2 container spacing={2}>
        <Grid2 size={{ xs: 12, lg: 7 }}>
          <Paper variant="outlined" sx={{ p: 3, borderRadius: 1, height: 360 }}>
            <Typography variant="h6" fontWeight={700}>
              Vendas por periodo
            </Typography>
            <Box sx={{ height: 280, mt: 2 }}>
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={salesData}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="period" tick={{ fontSize: 12 }} />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Line type="monotone" dataKey="soldLeads" stroke="#2e7d32" strokeWidth={3} dot={{ r: 3 }} />
                </LineChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        </Grid2>

        <Grid2 size={{ xs: 12, lg: 5 }}>
          <Paper variant="outlined" sx={{ p: 3, borderRadius: 1, minHeight: 360 }}>
            <Typography variant="h6" fontWeight={700}>
              Ranking de vendedores
            </Typography>
            <Box sx={{ display: 'grid', gap: 1.5, mt: 2 }}>
              {sellerData.map((item, index) => (
                <Box
                  key={item.sellerId}
                  sx={{ display: 'grid', gridTemplateColumns: '32px 1fr auto', alignItems: 'center', gap: 1.5 }}
                >
                  <Typography fontWeight={800}>{index + 1}</Typography>
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
