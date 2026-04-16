package com.example.parking.domain.reservation.controller;

import com.example.parking.domain.reservation.dto.ReservationReqDto;
import com.example.parking.domain.reservation.dto.ReservationResDto;
import com.example.parking.domain.reservation.entity.ReservationStatus;
import com.example.parking.domain.reservation.service.ReservationService;
import com.example.parking.global.response.RsData; // RsData 임포트 확인
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

    // [CUS-04] 내 예약 목록 조회
    @GetMapping
    public ResponseEntity<RsData<List<ReservationResDto>>> getList(
            @AuthenticationPrincipal CustomUserDetails userDetails, // 👈 수정
            @RequestParam(required = false) ReservationStatus status
    ) {
        List<ReservationResDto> data = reservationService.getMyReservations(userDetails.getUserId(), status);
        RsData<List<ReservationResDto>> rsData = new RsData<>("예약 목록 조회가 완료되었습니다.", "200-1", data);
        return ResponseEntity.status(rsData.getStatusCode()).body(rsData);
    }

    // [CUS-04] 특정 예약 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<RsData<ReservationResDto>> getDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails // 👈 수정
    ) {
        ReservationResDto data = reservationService.getReservationDetail(id, userDetails.getUserId());
        RsData<ReservationResDto> rsData = new RsData<>("예약 상세 조회가 완료되었습니다.", "200-2", data);
        return ResponseEntity.status(rsData.getStatusCode()).body(rsData);
    }

    // [CUS-04] 예약 취소
    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<RsData<Void>> cancel(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomUserDetails userDetails // 👈 토큰에서 유저 정보 꺼내오기!
    ) {
        // userDetails 객체 안에서 userId를 꺼내서 서비스 로직으로 던져줍니다.
        reservationService.cancelReservation(reservationId, userDetails.getUserId(), false);
        RsData<Void> rsData = new RsData<>("예약 취소 및 환불이 완료되었습니다.", "200-3");

        return ResponseEntity.status(rsData.getStatusCode()).body(rsData);
    }

    // [CUS-03] 예약 생성
    @PostMapping
    public ResponseEntity<RsData<ReservationResDto>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails, // 👈 수정
            @RequestBody ReservationReqDto reqDto
    ) {
        ReservationResDto data = reservationService.createReservation(userDetails.getUserId(), reqDto);
        RsData<ReservationResDto> rsData = new RsData<>("예약이 성공적으로 완료되었습니다.", "201-1", data);
        return ResponseEntity.status(rsData.getStatusCode()).body(rsData);
    }
}