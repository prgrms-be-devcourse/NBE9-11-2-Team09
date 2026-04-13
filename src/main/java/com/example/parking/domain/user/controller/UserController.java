package com.example.parking.domain.user.controller;

import com.example.parking.domain.user.dto.LoginReqDto;
import com.example.parking.domain.user.dto.LoginResDto;
import com.example.parking.domain.user.dto.SignupReqDto;
import com.example.parking.domain.user.dto.UserProfileResDto;
import com.example.parking.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage()
        ));
    }
}
