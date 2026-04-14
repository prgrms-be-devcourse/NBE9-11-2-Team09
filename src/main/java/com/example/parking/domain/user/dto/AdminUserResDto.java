package com.example.parking.domain.user.dto;

import com.example.parking.domain.user.entity.User;
import com.example.parking.domain.user.entity.UserRole;
import com.example.parking.domain.user.entity.UserStatus;
import com.example.parking.domain.user.entity.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// [ADM-05] 관리자 - 사용자 정보 조회 - 관리자 권한으로 특정 사용자의 상세 정보를 조회하는 DTO
@Getter
@AllArgsConstructor
public class AdminUserResDto {

    private Long userId;
    private String userEmail;
    private String userName;
    private String plateNumber;
    private VehicleType vehicleType;
    private UserRole role;
    private UserStatus userStatus;
    private LocalDateTime createdTime;

    public static AdminUserResDto from(User user) {
        return new AdminUserResDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPlateNumber(),
                user.getVehicleType(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedTime()
        );
    }
}
