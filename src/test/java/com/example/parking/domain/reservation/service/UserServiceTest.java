package com.example.parking.domain.reservation.service;

import com.example.parking.domain.user.dto.LoginReqDto;
import com.example.parking.domain.user.dto.LoginResDto;
import com.example.parking.domain.user.dto.RefreshTokenReqDto;
import com.example.parking.domain.user.dto.WithdrawReqDto;
import com.example.parking.domain.user.entity.RefreshToken;
import com.example.parking.domain.user.entity.User;
import com.example.parking.domain.user.entity.UserRole;
import com.example.parking.domain.user.entity.UserStatus;
import com.example.parking.domain.user.entity.VehicleType;
import com.example.parking.domain.user.repository.RefreshTokenRepository;
import com.example.parking.domain.user.repository.UserRepository;
import com.example.parking.domain.user.service.UserService;
import com.example.parking.global.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("로그인 성공 시 access token과 refresh token을 발급하고 refresh token을 저장한다")
    void login_issues_tokens() {
        // given
        User savedUser = userRepository.save(User.builder()
                .email("user1@test.com")
                .password(passwordEncoder.encode("test1234"))
                .name("강아지")
                .plateNumber("12가3456")
                .vehicleType(VehicleType.SMALL)
                .role(UserRole.USER)
                .build());

        LoginReqDto reqDto = new LoginReqDto();
        ReflectionTestUtils.setField(reqDto, "userEmail", "user1@test.com");
        ReflectionTestUtils.setField(reqDto, "password", "test1234");

        // when
        LoginResDto result = userService.login(reqDto);

        // then
        assertThat(result.getAccessToken()).isNotBlank();
        assertThat(result.getRefreshToken()).isNotBlank();
        assertThat(result.getTokenType()).isEqualTo("Bearer");

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(savedUser.getId()).orElseThrow();
        assertThat(refreshToken.getToken()).isEqualTo(result.getRefreshToken());
        assertThat(jwtUtil.getTokenType(result.getAccessToken())).isEqualTo("access");
        assertThat(jwtUtil.getTokenType(result.getRefreshToken())).isEqualTo("refresh");
    }

    @Test
    @DisplayName("refresh token으로 access token을 재발급할 수 있다")
    void refresh_reissues_access_token() {
        // given
        userRepository.save(User.builder()
                .email("user2@test.com")
                .password(passwordEncoder.encode("test1234"))
                .name("고양이")
                .plateNumber("34나5678")
                .vehicleType(VehicleType.LARGE)
                .role(UserRole.USER)
                .build());

        LoginReqDto loginReqDto = new LoginReqDto();
        ReflectionTestUtils.setField(loginReqDto, "userEmail", "user2@test.com");
        ReflectionTestUtils.setField(loginReqDto, "password", "test1234");

        LoginResDto loginResult = userService.login(loginReqDto);

        RefreshTokenReqDto refreshTokenReqDto = new RefreshTokenReqDto();
        ReflectionTestUtils.setField(refreshTokenReqDto, "refreshToken", loginResult.getRefreshToken());

        // when
        LoginResDto refreshResult = userService.refresh(refreshTokenReqDto);

        // then
        assertThat(refreshResult.getAccessToken()).isNotBlank();
        assertThat(refreshResult.getRefreshToken()).isEqualTo(loginResult.getRefreshToken());
        assertThat(jwtUtil.getTokenType(refreshResult.getAccessToken())).isEqualTo("access");
    }

    @Test
    @DisplayName("로그아웃하면 refresh token이 삭제된다")
    void logout_deletes_refresh_token() {
        // given
        User savedUser = userRepository.save(User.builder()
                .email("user3@test.com")
                .password(passwordEncoder.encode("test1234"))
                .name("호랑이")
                .plateNumber("56다7890")
                .vehicleType(VehicleType.ELECTRIC)
                .role(UserRole.USER)
                .build());

        LoginReqDto loginReqDto = new LoginReqDto();
        ReflectionTestUtils.setField(loginReqDto, "userEmail", "user3@test.com");
        ReflectionTestUtils.setField(loginReqDto, "password", "test1234");

        userService.login(loginReqDto);

        assertThat(refreshTokenRepository.findByUserId(savedUser.getId())).isPresent();

        // when
        userService.logout(savedUser.getId());

        // then
        assertThat(refreshTokenRepository.findByUserId(savedUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("회원탈퇴하면 상태가 WITHDRAW로 바뀌고 refresh token이 삭제된다")
    void withdraw_updates_status_and_deletes_refresh_token() {
        // given
        User savedUser = userRepository.save(User.builder()
                .email("user4@test.com")
                .password(passwordEncoder.encode("test1234"))
                .name("도마뱀")
                .plateNumber("78라1234")
                .vehicleType(VehicleType.SMALL)
                .role(UserRole.USER)
                .build());

        LoginReqDto loginReqDto = new LoginReqDto();
        ReflectionTestUtils.setField(loginReqDto, "userEmail", "user4@test.com");
        ReflectionTestUtils.setField(loginReqDto, "password", "test1234");

        userService.login(loginReqDto);

        WithdrawReqDto withdrawReqDto = new WithdrawReqDto();
        ReflectionTestUtils.setField(withdrawReqDto, "password", "test1234");

        // when
        userService.withdraw(savedUser.getId(), withdrawReqDto);

        // then
        User withdrawnUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(withdrawnUser.getStatus()).isEqualTo(UserStatus.WITHDRAW);
        assertThat(refreshTokenRepository.findByUserId(savedUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("로그아웃 후에는 같은 refresh token으로 재발급할 수 없다")
    void refresh_fails_after_logout() {
        // given
        User savedUser = userRepository.save(User.builder()
                .email("user5@test.com")
                .password(passwordEncoder.encode("test1234"))
                .name("토끼")
                .plateNumber("90마5678")
                .vehicleType(VehicleType.LARGE)
                .role(UserRole.USER)
                .build());

        LoginReqDto loginReqDto = new LoginReqDto();
        ReflectionTestUtils.setField(loginReqDto, "userEmail", "user5@test.com");
        ReflectionTestUtils.setField(loginReqDto, "password", "test1234");

        LoginResDto loginResult = userService.login(loginReqDto);

        userService.logout(savedUser.getId());

        RefreshTokenReqDto refreshTokenReqDto = new RefreshTokenReqDto();
        ReflectionTestUtils.setField(refreshTokenReqDto, "refreshToken", loginResult.getRefreshToken());

        // when & then
        assertThatThrownBy(() -> userService.refresh(refreshTokenReqDto))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
