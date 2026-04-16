package com.example.parking.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefreshTokenReqDto {

    // 재발급 API는 refresh token 하나만 받아 새 access token을 발급
    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    private String refreshToken;
}
