package com.example.parking.domain.user.repository;

import com.example.parking.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // [CUS-06] 회원가입 - 회원가입 시 이메일/차량번호 중복 검사
    boolean existsByEmail(String email);
    boolean existsByPlateNumber(String plateNumber);

    // [CUS-08] 로그인 - 로그인 시 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // [CUS-10] 차량 정보 수정 - 본인을 제외한 다른 사용자가 같은 차량번호를 사용하는지 확인
    // 본인 차량번호와 같은 값으로 다시 저장하는 경우는 허용해야 하므로 AndIdNot 조건이 필요
    boolean existsByPlateNumberAndIdNot(String plateNumber, Long id);
}
