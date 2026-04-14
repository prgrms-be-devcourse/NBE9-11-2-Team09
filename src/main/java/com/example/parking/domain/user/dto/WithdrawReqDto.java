package com.example.parking.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

// [CUS 07] 회원 탈퇴 - 회원탈퇴 요청 시 본인 확인을 위해 비밀번호를 전달받는 DTO
@Getter
@NoArgsConstructor
public class WithdrawReqDto {

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
