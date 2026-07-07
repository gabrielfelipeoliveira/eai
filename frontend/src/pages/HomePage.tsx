import { Box, Grid2, Paper, Typography } from '@mui/material';

interface HomePageProps {
  title?: string;
}

export function HomePage({ title = 'Dashboard' }: HomePageProps) {
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

      <Grid2 container spacing={2}>
        {[
          ['Leads novos', '24'],
          ['Tempo medio de resposta', '8 min'],
          ['Conversas ativas', '17'],
          ['Vendas em negociacao', '31'],
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
          Inicio pos-login
        </Typography>
        <Typography color="text.secondary" sx={{ mt: 1 }}>
          A area autenticada ja esta protegida por token JWT e pronta para receber os modulos comerciais.
        </Typography>
      </Paper>
    </Box>
  );
}
