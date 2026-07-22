import react from '@vitejs/plugin-react';
import { defineConfig } from 'vitest/config';

export default defineConfig({
  plugins: [react()],
  build: {
    rolldownOptions: {
      output: {
        manualChunks(id) {
          if (id.indexOf('node_modules') === -1) {
            return undefined;
          }
          if (id.indexOf('@mui/icons-material') !== -1) {
            return 'vendor-mui-icons';
          }
          if (id.indexOf('@mui/material') !== -1 || id.indexOf('@emotion/') !== -1) {
            return 'vendor-mui';
          }
          if (id.indexOf('react-dom') !== -1 || id.indexOf('react-router-dom') !== -1 || id.indexOf('/react/') !== -1) {
            return 'vendor-react';
          }
          if (id.indexOf('@tanstack/react-query') !== -1) {
            return 'vendor-query';
          }
          if (id.indexOf('react-hook-form') !== -1 || id.indexOf('@hookform/') !== -1 || id.indexOf('/zod/') !== -1) {
            return 'vendor-forms';
          }
          if (id.indexOf('recharts') !== -1 || id.indexOf('d3-') !== -1) {
            return 'vendor-charts';
          }
          return 'vendor';
        },
      },
    },
  },
  server: {
    port: 5173,
  },
  test: {
    environment: 'jsdom',
    exclude: ['e2e/**', 'node_modules/**', 'dist/**'],
    globals: true,
    setupFiles: './src/test/setup.ts',
  },
});
