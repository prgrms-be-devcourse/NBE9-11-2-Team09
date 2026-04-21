package com.example.parking.domain.admin.user.service;

import com.example.parking.domain.admin.user.dto.AdminUserResDto;
import com.example.parking.domain.user.entity.User;
import com.example.parking.domain.user.entity.UserRole;
import com.example.parking.domain.user.entity.VehicleType;
import com.example.parking.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class AdminUserServiceTest {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원 목록 조회 시 USER 계정만 반환한다")
    void get_admin_users_returns_only_user_role_accounts() {
        // given
        createUser("TDDuser1@test.com", "TDD홍길동", "12가3456", VehicleType.SMALL, UserRole.USER);
        createUser("TDDuser2@test.com", "TDD김철수", "23나4567", VehicleType.LARGE, UserRole.USER);
        createUser("TDDadmin@test.com", "TDD관리자", "99관9999", VehicleType.SMALL, UserRole.ADMIN);

        PageRequest pageable = PageRequest.of(0, 20);

        // when
        Page<AdminUserResDto> result = adminUserService.getAdminUsers("TDD", PageRequest.of(0, 20));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(AdminUserResDto::getUserEmail)
                .containsExactlyInAnyOrder("TDDuser1@test.com", "TDDuser2@test.com");

        assertThat(result.getContent())
                .extracting(AdminUserResDto::getRole)
                .containsOnly(UserRole.USER);
    }

    @Test
    @DisplayName("이름 키워드로 회원 목록을 검색할 수 있다")
    void get_admin_users_filters_by_name_keyword() {
        // given
        createUser("hong1@test.com", "홍길동", "34다5678", VehicleType.SMALL, UserRole.USER);
        createUser("hong2@test.com", "홍길순", "45라6789", VehicleType.ELECTRIC, UserRole.USER);
        createUser("kim@test.com", "김영희", "56마7890", VehicleType.LARGE, UserRole.USER);
        createUser("admin-hong@test.com", "홍관리", "00관0001", VehicleType.SMALL, UserRole.ADMIN);

        PageRequest pageable = PageRequest.of(0, 20);

        // when
        Page<AdminUserResDto> result = adminUserService.getAdminUsers("홍", pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(AdminUserResDto::getUserName)
                .containsExactlyInAnyOrder("홍길동", "홍길순");
        assertThat(result.getContent())
                .extracting(AdminUserResDto::getRole)
                .containsOnly(UserRole.USER);
    }

    @Test
    @DisplayName("이메일 키워드로 회원 목록을 검색할 수 있다")
    void get_admin_users_filters_by_email_keyword() {
        // given
        createUser("alpha.user@test.com", "사용자1", "67바8901", VehicleType.SMALL, UserRole.USER);
        createUser("beta.user@test.com", "사용자2", "78사9012", VehicleType.LARGE, UserRole.USER);
        createUser("gamma@test.com", "사용자3", "89아0123", VehicleType.ELECTRIC, UserRole.USER);

        PageRequest pageable = PageRequest.of(0, 20);

        // when
        Page<AdminUserResDto> result = adminUserService.getAdminUsers("beta", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserEmail()).isEqualTo("beta.user@test.com");
        assertThat(result.getContent().get(0).getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("검색 키워드가 일치해도 ADMIN 계정은 회원 목록에서 제외한다")
    void get_admin_users_does_not_return_admin_accounts_even_when_keyword_matches() {
        // given
        createUser("member-admin-keyword@test.com", "관리회원", "90자1234", VehicleType.SMALL, UserRole.USER);
        createUser("admin-keyword@test.com", "관리자키워드", "00관1234", VehicleType.SMALL, UserRole.ADMIN);

        PageRequest pageable = PageRequest.of(0, 20);

        // when
        Page<AdminUserResDto> result = adminUserService.getAdminUsers("admin", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserEmail()).isEqualTo("member-admin-keyword@test.com");
        assertThat(result.getContent().get(0).getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("회원 목록 조회에 pageable이 적용된다")
    void get_admin_users_applies_paging() {
        // given
        for (int i = 1; i <= 5; i++) {
            createUser(
                    "TDDpaging" + i + "@test.com",
                    "TDD회원" + i,
                    "10가10" + i,
                    VehicleType.SMALL,
                    UserRole.USER
            );
        }

        PageRequest pageable = PageRequest.of(0, 2);

        // when
        Page<AdminUserResDto> result = adminUserService.getAdminUsers("TDD", pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(5L);
        assertThat(result.getTotalPages()).isEqualTo(3);

        assertThat(result.getContent())
                .extracting(AdminUserResDto::getUserName)
                .allMatch(name -> name.startsWith("TDD"));
    }

    private User createUser(
            String email,
            String name,
            String plateNumber,
            VehicleType vehicleType,
            UserRole role
    ) {
        return userRepository.save(
                User.builder()
                        .email(email)
                        .password(passwordEncoder.encode("test1234"))
                        .name(name)
                        .plateNumber(plateNumber)
                        .vehicleType(vehicleType)
                        .role(role)
                        .build()
        );
    }
}
