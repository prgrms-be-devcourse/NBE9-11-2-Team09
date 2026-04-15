package com.example.parking.domain.user.dto;

import com.example.parking.domain.user.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// [CUS-10] 차량 정보 수정 - 차량 번호와 차량 종류를 전달받는 DTO
@Getter
@NoArgsConstructor
public class VehicleUpdateReqDto {
    @NotBlank(message = "차량번호는 필수입니다.")
    @Size(max = 20, message = "차량번호는 20자 이하로 입력해야 합니다.")
    private String plateNumber;

    @NotNull(message = "차량종류는 필수입니다.")
    private VehicleType vehicleType;
}
