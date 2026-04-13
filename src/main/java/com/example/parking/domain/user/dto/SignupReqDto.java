package com.example.parking.domain.user.dto;

import com.example.parking.domain.user.entity.VehicleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter// 누락된 Getter 어노테이션 추가
@NoArgsConstructor // 누락된 NoArgsConstructor 어노테이션 추가
public class SignupReqDto {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String userEmail;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "차량 번호는 필수입니다.")
    private String plateNumber;

    @NotNull(message = "차량 종류는 필수입니다.")
    private VehicleType vehicleType;

}
