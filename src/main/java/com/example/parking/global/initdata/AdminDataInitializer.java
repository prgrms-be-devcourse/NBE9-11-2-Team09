package com.example.parking.global.initdata;

import com.example.parking.domain.user.entity.User;
import com.example.parking.domain.user.entity.UserRole;
import com.example.parking.domain.user.entity.VehicleType;
import com.example.parking.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.name:관리자}")
    private String adminName;

    @Value("${app.admin.plate-number:00가0000}")
    private String adminPlateNumber;

    @Value("${app.admin.vehicle-type:SMALL}")
    private String adminVehicleType;

    @Override
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank()) {
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            return;
        }

        if (userRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }

        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .name(adminName)
                .plateNumber(adminPlateNumber)
                .vehicleType(VehicleType.valueOf(adminVehicleType))
                .role(UserRole.ADMIN)
                .build();

        userRepository.save(admin);
    }
}
