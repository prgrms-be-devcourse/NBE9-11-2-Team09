package com.example.parking.domain.admin.user.service;

import com.example.parking.domain.admin.user.dto.AdminUserResDto;
import com.example.parking.domain.user.entity.UserRole;
import com.example.parking.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.parking.domain.user.entity.UserRole;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// [ADM-05] 관리자 권한으로 전체 고객 목록 조회 - 이름 또는 이메일 키워드로 검색 가능, 페이징 처리
public class AdminUserService {

    private final UserRepository userRepository;

    public Page<AdminUserResDto> getAdminUsers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return userRepository.findByRole(UserRole.USER, pageable)
                    .map(AdminUserResDto::from);
        }

        return userRepository.findByRoleAndNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                        UserRole.USER,
                        keyword,
                        UserRole.USER,
                        keyword,
                        pageable
                )
                .map(AdminUserResDto::from);
    }
}
