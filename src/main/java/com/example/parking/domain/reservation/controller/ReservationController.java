package com.example.parking.domain.reservation.controller;

import com.example.parking.domain.reservation.dto.ReservationResDto;
import com.example.parking.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<ReservationResDto>> getList(@RequestParam Long userId) {
        return ResponseEntity.ok(reservationService.getMyReservations(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResDto> getDetail(@PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(reservationService.getReservationDetail(id, userId));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @RequestParam Long userId) {
        reservationService.cancelReservation(id, userId);
        return ResponseEntity.noContent().build();
    }
}
