package com.example.parking.domain.user.dto;

import com.example.parking.domain.user.entity.User;
import com.example.parking.domain.user.entity.UserRole;
import com.example.parking.domain.user.entity.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileResDto {

    private Long userId;
    private String userEmail;
    private String userName;
    private String plateNumber;
    private VehicleType vehicleType;
    private UserRole role; // 추가

    public static UserProfileResDto from(User user) {
        return new UserProfileResDto(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getPlateNumber(),
            user.getVehicleType(),
            user.getRole()
        );
    }
}
