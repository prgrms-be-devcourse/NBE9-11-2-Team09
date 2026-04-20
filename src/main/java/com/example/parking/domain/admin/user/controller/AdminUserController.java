package com.example.parking.domain.admin.user.controller;

import com.example.parking.domain.admin.user.dto.AdminUserResDto;
import com.example.parking.domain.admin.user.service.AdminUserService;
import com.example.parking.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@Tag(name = "관리자 - 회원", description = "관리자 회원 관련 API")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // [ADM-05] 전체 고객 목록 조회 - 관리자 권한으로 시스템 내 전체 사용자 리스트(검색 포함) 조회
    @Operation(summary = "전체 회원 목록 조회", description = "관리자 권한으로 전체 회원 목록을 조회합니다. 이름 또는 이메일로 검색 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 완료"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    public ResponseEntity<RsData<Page<AdminUserResDto>>> getAdminUsers(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        Page<AdminUserResDto> data = adminUserService.getAdminUsers(keyword, pageable);
        return ResponseEntity.ok(new RsData<>("고객 목록 조회가 완료되었습니다.", "200-1", data));
    }
}