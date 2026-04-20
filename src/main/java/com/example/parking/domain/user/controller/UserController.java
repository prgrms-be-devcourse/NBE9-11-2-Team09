package com.example.parking.domain.user.controller;

import com.example.parking.domain.user.dto.*;
import com.example.parking.domain.user.service.AuthService;
import com.example.parking.domain.user.service.UserService;
import com.example.parking.global.response.RsData;
import com.example.parking.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "회원", description = "회원 관련 API")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    // [CUS-06] 이메일 중복 체크 - 회원가입 시 이메일 중복 여부를 확인하는 API
    @Operation(summary = "이메일 중복 확인", description = "회원가입 시 이메일 중복 여부를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인 완료"),
            @ApiResponse(responseCode = "400", description = "이메일 형식 오류")
    })
    @GetMapping("/api/users/check-email")
    public ResponseEntity<EmailCheckResDto> checkEmail(@RequestParam String email) {
        EmailCheckResDto response = userService.checkEmail(email);
        return ResponseEntity.ok(response);
    }

    // [CUS-06] 회원가입 - 회원가입 요청을 받아 사용자 정보 저장
    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 이름, 차량번호, 차종을 입력하여 회원가입합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 완료"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 정보"),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일 또는 차량번호")
    })
    @PostMapping("/api/users/signup")
    public ResponseEntity<RsData<UserProfileResDto>> signup(@Valid @RequestBody SignupReqDto reqDto) {
        UserProfileResDto data = userService.signup(reqDto);
        RsData<UserProfileResDto> rsData = new RsData<>("회원가입이 완료되었습니다.", "200-1", data);
        return ResponseEntity.ok(rsData);
    }

    // [CUS-08] 로그인 - 로그인 요청을 받아 사용자 인증 후 JWT 토큰 발급
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 완료"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 이메일 또는 비밀번호 불일치"),
            @ApiResponse(responseCode = "409", description = "탈퇴한 사용자")
    })
    @PostMapping("/api/users/login")
    public ResponseEntity<RsData<LoginResDto>> login(@Valid @RequestBody LoginReqDto reqDto) {
        LoginResDto data = authService.login(reqDto);
        RsData<LoginResDto> rsData = new RsData<>("로그인이 완료되었습니다.", "200-2", data);
        return ResponseEntity.ok(rsData);
    }

    // access token 만료 후 refresh token으로 새 access token을 발급받는 API
    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 새로운 Access Token을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 완료"),
            @ApiResponse(responseCode = "400", description = "유효하지 않거나 저장되지 않은 Refresh Token")
    })
    @PostMapping("/api/users/refresh")
    public ResponseEntity<RsData<LoginResDto>> refresh(@Valid @RequestBody RefreshTokenReqDto reqDto) {
        LoginResDto data = authService.refresh(reqDto);
        RsData<LoginResDto> rsData = new RsData<>("토큰 재발급이 완료되었습니다.", "200-3", data);
        return ResponseEntity.ok(rsData);
    }

    // [CUS-08] 로그인 - 내 정보 조회 - JWT로 인증된 현재 사용자의 회원 정보 조회

    @Operation(summary = "내 정보 조회", description = "JWT로 인증된 현재 사용자의 프로필을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 완료"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 사용자"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/api/users/me")
    public ResponseEntity<RsData<UserProfileResDto>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserProfileResDto data = userService.getMyProfile(userDetails.getUserId());
        RsData<UserProfileResDto> rsData = new RsData<>("내 정보 조회가 완료되었습니다.", "200-4", data);
        return ResponseEntity.ok(rsData);
    }

    // [CUS-10] 차량 정보 수정 - JWT로 인증된 현재 사용자의 차량 정보 업데이트

    @Operation(summary = "차량 정보 수정", description = "JWT로 인증된 현재 사용자의 차량번호와 차종을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 완료"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 사용자"),
            @ApiResponse(responseCode = "409", description = "이미 등록된 차량번호")
    })
    @PatchMapping("/api/users/me/vehicle")
    public ResponseEntity<RsData<UserProfileResDto>> updateMyVehicle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody VehicleUpdateReqDto reqDto
    ) {
        UserProfileResDto data = userService.updateMyVehicle(userDetails.getUserId(), reqDto);
        RsData<UserProfileResDto> rsData = new RsData<>("차량 정보 수정이 완료되었습니다.", "200-5", data);
        return ResponseEntity.ok(rsData);
    }

    // [CUS-07] 회원 탈퇴 - JWT로 인증된 현재 사용자의 계정을 탈퇴 처리
    @Operation(summary = "회원 탈퇴", description = "비밀번호를 확인한 후 계정을 탈퇴 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 완료"),
            @ApiResponse(responseCode = "400", description = "비밀번호 불일치"),
            @ApiResponse(responseCode = "409", description = "이미 탈퇴한 사용자")
    })
    @DeleteMapping("/api/users/me")
    public ResponseEntity<RsData<Void>> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WithdrawReqDto reqDto
    ) {
        userService.withdraw(userDetails.getUserId(), reqDto);
        RsData<Void> rsData = new RsData<>("회원 탈퇴가 완료되었습니다.", "200-6");
        return ResponseEntity.ok(rsData);
    }

    // 로그아웃 - JWT로 인증된 현재 사용자의 세션을 무효화하여 로그아웃 처리
    @Operation(summary = "로그아웃", description = "Refresh Token을 삭제하여 로그아웃 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 완료"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 사용자"),
            @ApiResponse(responseCode = "409", description = "탈퇴한 사용자")
    })
    @PostMapping("/api/users/logout")
    public ResponseEntity<RsData<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authService.logout(userDetails.getUserId());
        RsData<Void> rsData = new RsData<>("로그아웃이 완료되었습니다.", "200-7");
        return ResponseEntity.ok(rsData);
    }
}