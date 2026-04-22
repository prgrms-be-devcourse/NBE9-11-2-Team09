package com.example.parking.global.security;

import com.example.parking.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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

    // 로그인 - JWT 토큰 생성 - access token과 refresh token의 만료 시간 설정
    private final long accessTokenExpirationMillis = 1000L * 60 * 30; // 30분
    private final long refreshTokenExpirationMillis = 1000L * 60 * 60 * 24 * 7; // 7일

    public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 로그인 - JWT 토큰 생성 - 사용자 정보를 담은 JWT access token과 refresh token 생성
    public String createAccessToken(User user) {
        return createToken(user, accessTokenExpirationMillis, "access");
    }

    public String createRefreshToken(User user) {
        return createToken(user, refreshTokenExpirationMillis, "refresh");
    }

    // [CUS-08] 로그인 - JWT 토큰 검증 - 토큰의 유효성 검사 및 사용자 정보 추출
    /**
     * 토큰이 유효하면 아무 일도 하지 않고,
     * 만료/서명오류/형식오류 등 문제가 있으면 예외를 던진다.
     */
    public void validateToken(String token) throws JwtException {
        parseClaims(token);
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

    // access / refresh 토큰 구분을 위해 type claim을 사용
    public String getTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    public Date getExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    // 사용자 정보를 담은 JWT 토큰 생성 - access token과 refresh token의 공통 메서드
    private String createToken(User user, long expirationMillis, String type) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("userId", user.getId())
                .claim("userEmail", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("type", type)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    // [CUS-08] 로그인 - JWT 토큰 검증 - 토큰의 유효성 검사 및 사용자 정보 추출을 위한 공통 메서드
    private Claims parseClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
