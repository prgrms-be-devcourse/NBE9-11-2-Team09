package com.example.parking.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
// [CUS-08] 로그인 - 로그인 성공 시 발급한 JWT 토큰 정보를 반환하는 DTO
public class LoginResDto {

    private String accessToken;
    private String tokenType;
}
