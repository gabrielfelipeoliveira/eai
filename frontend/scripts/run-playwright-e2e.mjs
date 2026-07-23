import { spawn } from 'node:child_process';

const { NO_COLOR: _noColor, ...env } = process.env;
const command = process.platform === 'win32' ? 'playwright.cmd' : 'playwright';
const child = spawn(command, ['test', ...process.argv.slice(2)], {
  env,
  shell: process.platform === 'win32',
  stdio: 'inherit',
});

child.on('exit', (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal);
    return;
  }

  process.exit(code ?? 1);
});

child.on('error', (error) => {
  console.error(error);
  process.exit(1);
});
