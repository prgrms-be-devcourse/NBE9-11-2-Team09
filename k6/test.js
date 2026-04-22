import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate } from 'k6/metrics';

// ============================================================
// 커스텀 메트릭
// ============================================================
const reserveSuccess = new Counter('reserve_success');   // 예약 성공 횟수
const reserveFail    = new Counter('reserve_fail');      // 예약 실패 횟수
const errorRate      = new Rate('error_rate');           // 에러 비율

// ============================================================
// 설정값 - 실행 전 반드시 본인 환경에 맞게 수정하세요
// ============================================================
const BASE_URL      = 'http://localhost:8080';
const PARKING_LOT_ID = 1;   // 테스트할 주차장 ID
const SPOT_ID        = 1;   // 테스트할 자리 ID (AVAILABLE 상태여야 함)

// 테스트 유저 목록 (DB에 미리 가입된 계정이어야 함)
// Race Condition 테스트는 VU 수만큼, Spike 테스트는 충분히 준비
const TEST_USERS = [
  { email: 'user1@test.com', password: 'Test1234!' },
  { email: 'user2@test.com', password: 'Test1234!' },
  { email: 'user3@test.com', password: 'Test1234!' },
  // ... 필요한 만큼 추가
];

// 예약 시간 (미래 시간으로 설정)
const START_TIME = '2026-05-01 14:00:00';
const END_TIME   = '2026-05-01 16:00:00';

// ============================================================
// TEST_TYPE 환경변수로 시나리오 선택
// 실행 예시:
//   k6 run --env TEST_TYPE=smoke test.js
//   k6 run --env TEST_TYPE=race  test.js
//   k6 run --env TEST_TYPE=spike test.js
// ============================================================
const TEST_TYPE = __ENV.TEST_TYPE || 'smoke';

const scenarios = {

  // ----------------------------------------------------------
  // [1] Smoke Test
  // 목적: API 흐름이 정상 작동하는지 기본 확인 (1~2 VU)
  // ----------------------------------------------------------
  smoke: {
    executor: 'shared-iterations',
    vus: 1,
    iterations: 1,
    maxDuration: '30s',
  },

  // ----------------------------------------------------------
  // [2] Race Condition Test
  // 목적: 동일 자리에 동시 요청 시 단 1건만 성공하는지 검증
  //       tryReserve CAS가 제대로 동작하는지 확인
  //
  // 주의: VU 수 = TEST_USERS 배열 크기와 맞춰야 함
  //       같은 SPOT_ID에 몰아넣으므로 1건만 200, 나머지는 실패 응답이어야 함
  //       테스트 전 해당 자리를 AVAILABLE 상태로 초기화할 것
  // ----------------------------------------------------------
  race: {
    executor: 'shared-iterations',
    vus: 3,          // TEST_USERS 수와 맞추세요
    iterations: 3,   // VU당 1번씩 동시 요청 (총 3번)
    maxDuration: '30s',
  },

  // ----------------------------------------------------------
  // [3] Spike Test
  // 목적: 급격한 트래픽 유입 시 DB 커넥션 풀 및 응답 안정성 확인
  //       HikariCP 기본 풀(10개) 한계 상황에서의 동작 검증
  //
  // 주의: 각 VU가 서로 다른 자리를 예약해야 의미 있는 테스트가 됨
  //       DB에 AVAILABLE 상태의 자리를 충분히 준비해둘 것
  //       1인 1주차장 제한으로 인해 유저도 VU 수만큼 필요함
  // ----------------------------------------------------------
  spike: {
    executor: 'ramping-vus',
    startVUs: 0,
    stages: [
      { duration: '5s',  target: 10  }, // 빠르게 10명으로 증가
      { duration: '10s', target: 100 }, // 100명까지 급증 (스파이크 시작)
      { duration: '10s', target: 500 }, // 최대 500명 (오픈런 상황 재현)
      { duration: '10s', target: 0   }, // 트래픽 급감
    ],
    gracefulRampDown: '5s',
  },
};

export const options = {
  scenarios: {
    parking_test: scenarios[TEST_TYPE],
  },
  thresholds: {
    // 전체 요청의 95%가 2초 이내 응답
    http_req_duration: ['p(95)<2000'],
    // 에러율 10% 미만 (Race test에서는 실패가 정상이므로 높게 설정)
    error_rate: ['rate<0.95'],
  },
};

// ============================================================
// setup(): 테스트 시작 전 1회 실행 - 유저별 토큰 발급
// ============================================================
export function setup() {
  const tokens = [];

  for (const user of TEST_USERS) {
    const res = http.post(
      `${BASE_URL}/api/users/login`,
      JSON.stringify({ userEmail: user.email, password: user.password }),
      { headers: { 'Content-Type': 'application/json' } }
    );

    if (res.status === 200) {
      const body = JSON.parse(res.body);
      tokens.push(body.data.accessToken);
      console.log(`[LOGIN OK] ${user.email}`);
    } else {
      console.error(`[LOGIN FAIL] ${user.email} → ${res.status}: ${res.body}`);
      tokens.push(null);
    }
  }

  return { tokens };
}

// ============================================================
// 메인 테스트 함수
// ============================================================
export default function (data) {
  const { tokens } = data;

  // VU 인덱스로 유저 토큰 선택 (순환)
  const token = tokens[(__VU - 1) % tokens.length];

  if (!token) {
    console.error(`[SKIP] VU ${__VU} - 토큰 없음`);
    return;
  }

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };

  // ----------------------------------------------------------
  // 예약 요청
  // ----------------------------------------------------------
  const payload = JSON.stringify({
    parkingLotId: PARKING_LOT_ID,
    parkingSpotId: SPOT_ID,
    startTime: START_TIME,
    endTime: END_TIME,
  });

  const res = http.post(`${BASE_URL}/api/reservations`, payload, { headers });

  const isSuccess = check(res, {
    '예약 성공 (200)': (r) => r.status === 200,
  });

  // 커스텀 메트릭 기록
  if (isSuccess) {
    reserveSuccess.add(1);
    errorRate.add(0);
    console.log(`[SUCCESS] VU ${__VU} - 예약 성공`);
  } else {
    reserveFail.add(1);
    errorRate.add(1);
    console.log(`[FAIL] VU ${__VU} - status: ${res.status} / body: ${res.body}`);
  }

  // Spike test에서는 약간의 간격을 두어 실제 유저 행동 흉내냄
  if (TEST_TYPE === 'spike') {
    sleep(Math.random() * 0.5); // 0~0.5초 랜덤 딜레이
  }
}

// ============================================================
// teardown(): 테스트 종료 후 결과 요약 출력
// ============================================================
export function teardown(data) {
  console.log('==============================');
  console.log('테스트 종료');
  console.log(`TEST_TYPE : ${TEST_TYPE}`);
  console.log(`SPOT_ID   : ${SPOT_ID}`);
  console.log('==============================');
}
