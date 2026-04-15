package com.example.parking.domain.user.service;

import com.example.parking.domain.admin.user.dto.AdminUserResDto;
import com.example.parking.domain.user.dto.*;
import com.example.parking.domain.user.entity.User;
import com.example.parking.domain.user.entity.UserStatus;
import com.example.parking.domain.user.repository.UserRepository;
import com.example.parking.global.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    // [CUS-08] 로그인 - 로그인 요청을 받아 사용자 인증 후 JWT access token 발급
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

    // [ADM-05] 관리자 권한으로 전체 고객 목록 조회 - 이름 또는 이메일 키워드로 검색 가능, 페이징 처리
    public Page<AdminUserResDto> getAdminUsers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return userRepository.findAll(pageable)
                    .map(AdminUserResDto::from);
        }

        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword,
                        keyword,
                        pageable
                )
                .map(AdminUserResDto::from);
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
    }

    // 로그아웃 - 클라이언트가 access token을 삭제하는 방식으로 로그아웃 처리 (서버에서는 별도의 상태 관리 없이 JWT의 유효성 검증으로 처리)
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("탈퇴한 사용자는 로그아웃할 수 없습니다.");
        }
    }
}
