package com.example.parking.domain.user.service;

import com.example.parking.domain.user.dto.LoginReqDto;
import com.example.parking.domain.user.dto.LoginResDto;
import com.example.parking.domain.user.dto.RefreshTokenReqDto;
import com.example.parking.domain.user.entity.RefreshToken;
import com.example.parking.domain.user.entity.User;
import com.example.parking.domain.user.entity.UserRole;
import com.example.parking.domain.user.entity.UserStatus;
import com.example.parking.domain.user.entity.VehicleType;
import com.example.parking.domain.user.repository.RefreshTokenRepository;
import com.example.parking.domain.user.repository.UserRepository;
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

public class AuthServiceTest {

    @Autowired
    private AuthService authService;

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
        User savedUser = createUser(
                "auth1@test.com",
                "test1234",
                "홍길동",
                "12가3456",
                VehicleType.SMALL
        );

        LoginReqDto reqDto = createLoginReqDto("auth1@test.com", "test1234");

        // when
        LoginResDto result = authService.login(reqDto);

        // then
        assertThat(result.getAccessToken()).isNotBlank();
        assertThat(result.getRefreshToken()).isNotBlank();
        assertThat(result.getTokenType()).isEqualTo("Bearer");

        RefreshToken savedRefreshToken = refreshTokenRepository.findByUserId(savedUser.getId()).orElseThrow();
        assertThat(savedRefreshToken.getToken()).isEqualTo(result.getRefreshToken());
        assertThat(jwtUtil.getTokenType(result.getAccessToken())).isEqualTo("access");
        assertThat(jwtUtil.getTokenType(result.getRefreshToken())).isEqualTo("refresh");
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 로그인할 수 없다")
    void login_fails_when_password_is_wrong() {
        // given
        createUser(
                "auth2@test.com",
                "test1234",
                "김철수",
                "23나4567",
                VehicleType.LARGE
        );

        LoginReqDto reqDto = createLoginReqDto("auth2@test.com", "wrong1234");

        // when & then
        assertThatThrownBy(() -> authService.login(reqDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("탈퇴한 사용자는 로그인할 수 없다")
    void login_fails_when_user_is_withdrawn() {
        // given
        User withdrawnUser = createUser(
                "auth3@test.com",
                "test1234",
                "이영희",
                "34다5678",
                VehicleType.ELECTRIC
        );
        withdrawnUser.withdraw();

        LoginReqDto reqDto = createLoginReqDto("auth3@test.com", "test1234");

        // when & then
        assertThatThrownBy(() -> authService.login(reqDto))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(withdrawnUser.getStatus()).isEqualTo(UserStatus.WITHDRAW);
    }

    @Test
    @DisplayName("유효한 refresh token으로 access token을 재발급할 수 있다")
    void refresh_reissues_access_token() {
        // given
        createUser(
                "auth4@test.com",
                "test1234",
                "박민수",
                "45라6789",
                VehicleType.SMALL
        );

        LoginReqDto loginReqDto = createLoginReqDto("auth4@test.com", "test1234");
        LoginResDto loginResult = authService.login(loginReqDto);

        RefreshTokenReqDto refreshReqDto = createRefreshTokenReqDto(loginResult.getRefreshToken());

        // when
        LoginResDto refreshResult = authService.refresh(refreshReqDto);

        // then
        assertThat(refreshResult.getAccessToken()).isNotBlank();
        assertThat(refreshResult.getRefreshToken()).isEqualTo(loginResult.getRefreshToken());
        assertThat(jwtUtil.getTokenType(refreshResult.getAccessToken())).isEqualTo("access");
        assertThat(jwtUtil.getTokenType(refreshResult.getRefreshToken())).isEqualTo("refresh");
    }

    @Test
    @DisplayName("DB에 저장되지 않은 refresh token으로는 재발급할 수 없다")
    void refresh_fails_when_token_is_not_saved() {
        // given
        User user = createUser(
                "auth5@test.com",
                "test1234",
                "최지은",
                "56마7890",
                VehicleType.LARGE
        );

        String unsavedRefreshToken = jwtUtil.createRefreshToken(user);
        RefreshTokenReqDto refreshReqDto = createRefreshTokenReqDto(unsavedRefreshToken);

        // when & then
        assertThatThrownBy(() -> authService.refresh(refreshReqDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("access token으로는 재발급할 수 없다")
    void refresh_fails_when_token_type_is_access() {
        // given
        createUser(
                "auth6@test.com",
                "test1234",
                "정수진",
                "67바8901",
                VehicleType.SMALL
        );

        LoginReqDto loginReqDto = createLoginReqDto("auth6@test.com", "test1234");
        LoginResDto loginResult = authService.login(loginReqDto);

        RefreshTokenReqDto refreshReqDto = createRefreshTokenReqDto(loginResult.getAccessToken());

        // when & then
        assertThatThrownBy(() -> authService.refresh(refreshReqDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("로그아웃하면 저장된 refresh token이 삭제된다")
    void logout_deletes_refresh_token() {
        // given
        User savedUser = createUser(
                "auth7@test.com",
                "test1234",
                "한지민",
                "78사9012",
                VehicleType.ELECTRIC
        );

        LoginReqDto loginReqDto = createLoginReqDto("auth7@test.com", "test1234");
        authService.login(loginReqDto);

        assertThat(refreshTokenRepository.findByUserId(savedUser.getId())).isPresent();

        // when
        authService.logout(savedUser.getId());

        // then
        assertThat(refreshTokenRepository.findByUserId(savedUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("같은 사용자에 대한 refresh token 저장 요청이 오면 기존 토큰을 갱신한다")
    void save_or_update_refresh_token_updates_existing_token() {
        // given
        User savedUser = createUser(
                "auth8@test.com",
                "test1234",
                "유재석",
                "89아0123",
                VehicleType.SMALL
        );

        String firstRefreshToken = jwtUtil.createRefreshToken(savedUser);
        authService.saveOrUpdateRefreshToken(savedUser.getId(), firstRefreshToken);

        RefreshToken firstSavedToken = refreshTokenRepository.findByUserId(savedUser.getId()).orElseThrow();
        Long firstTokenId = firstSavedToken.getId();

        String secondRefreshToken = jwtUtil.createRefreshToken(savedUser);

        // when
        authService.saveOrUpdateRefreshToken(savedUser.getId(), secondRefreshToken);

        // then
        RefreshToken updatedToken = refreshTokenRepository.findByUserId(savedUser.getId()).orElseThrow();
        assertThat(updatedToken.getId()).isEqualTo(firstTokenId);
        assertThat(updatedToken.getToken()).isEqualTo(secondRefreshToken);
    }

    private User createUser(
            String email,
            String rawPassword,
            String name,
            String plateNumber,
            VehicleType vehicleType
    ) {
        return userRepository.save(
                User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(rawPassword))
                        .name(name)
                        .plateNumber(plateNumber)
                        .vehicleType(vehicleType)
                        .role(UserRole.USER)
                        .build()
        );
    }

    private LoginReqDto createLoginReqDto(String email, String password) {
        LoginReqDto reqDto = new LoginReqDto();
        ReflectionTestUtils.setField(reqDto, "userEmail", email);
        ReflectionTestUtils.setField(reqDto, "password", password);
        return reqDto;
    }

    private RefreshTokenReqDto createRefreshTokenReqDto(String refreshToken) {
        RefreshTokenReqDto reqDto = new RefreshTokenReqDto();
        ReflectionTestUtils.setField(reqDto, "refreshToken", refreshToken);
        return reqDto;
    }
}
