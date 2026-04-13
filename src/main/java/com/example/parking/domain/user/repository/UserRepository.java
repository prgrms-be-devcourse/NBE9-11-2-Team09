package com.example.parking.domain.user.repository;

import com.example.parking.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 회원가입 시 이메일/차량번호 중복 검사
    boolean existsByEmail(String email);
    boolean existsByPlateNumber(String plateNumber);
}
