import DownloadIcon from '@mui/icons-material/Download';
import {
  Alert,
  Box,
  Button,
  Grid2,
  LinearProgress,
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
import { useQuery } from '@tanstack/react-query';
import type React from 'react';
import { useMemo, useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { listCompanies } from '../services/companyService';
import {
  downloadReportCsv,
  getReportLeads,
  getReportLost,
  getReportSales,
  getReportSellers,
  getReportSla,
  getReportSources,
} from '../services/reportService';
import { listStores } from '../services/storeService';
import { listUsers } from '../services/userService';
import type { LeadSource } from '../types/lead';
import type { ReportFilters } from '../types/report';

const sources: LeadSource[] = ['MANUAL', 'EMAIL', 'WEBSITE', 'FACEBOOK', 'INSTAGRAM', 'WEBMOTORS', 'ICARROS', 'OLX', 'API'];

function today() {
  return new Date().toISOString().slice(0, 10);
}

function thirtyDaysAgo() {
  const date = new Date();
  date.setDate(date.getDate() - 30);
  return date.toISOString().slice(0, 10);
}

function money(value: number | null | undefined) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value ?? 0);
}

function minutes(value: number) {
  return value < 60 ? `${Math.round(value)} min` : `${(value / 60).toFixed(1)} h`;
}

function date(value: string | null | undefined) {
  return value ? new Date(value).toLocaleDateString('pt-BR') : '-';
}

