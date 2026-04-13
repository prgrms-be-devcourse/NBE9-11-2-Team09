package com.example.parking.domain.user.service;

import com.example.parking.domain.user.dto.SignupReqDto;
import com.example.parking.domain.user.dto.UserProfileResDto;
import com.example.parking.domain.user.entity.User;
import com.example.parking.domain.user.repository.UserRepository;
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

}
