import { zodResolver } from '@hookform/resolvers/zod';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import { Alert, Box, Button, Paper, TextField, Typography } from '@mui/material';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
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
    <Box sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center', bgcolor: 'background.default', px: 2 }}>
      <Paper
        component="form"
        onSubmit={handleSubmit(onSubmit)}
        variant="outlined"
        sx={{ width: '100%', maxWidth: 420, p: 4, borderRadius: 1, display: 'grid', gap: 2.5 }}
      >
        <Box sx={{ display: 'grid', gap: 1 }}>
          <LockOutlinedIcon color="primary" />
          <Typography component="h1" variant="h4" fontWeight={800}>
            EAI
          </Typography>
          <Typography color="text.secondary">Acesse a operacao da loja.</Typography>
        </Box>

        {error && <Alert severity="error">{error}</Alert>}

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