export function ReportsPage() {
  const { hasAnyRole, user } = useAuth();
  const isAdmin = hasAnyRole(['ADMIN']);
  const canFilterTenant = hasAnyRole(['ADMIN', 'MANAGER', 'AUDITOR']);
  const canFilterSellers = hasAnyRole(['ADMIN', 'MANAGER', 'AUDITOR']);
  const [filters, setFilters] = useState<ReportFilters>({
    companyId: isAdmin ? undefined : user?.companyId,
    storeId: isAdmin || user?.roles.includes('MANAGER') || user?.roles.includes('AUDITOR') ? undefined : user?.storeId,
    sellerId: user?.roles.includes('SELLER') ? user.id : undefined,
    dateFrom: thirtyDaysAgo(),
    dateTo: today(),
  });

  const queryFilters = useMemo(() => filters, [filters]);

  const companiesQuery = useQuery({ queryKey: ['report-companies'], queryFn: listCompanies, enabled: isAdmin });
  const storesQuery = useQuery({
    queryKey: ['report-stores', filters.companyId],
    queryFn: () => listStores(filters.companyId),
    enabled: canFilterTenant,
  });
  const usersQuery = useQuery({ queryKey: ['report-users'], queryFn: listUsers, enabled: canFilterSellers });
  const leadsQuery = useQuery({ queryKey: ['report-leads', queryFilters], queryFn: () => getReportLeads(queryFilters) });
  const sellersQuery = useQuery({ queryKey: ['report-sellers', queryFilters], queryFn: () => getReportSellers(queryFilters) });
  const sourcesQuery = useQuery({ queryKey: ['report-sources', queryFilters], queryFn: () => getReportSources(queryFilters) });
  const lostQuery = useQuery({ queryKey: ['report-lost', queryFilters], queryFn: () => getReportLost(queryFilters) });
  const salesQuery = useQuery({ queryKey: ['report-sales', queryFilters], queryFn: () => getReportSales(queryFilters) });
  const slaQuery = useQuery({ queryKey: ['report-sla', queryFilters], queryFn: () => getReportSla(queryFilters) });

  const loading =
    leadsQuery.isLoading || sellersQuery.isLoading || sourcesQuery.isLoading || lostQuery.isLoading || salesQuery.isLoading || slaQuery.isLoading;
  const saleTotal = (salesQuery.data ?? []).reduce((total, item) => total + (item.saleValue ?? 0), 0);

  function updateFilter(name: keyof ReportFilters, value: string) {
    setFilters((current) => ({
      ...current,
      [name]: value || undefined,
      ...(name === 'companyId' ? { storeId: undefined, sellerId: undefined } : {}),
    }));
  }

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Stack direction={{ xs: 'column', lg: 'row' }} justifyContent="space-between" spacing={2}>
        <Box>
          <Typography component="h2" variant="h4" fontWeight={800}>
            Relatorios
          </Typography>
          <Typography color="text.secondary">Indicadores gerenciais por periodo, loja, vendedor e origem.</Typography>
        </Box>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
          <Button startIcon={<DownloadIcon />} variant="outlined" onClick={() => downloadReportCsv('/reports/leads/export.csv', filters, 'reports-leads.csv')}>
            Leads CSV
          </Button>
          <Button startIcon={<DownloadIcon />} variant="outlined" onClick={() => downloadReportCsv('/reports/sellers/export.csv', filters, 'reports-sellers.csv')}>
            Vendedores CSV
          </Button>
        </Stack>
      </Stack>

      <Paper variant="outlined" sx={{ borderRadius: 1, p: 2 }}>
        <Grid2 container spacing={2}>
          {isAdmin && (
            <Grid2 size={{ xs: 12, md: 2 }}>
              <TextField select fullWidth size="small" label="Empresa" value={filters.companyId ?? ''} onChange={(event) => updateFilter('companyId', event.target.value)}>
                <MenuItem value="">Todas</MenuItem>
                {(companiesQuery.data ?? []).map((company) => (
                  <MenuItem key={company.id} value={company.id}>
                    {company.name}
                  </MenuItem>
                ))}
              </TextField>
            </Grid2>
          )}
          {canFilterTenant && (
            <Grid2 size={{ xs: 12, md: 2 }}>
              <TextField select fullWidth size="small" label="Loja" value={filters.storeId ?? ''} onChange={(event) => updateFilter('storeId', event.target.value)}>
                <MenuItem value="">Todas</MenuItem>
                {(storesQuery.data ?? []).map((store) => (
                  <MenuItem key={store.id} value={store.id}>
                    {store.name}
                  </MenuItem>
                ))}
              </TextField>
            </Grid2>
          )}
          {canFilterSellers && (
            <Grid2 size={{ xs: 12, md: 2 }}>
              <TextField select fullWidth size="small" label="Vendedor" value={filters.sellerId ?? ''} onChange={(event) => updateFilter('sellerId', event.target.value)}>
                <MenuItem value="">Todos</MenuItem>
                {(usersQuery.data ?? [])
                  .filter((item) => item.roles.includes('SELLER'))
                  .map((seller) => (
                    <MenuItem key={seller.id} value={seller.id}>
                      {seller.name}
                    </MenuItem>
                  ))}
              </TextField>
            </Grid2>
          )}
          <Grid2 size={{ xs: 12, md: 2 }}>
            <TextField select fullWidth size="small" label="Origem" value={filters.source ?? ''} onChange={(event) => updateFilter('source', event.target.value)}>
              <MenuItem value="">Todas</MenuItem>
              {sources.map((source) => (
                <MenuItem key={source} value={source}>
                  {source}
                </MenuItem>
              ))}
            </TextField>
          </Grid2>
          <Grid2 size={{ xs: 12, md: 2 }}>
            <TextField fullWidth size="small" type="date" label="De" value={filters.dateFrom ?? ''} onChange={(event) => updateFilter('dateFrom', event.target.value)} slotProps={{ inputLabel: { shrink: true } }} />
          </Grid2>
          <Grid2 size={{ xs: 12, md: 2 }}>
            <TextField fullWidth size="small" type="date" label="Ate" value={filters.dateTo ?? ''} onChange={(event) => updateFilter('dateTo', event.target.value)} slotProps={{ inputLabel: { shrink: true } }} />
          </Grid2>
        </Grid2>
      </Paper>

      {loading && <LinearProgress />}

      <Grid2 container spacing={2}>
        {[
          ['Leads', slaQuery.data?.leadCount ?? 0],
          ['Vendas', salesQuery.data?.length ?? 0],
          ['Valor vendido', money(saleTotal)],
          ['Atrasos SLA', slaQuery.data?.overdueTotal ?? 0],
          ['Resposta media', minutes(slaQuery.data?.averageFirstResponseTimeMinutes ?? 0)],
          ['Fora SLA contato', slaQuery.data?.firstContactOutsideSla ?? 0],
        ].map(([label, value]) => (
          <Grid2 key={label} size={{ xs: 12, sm: 6, md: 2 }}>
            <Paper variant="outlined" sx={{ borderRadius: 1, p: 2, minHeight: 96 }}>
              <Typography variant="body2" color="text.secondary">
                {label}
              </Typography>
              <Typography variant="h6" fontWeight={800} sx={{ mt: 1 }}>
                {value}
              </Typography>
            </Paper>
          </Grid2>
        ))}
      </Grid2>

      <ReportTable title="Leads por periodo" empty="Nenhum lead no periodo." rowCount={leadsQuery.data?.length ?? 0}>
        <TableHead>
          <TableRow>
            <TableCell>Periodo</TableCell>
            <TableCell align="right">Leads</TableCell>
            <TableCell align="right">Vendidos</TableCell>
            <TableCell align="right">Perdidos</TableCell>
            <TableCell align="right">Conversao</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {(leadsQuery.data ?? []).map((item) => (
            <TableRow key={item.period}>
              <TableCell>{date(item.period)}</TableCell>
              <TableCell align="right">{item.leadCount}</TableCell>
              <TableCell align="right">{item.soldLeads}</TableCell>
              <TableCell align="right">{item.lostLeads}</TableCell>
              <TableCell align="right">{item.conversionRate.toFixed(1)}%</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </ReportTable>

      <ReportTable title="Leads por vendedor" empty="Nenhum vendedor com leads no periodo." rowCount={sellersQuery.data?.length ?? 0}>
        <TableHead>
          <TableRow>
            <TableCell>Vendedor</TableCell>
            <TableCell align="right">Leads</TableCell>
            <TableCell align="right">Vendidos</TableCell>
            <TableCell align="right">Perdidos</TableCell>
            <TableCell align="right">Conversao</TableCell>
            <TableCell align="right">Resposta media</TableCell>
            <TableCell align="right">Valor vendido</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {(sellersQuery.data ?? []).map((item) => (
            <TableRow key={item.sellerId}>
              <TableCell>{item.sellerName}</TableCell>
              <TableCell align="right">{item.leadCount}</TableCell>
              <TableCell align="right">{item.soldLeads}</TableCell>
              <TableCell align="right">{item.lostLeads}</TableCell>
              <TableCell align="right">{item.conversionRate.toFixed(1)}%</TableCell>
              <TableCell align="right">{minutes(item.averageFirstResponseTimeMinutes)}</TableCell>
              <TableCell align="right">{money(item.saleValue)}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </ReportTable>

      <Grid2 container spacing={2}>
        <Grid2 size={{ xs: 12, lg: 5 }}>
          <ReportTable title="Leads por origem" empty="Nenhuma origem no periodo." rowCount={sourcesQuery.data?.length ?? 0}>
            <TableHead>
              <TableRow>
                <TableCell>Origem</TableCell>
                <TableCell align="right">Leads</TableCell>
                <TableCell align="right">Vendidos</TableCell>
                <TableCell align="right">Conversao</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {(sourcesQuery.data ?? []).map((item) => (
                <TableRow key={item.source}>
                  <TableCell>{item.source}</TableCell>
                  <TableCell align="right">{item.leadCount}</TableCell>
                  <TableCell align="right">{item.soldLeads}</TableCell>
                  <TableCell align="right">{item.conversionRate.toFixed(1)}%</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </ReportTable>
        </Grid2>
        <Grid2 size={{ xs: 12, lg: 7 }}>
          <Paper variant="outlined" sx={{ borderRadius: 1, p: 2, height: '100%' }}>
            <Typography variant="h6" fontWeight={800} sx={{ mb: 1.5 }}>
              SLA e atrasos
            </Typography>
            <Alert severity={(slaQuery.data?.overdueTotal ?? 0) > 0 ? 'warning' : 'success'} sx={{ borderRadius: 1 }}>
              {slaQuery.data?.overdueToAssign ?? 0} atrasados para atribuicao e {slaQuery.data?.overdueToFirstContact ?? 0} atrasados para primeiro contato.
            </Alert>
            <Grid2 container spacing={1.5} sx={{ mt: 0.5 }}>
              {[
                ['Primeiro contato no SLA', slaQuery.data?.firstContactWithinSla ?? 0],
                ['Primeiro contato fora do SLA', slaQuery.data?.firstContactOutsideSla ?? 0],
                ['Resposta media', minutes(slaQuery.data?.averageFirstResponseTimeMinutes ?? 0)],
              ].map(([label, value]) => (
                <Grid2 key={label} size={{ xs: 12, sm: 4 }}>
                  <Paper variant="outlined" sx={{ borderRadius: 1, p: 1.5 }}>
                    <Typography variant="caption" color="text.secondary">
                      {label}
                    </Typography>
                    <Typography fontWeight={800}>{value}</Typography>
                  </Paper>
                </Grid2>
              ))}
            </Grid2>
          </Paper>
        </Grid2>
      </Grid2>

      <ReportTable title="Vendas realizadas" empty="Nenhuma venda no periodo." rowCount={salesQuery.data?.length ?? 0}>
        <TableHead>
          <TableRow>
            <TableCell>Cliente</TableCell>
            <TableCell>Veiculo</TableCell>
            <TableCell>Vendedor</TableCell>
            <TableCell>Origem</TableCell>
            <TableCell align="right">Valor</TableCell>
            <TableCell>Venda</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {(salesQuery.data ?? []).map((item) => (
            <TableRow key={item.leadId}>
              <TableCell>{item.customerName}</TableCell>
              <TableCell>{item.vehicleInterest ?? '-'}</TableCell>
              <TableCell>{item.sellerName}</TableCell>
              <TableCell>{item.source}</TableCell>
              <TableCell align="right">{money(item.saleValue)}</TableCell>
              <TableCell>{date(item.soldAt)}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </ReportTable>

      <ReportTable title="Leads perdidos" empty="Nenhum lead perdido no periodo." rowCount={lostQuery.data?.length ?? 0}>
        <TableHead>
          <TableRow>
            <TableCell>Cliente</TableCell>
            <TableCell>Veiculo</TableCell>
            <TableCell>Vendedor</TableCell>
            <TableCell>Origem</TableCell>
            <TableCell>Motivo</TableCell>
            <TableCell>Perda</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {(lostQuery.data ?? []).map((item) => (
            <TableRow key={item.leadId}>
              <TableCell>{item.customerName}</TableCell>
              <TableCell>{item.vehicleInterest ?? '-'}</TableCell>
              <TableCell>{item.sellerName}</TableCell>
              <TableCell>{item.source}</TableCell>
              <TableCell>{item.lostReason ?? '-'}</TableCell>
              <TableCell>{date(item.lostAt)}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </ReportTable>
    </Box>
  );
}

function ReportTable({ title, empty, rowCount, children }: { title: string; empty: string; rowCount: number; children: React.ReactNode }) {
  return (
    <Paper variant="outlined" sx={{ borderRadius: 1, p: 2, overflowX: 'auto' }}>
      <Typography variant="h6" fontWeight={800} sx={{ mb: 1.5 }}>
        {title}
      </Typography>
      <Table size="small">{children}</Table>
      {rowCount === 0 && (
        <Typography color="text.secondary" sx={{ py: 2 }}>
          {empty}
        </Typography>
      )}
    </Paper>
  );
}
