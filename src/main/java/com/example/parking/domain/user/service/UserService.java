package com.example.parking.domain.user.service;

import com.example.parking.domain.user.dto.LoginReqDto;
import com.example.parking.domain.user.dto.LoginResDto;
import com.example.parking.domain.user.dto.SignupReqDto;
import com.example.parking.domain.user.dto.UserProfileResDto;
import com.example.parking.domain.user.entity.User;
import com.example.parking.domain.user.entity.UserStatus;
import com.example.parking.domain.user.repository.UserRepository;
import com.example.parking.global.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
    // [CUS-08] 로그인 - 로그인 요청을 받아 사용자 인증 후 JWT 토큰 발급
    public LoginResDto login(LoginReqDto reqDto) {
        User user = userRepository.findByEmail(reqDto.getUserEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(reqDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("탈퇴한 사용자는 로그인할 수 없습니다.");
        }

        String accessToken = jwtUtil.createAccessToken(user);
        return new LoginResDto(accessToken, "Bearer");
    }

    public UserProfileResDto getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("탈퇴한 사용자는 조회할 수 없습니다.");
        }

        return UserProfileResDto.from(user);
    }
}
