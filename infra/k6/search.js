import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: Number(__ENV.VUS || 10),
  duration: __ENV.DURATION || '30s',
  thresholds: { http_req_failed: ['rate<0.01'], http_req_duration: ['p(95)<500'], checks: ['rate>0.99'] },
};
export default function () {
  const from = __ENV.FROM || 'DEMO_SEOUL';
  const to = __ENV.TO || 'DEMO_BUSAN';
  if (!__ENV.DATE) throw new Error('DATE must identify a seeded future operation date');
  const response = http.get(`${__ENV.BASE_URL || 'http://frontend'}/api/train-schedules?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&date=${__ENV.DATE}`);
  check(response, { 'search returns schedules': (r) => r.status === 200 && r.json('success') === true && r.json('data').length > 0 });
  sleep(0.2);
}
