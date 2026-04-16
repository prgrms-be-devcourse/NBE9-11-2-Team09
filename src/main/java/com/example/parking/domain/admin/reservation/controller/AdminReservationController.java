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

    // [ADM-01] 고객 예약 목록 페이징 조회 (userId 파라미터로 특정 고객 필터 가능)
    @GetMapping
    public ResponseEntity<Page<AdminReservationResDto>> getAdminReservations(
            @RequestParam(required = false) Long userId,
            Pageable pageable
    ) {
        Page<AdminReservationResDto> response = adminReservationService.getAdminReservations(userId, pageable);
        return ResponseEntity.ok(response);
    }

    // [ADM-01] 관리자 권한 특정 예약 강제 취소
    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<RsData<Void>> cancelByAdmin(@PathVariable Long reservationId) {
        adminReservationService.cancelReservationByAdmin(reservationId);

        // 💡 관리자 응답에도 환불 완료 문구 추가
        RsData<Void> rsData = new RsData<>("관리자 권한으로 예약 취소 및 환불이 완료되었습니다.", "200-5");
        return ResponseEntity.status(rsData.getStatusCode()).body(rsData);
    }
}