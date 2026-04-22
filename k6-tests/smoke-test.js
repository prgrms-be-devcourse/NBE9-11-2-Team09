import http from 'k6/http'; // HTTP 요청(GET, POST 등)을 보내기 위한 모듈
import { check, sleep } from 'k6'; // 검증(Assertion)과 대기(Sleep) 기능을 위한 모듈

/**
 * 1. 테스트 설정 (Options)
 * 테스트의 규모와 합격 기준을 정의합니다.
 */
export const options = {
  vus: 1,            // Virtual Users: 가상 사용자 1명 설정
  duration: '10s',   // 테스트 실행 시간: 10초 동안 반복
  
  // Thresholds: 테스트 통과를 위한 성능 임계치
  thresholds: {
    http_req_failed: ['rate<0.01'],   // 에러율이 1% 미만이어야 함 (실패 시 테스트 전체 실패 처리)
    http_req_duration: ['p(95)<500'], // 전체 요청 중 95%가 500ms 이내에 완료되어야 함
  },
};

/**
 * 2. Setup 단계 (Initial Configuration)
 * 실제 부하 테스트(default function)가 시작되기 전 딱 '한 번'만 실행됩니다.
 * 주로 인증(Login)을 수행하고 토큰을 받아오는 용도로 사용합니다.
 */
export function setup() {
  const loginUrl = 'http://localhost:8080/api/users/login';
  
  // JSON.stringify: 자바의 객체를 JSON 문자열로 직렬화하는 것과 같음 (DTO 형태 생성)
  // 테스트용 가상 이메일과 비빌번호
  const payload = JSON.stringify({
    userEmail: 'test@test.com', 
    password: 'password123!',
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
  };

  // 로그인 시도
  const loginRes = http.post(loginUrl, payload, params);

  // 서버 응답 디버깅 (로그인 실패 시 원인 파악용)
  // console.log(`[DEBUG] Status: ${loginRes.status}`);
  // console.log(`[DEBUG] Body: ${loginRes.body}`);

  // check: 서버 응답이 200 OK인지 검증 (JUnit의 assertEquals와 비슷)
  const isLoginSuccessful = check(loginRes, {
    '로그인 성공(200)': (r) => r.status === 200,
  });

  // 로그인이 실패하면 토큰 없이 null 리턴
  if (!isLoginSuccessful) {
    console.log(`로그인 실패! 상태 코드: ${loginRes.status}`);
    return { token: null }; 
  }

  // 응답 바디(JSON)에서 accessToken 추출
  // loginRes.json()은 Java의 Jackson 라이브러리가 JSON을 JavaScript 객체(Map/Object)로 변환하는 것과 같음
  const token = loginRes.json().data.accessToken; 
  
  // 여기서 리턴한 값은 아래 default function의 인자(data)로 전달됨
  return { token: token };
}

/**
 * 3. 메인 테스트 시나리오 (Main Loop)
 * 설정한 시간(10s) 동안 가상 사용자(VUs)들이 반복해서 실행하는 핵심 로직입니다.
 */
export default function (data) {
  // setup()에서 넘겨준 토큰이 없으면 더 이상 진행하지 않음
  if (!data.token) {
    console.log('토큰이 없어 테스트를 중단합니다.');
    return;
  }

  const url = 'http://localhost:8080/api/parking-lots'; 
  
  // 인증 헤더에 JWT 토큰 부착
  const params = {
    headers: {
      'Authorization': `Bearer ${data.token}`,
      'Content-Type': 'application/json',
    },
  };
  
  // 실제 주차장 목록 조회 API 호출
  const res = http.get(url, params);

  /**
   * 4. 검증 (Assertion)
   * API 결과가 비즈니스 요구사항에 맞는지 확인합니다.
   */
  check(res, {
    '응답 코드가 200인가': (r) => r.status === 200,
    '데이터가 1개 이상인가': (r) => r.json().data.length >= 1, 
  });

  // 실제 사용자의 행위를 시뮬레이션하기 위해 1초간 쉬어줌 (생략 시 무한 요청 발송)
  sleep(1); 
}