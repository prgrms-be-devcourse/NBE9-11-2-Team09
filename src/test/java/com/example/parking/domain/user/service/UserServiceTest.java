package com.example.parking.domain.user.service;

import com.example.parking.domain.user.dto.EmailCheckResDto;
import com.example.parking.domain.user.dto.SignupReqDto;
import com.example.parking.domain.user.dto.UserProfileResDto;
import com.example.parking.domain.user.dto.VehicleUpdateReqDto;
import com.example.parking.domain.user.dto.WithdrawReqDto;
import com.example.parking.domain.user.entity.User;
import com.example.parking.domain.user.entity.UserRole;
import com.example.parking.domain.user.entity.UserStatus;
import com.example.parking.domain.user.entity.VehicleType;
import com.example.parking.domain.user.repository.RefreshTokenRepository;
import com.example.parking.domain.user.repository.UserRepository;
import com.example.parking.domain.user.dto.LoginReqDto;
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
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("사용 가능한 이메일이면 available=true를 반환한다")
    void check_email_returns_available_true() {
        // when
        EmailCheckResDto result = userService.checkEmail("available@test.com");

        // then
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getMessage()).isEqualTo("사용 가능한 이메일입니다.");
    }

    @Test
    @DisplayName("이미 존재하는 이메일이면 available=false를 반환한다")
    void check_email_returns_available_false_when_email_exists() {
        // given
        createUser(
                "exists@test.com",
                "test1234",
                "홍길동",
                "12가3456",
                VehicleType.SMALL
        );

        // when
        EmailCheckResDto result = userService.checkEmail("exists@test.com");

        // then
        assertThat(result.isAvailable()).isFalse();
        assertThat(result.getMessage()).isEqualTo("이미 사용 중인 이메일입니다.");
    }

    @Test
    @DisplayName("회원가입 성공 시 사용자를 저장하고 비밀번호를 암호화한다")
    void signup_saves_user() {
        // given
        SignupReqDto reqDto = createSignupReqDto(
                "signup1@test.com",
                "test1234",
                "김철수",
                "23나4567",
                VehicleType.LARGE
        );

        // when
        UserProfileResDto result = userService.signup(reqDto);

        // then
        User savedUser = userRepository.findByEmail("signup1@test.com").orElseThrow();

        assertThat(result.getUserId()).isEqualTo(savedUser.getId());
        assertThat(result.getUserEmail()).isEqualTo("signup1@test.com");
        assertThat(result.getUserName()).isEqualTo("김철수");
        assertThat(result.getPlateNumber()).isEqualTo("23나4567");
        assertThat(result.getVehicleType()).isEqualTo(VehicleType.LARGE);
        assertThat(result.getRole()).isEqualTo(UserRole.USER);

        assertThat(savedUser.getName()).isEqualTo("김철수");
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(passwordEncoder.matches("test1234", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("회원가입 시 이메일이 중복되면 실패한다")
    void signup_fails_when_email_exists() {
        // given
        createUser(
                "duplicate@test.com",
                "test1234",
                "이영희",
                "34다5678",
                VehicleType.SMALL
        );

        SignupReqDto reqDto = createSignupReqDto(
                "duplicate@test.com",
                "test5678",
                "박민수",
                "45라6789",
                VehicleType.ELECTRIC
        );

        // when & then
        assertThatThrownBy(() -> userService.signup(reqDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("회원가입 시 차량번호가 중복되면 실패한다")
    void signup_fails_when_plate_number_exists() {
        // given
        createUser(
                "plate-owner@test.com",
                "test1234",
                "최지은",
                "56마7890",
                VehicleType.LARGE
        );

        SignupReqDto reqDto = createSignupReqDto(
                "newuser@test.com",
                "test5678",
                "정수진",
                "56마7890",
                VehicleType.SMALL
        );

        // when & then
        assertThatThrownBy(() -> userService.signup(reqDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("ACTIVE 사용자는 내 정보를 조회할 수 있다")
    void get_my_profile_returns_user_info() {
        // given
        User savedUser = createUser(
                "profile@test.com",
                "test1234",
                "한지민",
                "67바8901",
                VehicleType.ELECTRIC
        );

        // when
        UserProfileResDto result = userService.getMyProfile(savedUser.getId());

        // then
        assertThat(result.getUserId()).isEqualTo(savedUser.getId());
        assertThat(result.getUserEmail()).isEqualTo("profile@test.com");
        assertThat(result.getUserName()).isEqualTo("한지민");
        assertThat(result.getPlateNumber()).isEqualTo("67바8901");
        assertThat(result.getVehicleType()).isEqualTo(VehicleType.ELECTRIC);
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("탈퇴한 사용자는 내 정보를 조회할 수 없다")
    void get_my_profile_fails_when_user_is_withdrawn() {
        // given
        User withdrawnUser = createUser(
                "withdrawn-profile@test.com",
                "test1234",
                "유재석",
                "78사9012",
                VehicleType.SMALL
        );
        withdrawnUser.withdraw();

        // when & then
        assertThatThrownBy(() -> userService.getMyProfile(withdrawnUser.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("차량 정보 수정 시 차량번호와 차량종류가 변경된다")
    void update_my_vehicle_updates_plate_and_type() {
        // given
        User savedUser = createUser(
                "vehicle@test.com",
                "test1234",
                "강호동",
                "89아0123",
                VehicleType.SMALL
        );

        VehicleUpdateReqDto reqDto = createVehicleUpdateReqDto(
                "99가9999",
                VehicleType.ELECTRIC
        );

        // when
        UserProfileResDto result = userService.updateMyVehicle(savedUser.getId(), reqDto);

        // then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(result.getPlateNumber()).isEqualTo("99가9999");
        assertThat(result.getVehicleType()).isEqualTo(VehicleType.ELECTRIC);

        assertThat(updatedUser.getPlateNumber()).isEqualTo("99가9999");
        assertThat(updatedUser.getVehicleType()).isEqualTo(VehicleType.ELECTRIC);
    }

    @Test
    @DisplayName("차량 정보 수정 시 다른 사용자의 차량번호와 중복되면 실패한다")
    void update_my_vehicle_fails_when_plate_number_exists() {
        // given
        User targetUser = createUser(
                "target@test.com",
                "test1234",
                "신동엽",
                "10가1010",
                VehicleType.SMALL
        );

        createUser(
                "other@test.com",
                "test1234",
                "서현진",
                "20나2020",
                VehicleType.LARGE
        );

        VehicleUpdateReqDto reqDto = createVehicleUpdateReqDto(
                "20나2020",
                VehicleType.ELECTRIC
        );

        // when & then
        assertThatThrownBy(() -> userService.updateMyVehicle(targetUser.getId(), reqDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("회원탈퇴 시 상태가 WITHDRAW로 변경되고 refresh token이 삭제된다")
    void withdraw_updates_status_and_deletes_refresh_token() {
        // given
        User savedUser = createUser(
                "withdraw@test.com",
                "test1234",
                "남주혁",
                "30다3030",
                VehicleType.SMALL
        );

        LoginReqDto loginReqDto = createLoginReqDto("withdraw@test.com", "test1234");
        authService.login(loginReqDto);

        assertThat(refreshTokenRepository.findByUserId(savedUser.getId())).isPresent();

        WithdrawReqDto reqDto = createWithdrawReqDto("test1234");

        // when
        userService.withdraw(savedUser.getId(), reqDto);

        // then
        User withdrawnUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(withdrawnUser.getStatus()).isEqualTo(UserStatus.WITHDRAW);
        assertThat(refreshTokenRepository.findByUserId(savedUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("회원탈퇴 시 비밀번호가 일치하지 않으면 실패한다")
    void withdraw_fails_when_password_is_wrong() {
        // given
        User savedUser = createUser(
                "withdraw-fail@test.com",
                "test1234",
                "배수지",
                "40라4040",
                VehicleType.LARGE
        );

        WithdrawReqDto reqDto = createWithdrawReqDto("wrong1234");

        // when & then
        assertThatThrownBy(() -> userService.withdraw(savedUser.getId(), reqDto))
                .isInstanceOf(IllegalArgumentException.class);

        User notWithdrawnUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(notWithdrawnUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
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

    private SignupReqDto createSignupReqDto(
            String email,
            String password,
            String name,
            String plateNumber,
            VehicleType vehicleType
    ) {
        SignupReqDto reqDto = new SignupReqDto();
        ReflectionTestUtils.setField(reqDto, "userEmail", email);
        ReflectionTestUtils.setField(reqDto, "password", password);
        ReflectionTestUtils.setField(reqDto, "name", name);
        ReflectionTestUtils.setField(reqDto, "plateNumber", plateNumber);
        ReflectionTestUtils.setField(reqDto, "vehicleType", vehicleType);
        return reqDto;
    }

    private VehicleUpdateReqDto createVehicleUpdateReqDto(String plateNumber, VehicleType vehicleType) {
        VehicleUpdateReqDto reqDto = new VehicleUpdateReqDto();
        ReflectionTestUtils.setField(reqDto, "plateNumber", plateNumber);
        ReflectionTestUtils.setField(reqDto, "vehicleType", vehicleType);
        return reqDto;
    }

    private WithdrawReqDto createWithdrawReqDto(String password) {
        WithdrawReqDto reqDto = new WithdrawReqDto();
        ReflectionTestUtils.setField(reqDto, "password", password);
        return reqDto;
    }

    private LoginReqDto createLoginReqDto(String email, String password) {
        LoginReqDto reqDto = new LoginReqDto();
        ReflectionTestUtils.setField(reqDto, "userEmail", email);
        ReflectionTestUtils.setField(reqDto, "password", password);
        return reqDto;
    }
}
