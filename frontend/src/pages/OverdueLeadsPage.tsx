import AssignmentLateIcon from '@mui/icons-material/AssignmentLate';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { Box, Chip, LinearProgress, Paper, Stack, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
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

  return (
    <Box sx={{ display: 'grid', gap: 3 }}>
      <Box>
        <Typography component="h2" variant="h4" fontWeight={800}>
          Leads atrasados
        </Typography>
        <Typography color="text.secondary">Fila gerencial de leads fora do prazo de atribuicao ou primeiro contato.</Typography>
      </Box>

      {overdueQuery.isLoading && <LinearProgress />}

      <Paper variant="outlined" sx={{ borderRadius: 1, overflow: 'hidden' }}>
        <Table>
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
            {overdueQuery.data?.map((lead) => (
              <TableRow key={lead.id} hover>
                <TableCell>
                  <Typography fontWeight={700}>{lead.customerName}</Typography>
                  <Typography color="text.secondary" variant="caption">
                    {lead.customerPhone ?? lead.customerEmail ?? '-'}
                  </Typography>
                </TableCell>
                <TableCell>{vehicleLabel(lead)}</TableCell>
                <TableCell>{userName(lead.assignedToUserId)}</TableCell>
                <TableCell>
                  <Stack direction="row" flexWrap="wrap" gap={0.75}>
                    {lead.overdueToAssign && <Chip color="error" icon={<AssignmentLateIcon />} label="Atribuicao" size="small" variant="outlined" />}
                    {lead.overdueToFirstContact && <Chip color="error" icon={<WarningAmberIcon />} label="Primeiro contato" size="small" variant="outlined" />}
                  </Stack>
                </TableCell>
                <TableCell>{new Date(lead.createdAt).toLocaleString('pt-BR')}</TableCell>
              </TableRow>
            ))}
            {!overdueQuery.isLoading && overdueQuery.data?.length === 0 && (
              <TableRow>
                <TableCell colSpan={5}>Nenhum lead atrasado.</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Paper>
    </Box>
  );
}
