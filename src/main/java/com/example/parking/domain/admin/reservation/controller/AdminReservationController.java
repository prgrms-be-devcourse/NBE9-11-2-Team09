package com.example.parking.domain.admin.reservation.controller;

import com.example.parking.domain.admin.reservation.dto.AdminReservationResDto;
import com.example.parking.domain.admin.reservation.service.AdminReservationService;
import com.example.parking.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reservations")
public class AdminReservationController {
    private final AdminReservationService adminReservationService;

    // [ADM-01] 예약 목록 조회 - 관리자 권한으로 전체 또는 특정 고객의 예약 리스트 조회
    @GetMapping
    public ResponseEntity<RsData<Page<AdminReservationResDto>>> getAdminReservations(
            @RequestParam(required = false) Long userId,
            Pageable pageable
    ) {
        Page<AdminReservationResDto> data = adminReservationService.getAdminReservations(userId, pageable);
        return ResponseEntity.ok(new RsData<>("전체 예약 목록 조회가 완료되었습니다.", "200-1", data));
    }

    // [ADM-01] 예약 강제 취소 - 관리자가 정책 위반 등의 사유로 예약을 강제로 취소
    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<RsData<Void>> cancelByAdmin(@PathVariable Long reservationId) {
        adminReservationService.cancelReservationByAdmin(reservationId);
        return ResponseEntity.ok(new RsData<>("관리자 권한으로 예약 취소 및 환불이 완료되었습니다.", "200-5"));
    }
}