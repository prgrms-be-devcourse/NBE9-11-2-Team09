package com.example.parking.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
// [CUS-08] 로그인 - 로그인 API 응답 DTO
public class LoginResDto {

    private String accessToken;
    private String tokenType;
}
