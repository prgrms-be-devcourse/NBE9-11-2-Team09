package com.example.parking.domain.user.service;

import java.util.regex.Pattern;
import com.example.parking.domain.user.dto.*;
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
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public EmailCheckResDto checkEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }

        boolean exists = userRepository.existsByEmail(email);

        if (exists) {
            return new EmailCheckResDto(false, "이미 사용 중인 이메일입니다.");
        }

        return new EmailCheckResDto(true, "사용 가능한 이메일입니다.");
    }

    @Transactional
    public UserProfileResDto signup(SignupReqDto reqDto) {
        if(userRepository.existsByEmail(reqDto.getUserEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if(userRepository.existsByPlateNumber(reqDto.getPlateNumber())) {
            throw new IllegalArgumentException("이미 등록된 차량 번호입니다.");
        }

        User user = User.builder()
                .email(reqDto.getUserEmail())
                // [CUS-06] 회원가입 - 비밀번호는 저장 전에 BCrypt로 암호화
                .password(passwordEncoder.encode(reqDto.getPassword()))
                .name(reqDto.getName())
                .plateNumber(reqDto.getPlateNumber())
                .vehicleType(reqDto.getVehicleType())
                .build();

        User savedUser = userRepository.save(user);
        return UserProfileResDto.from(savedUser);
    }

    public UserProfileResDto getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("탈퇴한 사용자는 조회할 수 없습니다.");
        }

        return UserProfileResDto.from(user);
    }

    // [CUS-10] 내 차량 정보 수정 - JWT로 인증된 현재 사용자의 차량 번호와 차량 종류 수정
    @Transactional
    public UserProfileResDto updateMyVehicle(Long userId, VehicleUpdateReqDto reqDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("탈퇴한 사용자는 수정할 수 없습니다.");
        }

        if (userRepository.existsByPlateNumberAndIdNot(reqDto.getPlateNumber(), userId)) {
            throw new IllegalArgumentException("이미 등록된 차량 번호입니다.");
        }

        user.updateVehicleInfo(reqDto.getPlateNumber(), reqDto.getVehicleType());

        return UserProfileResDto.from(user);
    }

    // [CUS 07] 회원탈퇴 - 인증된 사용자의 비밀번호를 다시 확인한 뒤 soft delete 처리
    @Transactional
    public void withdraw(Long userId, WithdrawReqDto reqDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("이미 탈퇴한 사용자입니다.");
        }

        if (!passwordEncoder.matches(reqDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        user.withdraw();

        // 탈퇴 후 재발급 경로를 막기 위해 refresh token도 함께 삭제
        refreshTokenRepository.deleteByUserId(userId);
    }
}
