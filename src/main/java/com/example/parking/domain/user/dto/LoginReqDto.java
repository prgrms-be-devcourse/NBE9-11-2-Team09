package com.example.parking.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
// [CUS-08] 로그인 - 로그인 요청에서 이메일과 비밀번호를 전달받는 DTO
public class LoginReqDto {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String userEmail;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
