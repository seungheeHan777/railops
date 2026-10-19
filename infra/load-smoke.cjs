const { performance } = require('node:perf_hooks');

async function main() {
  const base = process.env.BASE_URL || 'http://127.0.0.1:8080';
  const date = process.env.DATE;
  if (!date) throw new Error('DATE must identify a seeded future operation date');
  const query = new URLSearchParams({ from: process.env.FROM || 'DEMO_SEOUL', to: process.env.TO || 'DEMO_BUSAN', date });
  const durations = [];
  let failures = 0;
  const started = performance.now();
  await Promise.all(Array.from({ length: 10 }, async () => {
    for (let i = 0; i < 20; i++) {
      const start = performance.now();
      try {
        const response = await fetch(`${base}/api/train-schedules?${query}`, { signal: AbortSignal.timeout(10000) });
        const payload = await response.json();
        if (!response.ok || !payload.success || !payload.data.length) failures++;
      } catch { failures++; }
      durations.push(performance.now() - start);
    }
  }));
  durations.sort((a, b) => a - b);
  const result = { concurrency: 10, requests: durations.length, failures, elapsedMs: Math.round(performance.now() - started),
    p50Ms: Math.round(durations[Math.floor(durations.length * 0.5)]), p95Ms: Math.round(durations[Math.floor(durations.length * 0.95)]), maxMs: Math.round(durations.at(-1)) };
  console.log(JSON.stringify(result, null, 2));
  if (failures) process.exitCode = 1;
}
main().catch((error) => { console.error(error.message); process.exitCode = 1; });
