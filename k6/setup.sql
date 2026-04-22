-- ============================================================
-- k6 부하 테스트 사전 준비 SQL
-- 실행 순서: 1. 자리 초기화 → 2. 테스트 유저 생성 → 3. 사용 가능한 자리 ID 확인
-- ============================================================

-- ① 테스트에 사용할 자리들 AVAILABLE로 초기화 (테스트 전 매번 실행)
UPDATE parking_spot
SET parking_spot_status = 'AVAILABLE', reserved_at = NULL
WHERE parking_spot_status IN ('OCCUPIED', 'PAYING');

-- ② SMALL 타입 AVAILABLE 자리 50개 ID 확인 (load_test.js의 SPOT_IDS에 복붙)
SELECT parking_spot_id
FROM parking_spot
WHERE parking_spot_status = 'AVAILABLE'
  AND parking_spot_type = 'SMALL'
LIMIT 50;

-- ③ 차종별 자리 현황 확인
SELECT parking_spot_type, parking_spot_status, COUNT(*) as cnt
FROM parking_spot
GROUP BY parking_spot_type, parking_spot_status
ORDER BY parking_spot_type, parking_spot_status;

-- ============================================================
-- 테스트 유저 생성 (vehicleType = SMALL, 비밀번호는 'Test1234!'를 BCrypt로 인코딩한 값)
-- 주의: 비밀번호 해시값은 Spring의 BCryptPasswordEncoder 기준
--       아래 해시값은 'Test1234!' 에 해당하는 값으로 교체 필요
-- 직접 회원가입 API로 계정을 만드는 것을 권장합니다.
-- POST /api/users/signup
-- {
--   "userEmail": "load_user_1@test.com",
--   "password": "Test1234!",
--   "name": "부하테스트1",
--   "plateNumber": "11가1001",
--   "vehicleType": "SMALL"
-- }
-- ============================================================

-- 테스트 후 예약 데이터 정리 (재테스트 시 실행)
DELETE FROM reservation
WHERE user_id IN (
    SELECT user_id FROM users WHERE user_email LIKE 'load_user_%@test.com'
);
