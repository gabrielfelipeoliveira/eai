import { zodResolver } from '@hookform/resolvers/zod';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import StorefrontIcon from '@mui/icons-material/Storefront';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import { Alert, Box, Button, Chip, Paper, Stack, TextField, Typography } from '@mui/material';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Navigate, useLocation, useNavigate } from 'react-router';
import { z } from 'zod';
import { useAuth } from '../hooks/useAuth';

const loginSchema = z.object({
  email: z.string().email('Informe um e-mail valido'),
  password: z.string().min(1, 'Informe a senha'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export function LoginPage() {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState<string | null>(null);
  const from = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? '/';

  const {
    formState: { errors, isSubmitting },
    handleSubmit,
    register,
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: 'admin@eai.com',
      password: 'admin123',
    },
  });

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  async function onSubmit(values: LoginFormValues) {
    setError(null);
    try {
      await login(values.email, values.password);
      navigate(from, { replace: true });
    } catch {
      setError('E-mail ou senha invalidos.');
    }
  }

  return (
    <Box
      sx={{
        alignItems: 'center',
        bgcolor: 'background.default',
        display: 'grid',
        gridTemplateColumns: { xs: '1fr', md: 'minmax(320px, 0.95fr) minmax(360px, 420px)' },
        minHeight: '100vh',
        px: { xs: 2, sm: 4, md: 6 },
        py: { xs: 4, md: 6 },
      }}
    >
      <Box sx={{ display: { xs: 'none', md: 'grid' }, gap: 3, maxWidth: 560 }}>
        <Box>
          <Typography color="text.secondary" fontWeight={700} variant="caption">
            EAI CRM
          </Typography>
          <Typography component="h2" sx={{ mt: 1, maxWidth: 520 }} variant="h4">
            Operacao comercial automotiva em um unico painel.
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 1.5, maxWidth: 460 }} variant="body2">
            Acompanhe leads, pipeline, conversas e rotinas da loja com foco em acao rapida.
          </Typography>
        </Box>

        <Stack direction="row" flexWrap="wrap" gap={1}>
          <Chip icon={<StorefrontIcon />} label="Multi-loja" variant="outlined" />
          <Chip icon={<TrendingUpIcon />} label="Pipeline" variant="outlined" />
          <Chip icon={<LockOutlinedIcon />} label="Acesso seguro" variant="outlined" />
        </Stack>
      </Box>

      <Paper
        component="form"
        onSubmit={handleSubmit(onSubmit)}
        variant="outlined"
        sx={{
          borderRadius: 1,
          display: 'grid',
          gap: 2.25,
          justifySelf: { xs: 'center', md: 'end' },
          maxWidth: 420,
          p: { xs: 3, sm: 4 },
          width: '100%',
        }}
      >
        <Box sx={{ display: 'grid', gap: 1 }}>
          <Box
            sx={{
              alignItems: 'center',
              bgcolor: 'primary.main',
              borderRadius: 1,
              color: 'primary.contrastText',
              display: 'grid',
              height: 40,
              placeItems: 'center',
              width: 40,
            }}
          >
            <LockOutlinedIcon fontSize="small" />
          </Box>
          <Typography component="h1" variant="h4">
            EAI
          </Typography>
          <Typography color="text.secondary" variant="body2">
            Acesse a operacao da loja.
          </Typography>
        </Box>

        {error && (
          <Alert severity="error" variant="outlined">
            {error}
          </Alert>
        )}

        <TextField
          autoComplete="email"
          error={Boolean(errors.email)}
          helperText={errors.email?.message}
          label="E-mail"
          type="email"
          {...register('email')}
        />
        <TextField
          autoComplete="current-password"
          error={Boolean(errors.password)}
          helperText={errors.password?.message}
          label="Senha"
          type="password"
          {...register('password')}
        />
        <Button disabled={isSubmitting} size="large" type="submit" variant="contained">
          Entrar
        </Button>
      </Paper>
    </Box>
  );
}
