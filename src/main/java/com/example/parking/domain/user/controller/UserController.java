package com.example.parking.domain.user.controller;

import com.example.parking.domain.admin.user.dto.AdminUserResDto;
import com.example.parking.domain.user.dto.*;
import com.example.parking.domain.user.service.UserService;
import com.example.parking.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private  final UserService userService;

    // [CUS-06] 회원가입 - 회원가입 요청을 받아 사용자 정보 저장
    @PostMapping("/api/users/signup")
    public ResponseEntity<UserProfileResDto> signup(@Valid @RequestBody SignupReqDto reqDto) {
        UserProfileResDto response = userService.signup(reqDto);
        return ResponseEntity.ok(response);
    }
    // [CUS-08] 로그인 - 로그인 요청을 받아 사용자 인증 후 JWT 토큰 발급
    @PostMapping("/api/users/login")
    public ResponseEntity<LoginResDto> login(@Valid @RequestBody LoginReqDto reqDto) {
        LoginResDto response = userService.login(reqDto);
        return ResponseEntity.ok(response);
    }

    // [CUS-08] 로그인 - 내 정보 조회 - JWT로 인증된 현재 사용자의 회원 정보 조회
    @GetMapping("/api/users/me")
    public ResponseEntity<UserProfileResDto> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserProfileResDto response = userService.getMyProfile(userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    // [CUS-10] 차량 정보 수정 - JWT로 인증된 현재 사용자의 차량 정보 업데이트
    @PatchMapping("/api/users/me/vehicle")
    public ResponseEntity<UserProfileResDto> updateMyVehicle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody VehicleUpdateReqDto reqDto
    ) {
        UserProfileResDto response = userService.updateMyVehicle(userDetails.getUserId(), reqDto);
        return ResponseEntity.ok(response);
    }

    // [CUS-07] 회원 탈퇴 - JWT로 인증된 현재 사용자의 계정을 탈퇴 처리
    @DeleteMapping("/api/users/me")
    public ResponseEntity<Map<String, String>> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WithdrawReqDto reqDto
    ) {
        userService.withdraw(userDetails.getUserId(), reqDto);
        return ResponseEntity.ok(Map.of(
                "message", "회원 탈퇴가 완료되었습니다."
        ));
    }

    // 로그아웃 - JWT로 인증된 현재 사용자의 세션을 무효화하여 로그아웃 처리
    @PostMapping("/api/users/logout")
    public ResponseEntity<Map<String, String>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.logout(userDetails.getUserId());
        return ResponseEntity.ok(Map.of(
                "message", "로그아웃이 완료되었습니다."
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage()
        ));
    }
}
