import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '20s', target: 20 },  // 평소 트래픽 (Warm-up)
    { duration: '10s', target: 500 }, // 500명으로 폭증 (Spike)
    { duration: '1m', target: 500 },  // 1분간 버티기 (폭증 상태 유지)
    { duration: '20s', target: 0 },   // 종료
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'], // 에러율 1% 미만이어야 함
    http_req_duration: ['p(95)<100'], // 95%의 요청은 100ms 이하여야 함
  },
};

export default function () {
  const url = 'http://localhost:8080/api/parking-lots';
  const res = http.get(url);

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(0.1); // 요청 간격을 좁혀서 부하를 줌
}