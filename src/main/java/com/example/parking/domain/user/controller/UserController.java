package com.example.parking.domain.user.controller;

import com.example.parking.domain.user.dto.LoginReqDto;
import com.example.parking.domain.user.dto.LoginResDto;
import com.example.parking.domain.user.dto.SignupReqDto;
import com.example.parking.domain.user.dto.UserProfileResDto;
import com.example.parking.domain.user.service.UserService;
import com.example.parking.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage()
        ));
    }
}
