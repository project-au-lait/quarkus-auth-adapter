import http from 'node:http';

const ports = process.argv.slice(2).map((p) => parseInt(p, 10));
const timeout = 30000;
const interval = 500;

function checkPort(port) {
  return new Promise((resolve) => {
    const req = http.get(`http://localhost:${port}`, (res) => {
      res.resume();
      resolve(true);
    });
    req.on('error', () => resolve(false));
    req.setTimeout(1000, () => {
      req.destroy();
      resolve(false);
    });
  });
}

async function waitForPort(port) {
  const start = Date.now();
  while (Date.now() - start < timeout) {
    if (await checkPort(port)) {
      console.log(`Port ${port} is ready`);
      return;
    }
    await new Promise((r) => setTimeout(r, interval));
  }
  throw new Error(`Port ${port} did not become ready within ${timeout}ms`);
}

try {
  await Promise.all(ports.map(waitForPort));
  console.log('All servers are ready');
} catch (e) {
  console.error(e.message);
  process.exit(1);
}
