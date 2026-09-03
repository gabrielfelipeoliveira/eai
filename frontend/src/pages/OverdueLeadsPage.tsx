import AssignmentLateIcon from '@mui/icons-material/AssignmentLate';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { Box, Chip, Stack, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { PageHeader } from '../components/PageHeader';
import { RecordCard, RecordCardRow } from '../components/RecordCard';
import { ResponsiveDataView } from '../components/ResponsiveDataView';
import { vehicleLabel } from '../features/leads/leadDisplay';
import { listOverdueLeads } from '../services/leadService';
import { listUsers } from '../services/userService';

export function OverdueLeadsPage() {
  const overdueQuery = useQuery({
    queryKey: ['overdue-leads'],
    queryFn: listOverdueLeads,
  });
  const usersQuery = useQuery({
    queryKey: ['users'],
    queryFn: listUsers,
  });

  function userName(userId: string | null) {
    if (!userId) {
      return 'Sem vendedor';
    }
    return usersQuery.data?.find((user) => user.id === userId)?.name ?? userId;
  }

  const leads = overdueQuery.data ?? [];

  function slaChips(lead: (typeof leads)[number]) {
    return (
      <Stack direction="row" flexWrap="wrap" gap={0.75}>
        {lead.overdueToAssign && <Chip color="error" icon={<AssignmentLateIcon />} label="Atribuicao" size="small" variant="outlined" />}
        {lead.overdueToFirstContact && <Chip color="error" icon={<WarningAmberIcon />} label="Primeiro contato" size="small" variant="outlined" />}
      </Stack>
    );
  }

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <PageHeader description="Fila gerencial de leads fora do prazo de atribuicao ou primeiro contato." title="Leads atrasados" />

      <ResponsiveDataView
        cards={leads.map((lead) => (
          <RecordCard key={lead.id} status={slaChips(lead)} subtitle={lead.customerPhone ?? lead.customerEmail ?? '-'} title={lead.customerName}>
            <RecordCardRow label="Veiculo" value={vehicleLabel(lead)} />
            <RecordCardRow label="Vendedor" value={userName(lead.assignedToUserId)} />
            <RecordCardRow label="Criado em" value={new Date(lead.createdAt).toLocaleString('pt-BR')} />
          </RecordCard>
        ))}
        empty={!overdueQuery.isLoading && leads.length === 0}
        emptyMessage="Nenhum lead atrasado."
        error={overdueQuery.isError}
        loading={overdueQuery.isLoading}
        loadingLabel="Carregando leads atrasados"
        table={
          <Table sx={{ minWidth: 900 }}>
          <TableHead>
            <TableRow>
              <TableCell>Cliente</TableCell>
              <TableCell>Veiculo</TableCell>
              <TableCell>Vendedor</TableCell>
              <TableCell>SLA</TableCell>
              <TableCell>Criado em</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {leads.map((lead) => (
              <TableRow key={lead.id} hover>
                <TableCell>
                  <Typography fontWeight={700}>{lead.customerName}</Typography>
                  <Typography color="text.secondary" variant="caption">
                    {lead.customerPhone ?? lead.customerEmail ?? '-'}
                  </Typography>
                </TableCell>
                <TableCell>{vehicleLabel(lead)}</TableCell>
                <TableCell>{userName(lead.assignedToUserId)}</TableCell>
                <TableCell>{slaChips(lead)}</TableCell>
                <TableCell>{new Date(lead.createdAt).toLocaleString('pt-BR')}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        }
      />
    </Box>
  );
}
