package com.example.parking.domain.reservation.controller;

import com.example.parking.domain.reservation.dto.ReservationReqDto;
import com.example.parking.domain.reservation.dto.ReservationResDto;
import com.example.parking.domain.reservation.entity.ReservationStatus;
import com.example.parking.domain.reservation.service.ReservationService;
import com.example.parking.global.response.RsData;
import com.example.parking.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    // [CUS-04] 내 예약 목록 조회 - 사용자의 전체 예약 리스트 조회
    @GetMapping
    public ResponseEntity<RsData<List<ReservationResDto>>> getList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) ReservationStatus status
    ) {
        List<ReservationResDto> data = reservationService.getMyReservations(userDetails.getUserId(), status);
        return ResponseEntity.ok(new RsData<>("예약 목록 조회가 완료되었습니다.", "200-1", data));
    }

    // [CUS-04] 특정 예약 상세 조회 - 예약 ID를 통한 단일 예약의 상세 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<RsData<ReservationResDto>> getDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ReservationResDto data = reservationService.getReservationDetail(id, userDetails.getUserId());
        return ResponseEntity.ok(new RsData<>("예약 상세 조회가 완료되었습니다.", "200-2", data));
    }

    // [CUS-04] 예약 취소 - 사용자가 직접 예약을 취소하고 환불 절차 진행
    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<RsData<Void>> cancel(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        reservationService.cancelReservation(reservationId, userDetails.getUserId(), false);
        return ResponseEntity.ok(new RsData<>("예약 취소 및 환불이 완료되었습니다.", "200-3"));
    }

    // [CUS-03] 예약 생성 - 주차장 및 자리를 선택하여 새로운 예약 생성
    @PostMapping
    public ResponseEntity<RsData<ReservationResDto>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ReservationReqDto reqDto
    ) {
        ReservationResDto data = reservationService.createReservation(userDetails.getUserId(), reqDto);
        return ResponseEntity.ok(new RsData<>("예약이 성공적으로 완료되었습니다.", "201-1", data));
    }
}