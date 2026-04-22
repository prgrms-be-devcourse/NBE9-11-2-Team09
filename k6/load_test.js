import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// ============================================================
// 커스텀 메트릭
// ============================================================
const reserveSuccess  = new Counter('reserve_success');
const reserveFail     = new Counter('reserve_fail');
const errorRate       = new Rate('error_rate');
const reserveDuration = new Trend('reserve_duration_ms', true); // 예약 API 응답시간 별도 추적

// ============================================================
// 설정값 - 반드시 실제 환경에 맞게 수정하세요
// ============================================================
const BASE_URL      = 'http://localhost:8080';
const PARKING_LOT_ID = 1; // 테스트할 주차장 ID

// setup.sql의 ② 쿼리 결과를 여기에 붙여넣으세요 (SMALL 타입 자리 ID 목록)
// VU 수와 같거나 많아야 합니다
const SPOT_IDS = [
  1001, 1002, 1003, 1004, 1005,
  1006, 1007, 1008, 1009, 1010,
  1011, 1012, 1013, 1014, 1015,
  1016, 1017, 1018, 1019, 1020,
  // ... 50개까지 채우세요
];

// 테스트 유저 목록 (vehicleType = SMALL 계정이어야 함)
// SPOT_IDS 수와 같거나 많아야 합니다 (1인 1주차장 제한 때문)
const TEST_USERS = [
  { email: 'load_user_1@test.com', password: 'Test1234!' },
  { email: 'load_user_2@test.com', password: 'Test1234!' },
  { email: 'load_user_3@test.com', password: 'Test1234!' },
  // ... SPOT_IDS 수만큼 채우세요
];

// 예약 시간 (현재보다 미래 시간으로 유지)
const START_TIME = '2026-05-01 14:00:00';
const END_TIME   = '2026-05-01 16:00:00';

// ============================================================
// 시나리오 설정
// VU 수를 TEST_USERS / SPOT_IDS 수에 맞게 조정하세요
// 예: 유저 20명 → maxVUs: 20
// ============================================================
export const options = {
  scenarios: {
    load_test: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 10 },  // 워밍업: 10명까지
        { duration: '20s', target: 20 },  // 중간 부하
        { duration: '20s', target: 50 },  // 최대 부하 (SPOT_IDS 수 이하로)
        { duration: '10s', target: 0  },  // 종료
      ],
      gracefulRampDown: '5s',
    },
  },
  thresholds: {
    // 예약 API p(95) 응답시간 1초 이내
    reserve_duration_ms: ['p(95)<1000'],
    // 에러율 30% 미만 (일부 중복/만료 실패는 허용)
    error_rate: ['rate<0.30'],
    // 전체 HTTP p(95) 2초 이내
    http_req_duration: ['p(95)<2000'],
  },
};

// ============================================================
// setup(): 유저별 토큰 발급 (1회 실행)
// ============================================================
export function setup() {
  console.log(`총 유저: ${TEST_USERS.length}명 / 총 자리: ${SPOT_IDS.length}개`);

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
    } else {
      console.error(`[LOGIN FAIL] ${user.email} → ${res.status}`);
      tokens.push(null);
    }
  }

  const validCount = tokens.filter(t => t !== null).length;
  console.log(`로그인 성공: ${validCount}/${TEST_USERS.length}명`);

  return { tokens };
}

// ============================================================
// 메인 테스트 함수
// VU마다 고유한 유저 + 고유한 자리를 할당
// ============================================================
export default function (data) {
  const { tokens } = data;

  // VU 인덱스 기반으로 유저와 자리를 고정 배정
  const idx   = (__VU - 1) % tokens.length;
  const token = tokens[idx];
  const spotId = SPOT_IDS[idx % SPOT_IDS.length];

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
  const startTs = Date.now();

  const res = http.post(
    `${BASE_URL}/api/reservations`,
    JSON.stringify({
      parkingLotId: PARKING_LOT_ID,
      parkingSpotId: spotId,
      startTime: START_TIME,
      endTime: END_TIME,
    }),
    { headers }
  );

  const elapsed = Date.now() - startTs;
  reserveDuration.add(elapsed);

  const isSuccess = check(res, {
    '예약 성공 (200)': (r) => r.status === 200,
  });

  if (isSuccess) {
    reserveSuccess.add(1);
    errorRate.add(0);
    console.log(`[SUCCESS] VU ${__VU} | spotId: ${spotId} | ${elapsed}ms`);
  } else {
    reserveFail.add(1);
    errorRate.add(1);

    // 에러 유형 분류
    const body = JSON.parse(res.body || '{}');
    const msg  = body.msg || '알 수 없는 오류';

    if (res.status === 409) {
      // 선점 실패 or 중복 예약 → 동시성 제어 정상 동작
      console.log(`[CONFLICT] VU ${__VU} | spotId: ${spotId} | ${msg}`);
    } else if (res.status === 401) {
      console.error(`[AUTH FAIL] VU ${__VU} | 토큰 만료 또는 인증 오류`);
    } else {
      console.error(`[FAIL] VU ${__VU} | status: ${res.status} | ${msg}`);
    }
  }

  // 실제 유저처럼 약간의 딜레이 (0.1~0.5초)
  sleep(Math.random() * 0.4 + 0.1);
}

// ============================================================
// teardown(): 결과 요약
// ============================================================
export function teardown(data) {
  console.log('======================================');
  console.log('부하 테스트 완료');
  console.log(`유저 수: ${TEST_USERS.length} / 자리 수: ${SPOT_IDS.length}`);
  console.log('reserve_duration_ms 지표로 DB 성능 확인하세요');
  console.log('======================================');
}
