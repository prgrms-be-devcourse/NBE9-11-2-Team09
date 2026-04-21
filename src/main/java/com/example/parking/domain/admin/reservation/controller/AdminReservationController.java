package com.example.parking.domain.admin.reservation.controller;

import com.example.parking.domain.admin.reservation.dto.AdminReservationResDto;
import com.example.parking.domain.admin.reservation.service.AdminReservationService;
import com.example.parking.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reservations")
@Tag(name = "관리자 - 예약", description = "관리자 예약 관련 API")
public class AdminReservationController {
    private final AdminReservationService adminReservationService;

    // [ADM-01] 예약 목록 조회 - 관리자 권한으로 전체 또는 특정 고객의 예약 리스트 조회
    @Operation(summary = "전체 예약 목록 조회", description = "관리자 권한으로 전체 또는 특정 고객의 예약 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 완료"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    public ResponseEntity<RsData<Page<AdminReservationResDto>>> getAdminReservations(
            @RequestParam(required = false) Long userId,
            Pageable pageable
    ) {
        Page<AdminReservationResDto> data = adminReservationService.getAdminReservations(userId, pageable);
        return ResponseEntity.ok(new RsData<>("전체 예약 목록 조회가 완료되었습니다.", "200-1", data));
    }

    // [ADM-01] 예약 강제 취소 - 관리자가 정책 위반 등의 사유로 예약을 강제로 취소
    @Operation(summary = "예약 강제 취소", description = "관리자 권한으로 특정 예약을 강제 취소하고 환불 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "강제 취소 완료"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 예약"),
            @ApiResponse(responseCode = "409", description = "이미 취소된 예약")
    })
    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<RsData<Void>> cancelByAdmin(@PathVariable Long reservationId) {
        adminReservationService.cancelReservationByAdmin(reservationId);
        return ResponseEntity.ok(new RsData<>("관리자 권한으로 예약 취소 및 환불이 완료되었습니다.", "200-5"));
    }
}