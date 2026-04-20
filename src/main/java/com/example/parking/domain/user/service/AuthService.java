package com.example.parking.domain.user.service;

import com.example.parking.domain.user.dto.LoginReqDto;
import com.example.parking.domain.user.dto.LoginResDto;
import com.example.parking.domain.user.dto.RefreshTokenReqDto;
import com.example.parking.domain.user.entity.RefreshToken;
import com.example.parking.domain.user.entity.User;
import com.example.parking.domain.user.entity.UserStatus;
import com.example.parking.domain.user.repository.RefreshTokenRepository;
import com.example.parking.domain.user.repository.UserRepository;
import com.example.parking.global.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 로그인, 토큰 재발급, 로그아웃 등 인증 관련 책임 담당
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // [CUS-08] 로그인 - 로그인 요청을 받아 사용자 인증 후 JWT access token 발급
    @Transactional
    public LoginResDto login(LoginReqDto reqDto) {
        User user = userRepository.findByEmail(reqDto.getUserEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // [CUS-08] 로그인 - 저장된 암호화 비밀번호와 입력 비밀번호 비교
        if (!passwordEncoder.matches(reqDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // [CUS-08] 로그인 - ACTIVE 상태가 아닌 사용자는 로그인할 수 없다.
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("탈퇴한 사용자는 로그인할 수 없습니다.");
        }

        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);

        // 사용자당 refresh token 1개 정책으로 저장하거나 기존 값을 갱신한다.
        saveOrUpdateRefreshToken(user.getId(), refreshToken);

        return new LoginResDto(accessToken, refreshToken, "Bearer");
    }

    @Transactional
    public LoginResDto refresh(RefreshTokenReqDto reqDto) {
        String refreshTokenValue = reqDto.getRefreshToken();

        if (!jwtUtil.isValid(refreshTokenValue)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        if (!"refresh".equals(jwtUtil.getTokenType(refreshTokenValue))) {
            throw new IllegalArgumentException("리프레시 토큰이 아닙니다.");
        }

        // JWT가 유효해도 DB에 저장된 토큰이 아니면 재발급을 허용하지 않는다.
        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("저장된 리프레시 토큰이 없습니다."));

        Long userId = jwtUtil.getUserId(refreshTokenValue);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!savedToken.getUserId().equals(userId)) {
            throw new IllegalArgumentException("리프레시 토큰 사용자 정보가 일치하지 않습니다.");
        }

        // 재발급 시점에는 실제 사용자 상태를 다시 확인한다.
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("탈퇴한 사용자는 토큰을 재발급할 수 없습니다.");
        }

        String newAccessToken = jwtUtil.createAccessToken(user);

        // 1차 버전에서는 rotation 없이 기존 refresh token을 그대로 유지한다.
        return new LoginResDto(newAccessToken, refreshTokenValue, "Bearer");
    }

    // 로그아웃 - 클라이언트가 access token을 삭제하는 방식으로 로그아웃 처리 (서버에서는 별도의 상태 관리 없이 JWT의 유효성 검증으로 처리)
    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("탈퇴한 사용자는 로그아웃할 수 없습니다.");
        }

        // 로그아웃 시 저장된 refresh token을 제거해 세션을 종료
        refreshTokenRepository.deleteByUserId(userId);
    }

    // 사용자당 refresh token 1개 정책으로 저장하거나 기존 값을 갱신하는 메서드
    @Transactional
    public void saveOrUpdateRefreshToken(Long userId, String refreshToken) {
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                jwtUtil.getExpiration(refreshToken).toInstant(),
                ZoneId.systemDefault()
        );

        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        savedToken -> savedToken.updateToken(refreshToken, expiresAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .userId(userId)
                                        .token(refreshToken)
                                        .expiresAt(expiresAt)
                                        .build()
                        )
                );
    }
}
