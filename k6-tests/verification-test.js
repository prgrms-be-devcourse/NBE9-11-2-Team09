import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 1, // 검증을 위해 1명만 접속
  iterations: 10, // 10번 반복 수행
};

export default function () {
  const url = 'http://localhost:8080/api/parking-lots';

  // 1. 첫 번째 요청 (Cold Start - 캐시가 없어서 DB로 갈 것임)
  const res1 = http.get(url);
  const coldDuration = res1.timings.duration;

  check(res1, {
    'First request status is 200': (r) => r.status === 200,
  });

  // 캐시가 생성될 시간을 아주 잠깐 준다
  sleep(1);

  // 2. 두 번째 요청 (Warm Start - 이제는 캐시에서 나와야 함)
  const res2 = http.get(url);
  const warmDuration = res2.timings.duration;

  check(res2, {
    'Second request status is 200': (r) => r.status === 200,
    // 검증 로직: 두 번째 요청이 첫 번째 요청보다 최소 2배 이상 빨라야 함
    'Cache is working (Warm is faster than Cold)': (r) => warmDuration < (coldDuration / 2),
  });

  console.log(`Cold: ${coldDuration.toFixed(2)}ms, Warm: ${warmDuration.toFixed(2)}ms`);
}