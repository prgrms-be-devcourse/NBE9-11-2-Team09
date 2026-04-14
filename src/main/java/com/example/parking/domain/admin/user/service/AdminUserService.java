package com.example.parking.domain.admin.user.service;

import com.example.parking.domain.admin.user.dto.AdminUserResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// [ADM-05] 관리자 권한으로 전체 고객 목록 조회 - 이름 또는 이메일 키워드로 검색 가능, 페이징 처리
public class AdminUserService {

    public Page<AdminUserResDto> getAdminUsers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return userRepository.findAll(pageable)
                    .map(AdminUserResDto::from);
        }

        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword,
                        keyword,
                        pageable
                )
                .map(AdminUserResDto::from);
    }
}
