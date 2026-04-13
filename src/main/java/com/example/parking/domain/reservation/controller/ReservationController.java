package com.example.parking.domain.reservation.controller;

import com.example.parking.domain.reservation.dto.ReservationReqDto;
import com.example.parking.domain.reservation.dto.ReservationResDto;
import com.example.parking.domain.reservation.entity.ReservationStatus;
import com.example.parking.domain.reservation.service.ReservationService;
import com.example.parking.global.response.RsData; // RsData 임포트 확인
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            @RequestParam Long userId,
            @RequestParam(required = false) ReservationStatus status // 👈 필터링을 위한 상태값 추가
    ) {
        // 서비스 메서드에 status를 함께 전달합니다.
        List<ReservationResDto> data = reservationService.getMyReservations(userId, status);

        RsData<List<ReservationResDto>> rsData = new RsData<>("예약 목록 조회가 완료되었습니다.", "200-1", data);

        return ResponseEntity.status(rsData.getStatusCode()).body(rsData);
    }

    // [CUS-04] 특정 예약 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<RsData<ReservationResDto>> getDetail(@PathVariable Long id, @RequestParam Long userId) {
        ReservationResDto data = reservationService.getReservationDetail(id, userId);
        RsData<ReservationResDto> rsData = new RsData<>("예약 상세 조회가 완료되었습니다.", "200-2", data);

        return ResponseEntity.status(rsData.getStatusCode()).body(rsData);
    }

    // [CUS-04] 예약 취소
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RsData<Void>> cancel(@PathVariable Long id, @RequestParam Long userId) {
        reservationService.cancelReservation(id, userId);
        RsData<Void> rsData = new RsData<>("예약 취소가 완료되었습니다.", "200-3");

        return ResponseEntity.status(rsData.getStatusCode()).body(rsData);
    }

    // [CUS-03] 예약 생성
    @PostMapping
    public ResponseEntity<RsData<ReservationResDto>> create(@RequestParam Long userId, @RequestBody ReservationReqDto reqDto) {
        ReservationResDto data = reservationService.createReservation(userId, reqDto);
        RsData<ReservationResDto> rsData = new RsData<>("예약이 성공적으로 완료되었습니다.", "201-1", data);

        return ResponseEntity.status(rsData.getStatusCode()).body(rsData);
    }
}