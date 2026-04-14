package com.example.parking.global.security;

import com.example.parking.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// [CUS-08] 로그인 - JWT 토큰 생성 및 검증을 위한 유틸리티 클래스
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpirationMillis = 1000L * 60 * 60; // 1시간

    public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // [CUS-08] 로그인 - 사용자 식별 정보와 권한을 담은 JWT access token 생성
    public String createAccessToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpirationMillis);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("userId", user.getId())
                .claim("userEmail", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    // [CUS-08] 로그인 - JWT 토큰 검증 - 토큰의 유효성 검사 및 사용자 정보 추출
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // [CUS-08] 로그인 - JWT 토큰 검증 - 토큰에서 사용자 ID 추출
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        Object userId = claims.get("userId");

        if (userId instanceof Integer integerUserId) {
            return integerUserId.longValue();
        }

        if (userId instanceof Long longUserId) {
            return longUserId;
        }

        return Long.valueOf(claims.getSubject());
    }

    // [CUS-08] 로그인 - JWT 토큰 검증 - 토큰에서 사용자 이메일 추출
    public String getUserEmail(String token) {
        return parseClaims(token).get("userEmail", String.class);
    }

    // [CUS-08] 로그인 - JWT 토큰 검증 - 토큰에서 사용자 역할 추출
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // [CUS-08] 로그인 - JWT 토큰 검증 - 토큰의 유효성 검사 및 사용자 정보 추출을 위한 공통 메서드
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}