package com.example.parking.domain.user.controller;

import com.example.parking.domain.user.dto.*;
import com.example.parking.domain.user.service.UserService;
import com.example.parking.global.response.RsData;
import com.example.parking.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // [CUS-06] 회원가입 - 회원가입 요청을 받아 사용자 정보 저장
    @PostMapping("/api/users/signup")
    public ResponseEntity<RsData<UserProfileResDto>> signup(@Valid @RequestBody SignupReqDto reqDto) {
        UserProfileResDto data = userService.signup(reqDto);
        RsData<UserProfileResDto> rsData = new RsData<>("회원가입이 완료되었습니다.", "200-1", data);
        return ResponseEntity.ok(rsData);
    }

    // [CUS-08] 로그인 - 로그인 요청을 받아 사용자 인증 후 JWT 토큰 발급
    @PostMapping("/api/users/login")
    public ResponseEntity<RsData<LoginResDto>> login(@Valid @RequestBody LoginReqDto reqDto) {
        LoginResDto data = userService.login(reqDto);
        RsData<LoginResDto> rsData = new RsData<>("로그인이 완료되었습니다.", "200-2", data);
        return ResponseEntity.ok(rsData);
    }

    // access token 만료 후 refresh token으로 새 access token을 발급받는 API
    @PostMapping("/api/users/refresh")
    public ResponseEntity<RsData<LoginResDto>> refresh(@Valid @RequestBody RefreshTokenReqDto reqDto) {
        LoginResDto data = userService.refresh(reqDto);
        RsData<LoginResDto> rsData = new RsData<>("토큰 재발급이 완료되었습니다.", "200-3", data);
        return ResponseEntity.ok(rsData);
    }

    // [CUS-08] 로그인 - 내 정보 조회 - JWT로 인증된 현재 사용자의 회원 정보 조회
    @GetMapping("/api/users/me")
    public ResponseEntity<RsData<UserProfileResDto>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserProfileResDto data = userService.getMyProfile(userDetails.getUserId());
        RsData<UserProfileResDto> rsData = new RsData<>("내 정보 조회가 완료되었습니다.", "200-4", data);
        return ResponseEntity.ok(rsData);
    }

    // [CUS-10] 차량 정보 수정 - JWT로 인증된 현재 사용자의 차량 정보 업데이트
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
    @PostMapping("/api/users/logout")
    public ResponseEntity<RsData<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.logout(userDetails.getUserId());
        RsData<Void> rsData = new RsData<>("로그아웃이 완료되었습니다.", "200-7");
        return ResponseEntity.ok(rsData);
    }
}