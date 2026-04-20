package com.example.parking.domain.reservation.controller;

import com.example.parking.domain.reservation.dto.ReservationReqDto;
import com.example.parking.domain.reservation.dto.ReservationResDto;
import com.example.parking.domain.reservation.entity.ReservationStatus;
import com.example.parking.domain.reservation.service.ReservationService;
import com.example.parking.global.response.RsData;
import com.example.parking.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "예약", description = "예약 관련 API")
public class ReservationController {
    private final ReservationService reservationService;

    // [CUS-04] 내 예약 목록 조회 - 사용자의 전체 예약 리스트 조회
    @Operation(summary = "내 예약 목록 조회", description = "현재 로그인한 사용자의 예약 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 완료"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    public ResponseEntity<RsData<List<ReservationResDto>>> getList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) ReservationStatus status
    ) {
        List<ReservationResDto> data = reservationService.getMyReservations(userDetails.getUserId(), status);
        return ResponseEntity.ok(new RsData<>("예약 목록 조회가 완료되었습니다.", "200-1", data));
    }

    // [CUS-04] 특정 예약 상세 조회 - 예약 ID를 통한 단일 예약의 상세 정보 조회
    @Operation(summary = "예약 상세 조회", description = "예약 ID로 특정 예약의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 완료"),
            @ApiResponse(responseCode = "400", description = "존재하지 않거나 권한 없는 예약")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RsData<ReservationResDto>> getDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ReservationResDto data = reservationService.getReservationDetail(id, userDetails.getUserId());
        return ResponseEntity.ok(new RsData<>("예약 상세 조회가 완료되었습니다.", "200-2", data));
    }

    // [CUS-04] 예약 취소 - 사용자가 직접 예약을 취소하고 환불 절차 진행
    @Operation(summary = "예약 생성", description = "주차장과 자리를 선택하여 새로운 예약을 생성합니다. 생성 후 5분 이내에 결제를 완료해야 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 완료"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 주차장/자리/유저"),
            @ApiResponse(responseCode = "403", description = "차종 불일치"),
            @ApiResponse(responseCode = "409", description = "이미 진행 중인 예약 존재 또는 자리 선점 실패")
    })
    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<RsData<Void>> cancel(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        reservationService.cancelReservation(reservationId, userDetails.getUserId(), false);
        return ResponseEntity.ok(new RsData<>("예약 취소 및 환불이 완료되었습니다.", "200-3"));
    }

    // [CUS-03] 예약 생성 - 주차장 및 자리를 선택하여 새로운 예약 생성
    @Operation(summary = "예약 취소", description = "예약을 취소합니다. 입차 30분 전까지만 취소 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "취소 완료"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 예약"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "409", description = "이미 취소된 예약 또는 취소 불가 시간")
    })
    @PostMapping
    public ResponseEntity<RsData<ReservationResDto>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ReservationReqDto reqDto
    ) {
        ReservationResDto data = reservationService.createReservation(userDetails.getUserId(), reqDto);
        return ResponseEntity.ok(new RsData<>("예약이 성공적으로 완료되었습니다.", "201-1", data));
    }
}