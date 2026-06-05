import { execSync } from 'node:child_process';

const basePort = parseInt(process.argv[2] || '5173', 10);
const ports = [basePort, basePort + 1];

for (const port of ports) {
  try {
    if (process.platform === 'win32') {
      const result = execSync(`netstat -ano | findstr :${port} | findstr LISTENING`, {
        encoding: 'utf8'
      });
      for (const line of result.trim().split(/\r?\n/)) {
        const pid = line.trim().split(/\s+/).pop();
        if (pid && pid !== '0') {
          execSync(`taskkill /F /PID ${pid}`, { stdio: 'ignore' });
        }
      }
    } else {
      execSync(`fuser -k ${port}/tcp 2>/dev/null`, { stdio: 'ignore' });
    }
  } catch {
    // Port not in use — ignore
  }
}
