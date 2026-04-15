package com.example.parking.domain.user.repository;

import com.example.parking.domain.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 로그인 - 로그인 시 리프레시 토큰 저장 및 조회를 위한 RefreshTokenRepository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUserId(Long userId);

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(Long userId);
}
