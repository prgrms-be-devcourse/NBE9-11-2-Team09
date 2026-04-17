package com.example.parking.domain.admin.user.controller;

import com.example.parking.domain.admin.user.dto.AdminUserResDto;
import com.example.parking.domain.admin.user.service.AdminUserService;
import com.example.parking.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // [ADM-05] 전체 고객 목록 조회 - 관리자 권한으로 시스템 내 전체 사용자 리스트(검색 포함) 조회
    @GetMapping
    public ResponseEntity<RsData<Page<AdminUserResDto>>> getAdminUsers(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        Page<AdminUserResDto> data = adminUserService.getAdminUsers(keyword, pageable);
        return ResponseEntity.ok(new RsData<>("고객 목록 조회가 완료되었습니다.", "200-1", data));
    }
}