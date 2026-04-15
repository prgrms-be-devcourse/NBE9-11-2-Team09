package com.example.parking.domain.admin.user.controller;

import com.example.parking.domain.admin.user.dto.AdminUserResDto;
import com.example.parking.domain.admin.user.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
// [ADM-05] 관리자 화면에서 전체 고객 목록 페이징 조회 - 이름 또는 이메일 키워드로 검색 가능
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/api/admin/users")
    public ResponseEntity<Page<AdminUserResDto>> getAdminUsers(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        Page<AdminUserResDto> response = adminUserService.getAdminUsers(keyword, pageable);
        return ResponseEntity.ok(response);
    }
}
