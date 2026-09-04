import { alpha, createTheme } from '@mui/material/styles';

const colors = {
  accent: '#2563eb',
  border: '#d8e0ea',
  canvas: '#f4f6f8',
  danger: '#dc2626',
  ink: '#0f172a',
  muted: '#64748b',
  panel: '#ffffff',
  success: '#15803d',
  warning: '#b45309',
};

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: colors.accent,
      dark: '#1d4ed8',
      light: '#60a5fa',
    },
    secondary: {
      main: colors.ink,
    },
    background: {
      default: colors.canvas,
      paper: colors.panel,
    },
    divider: colors.border,
    error: {
      main: colors.danger,
    },
    success: {
      main: colors.success,
    },
    text: {
      primary: colors.ink,
      secondary: colors.muted,
    },
    warning: {
      main: colors.warning,
    },
  },
  shape: {
    borderRadius: 8,
  },
  typography: {
    fontFamily: ['"IBM Plex Sans"', 'Roboto', 'Arial', 'sans-serif'].join(','),
    h4: {
      fontSize: '1.75rem',
      fontWeight: 700,
      lineHeight: 1.2,
    },
    h5: {
      fontSize: '1.375rem',
      fontWeight: 700,
      lineHeight: 1.25,
    },
    h6: {
      fontSize: '1rem',
      fontWeight: 700,
      lineHeight: 1.35,
    },
    button: {
      fontWeight: 700,
      letterSpacing: 0,
      textTransform: 'none',
    },
  },
  components: {
    MuiAlert: {
      styleOverrides: {
        root: {
          borderRadius: 8,
        },
      },
    },
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
      styleOverrides: {
        root: {
          borderRadius: 8,
          minHeight: 36,
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 6,
          fontWeight: 600,
        },
      },
    },
    MuiIconButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
        },
      },
    },
    MuiInputBase: {
      styleOverrides: {
        root: {
          backgroundColor: colors.panel,
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        outlined: {
          borderColor: colors.border,
          boxShadow: `0 1px 2px ${alpha(colors.ink, 0.04)}`,
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: {
          backgroundColor: '#f8fafc',
          color: colors.muted,
          fontSize: '0.75rem',
          fontWeight: 700,
          textTransform: 'uppercase',
        },
      },
    },
  },
});
