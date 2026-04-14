package com.example.parking.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ReservationReqDto(
        @NotNull(message = "주차장 ID는 필수입니다.")
        Long parkingLotId,

        @NotNull(message = "주차 자리 ID는 필수입니다.")
        Long parkingSpotId,

        // 💡 Jackson 에러를 피하기 위해 일차적으로 String으로 받습니다.
        @NotNull(message = "시작 시간은 필수입니다.")
        String startTime,

        @NotNull(message = "종료 시간은 필수입니다.")
        String endTime
) {
        // 🚀 DTO 내부에서 24:00 처리를 포함하여 안전하게 변환해 주는 메서드
        public LocalDateTime getParsedStartTime() {
                return parseTime(this.startTime);
        }

        public LocalDateTime getParsedEndTime() {
                return parseTime(this.endTime);
        }

        // 실제 파싱 로직
        private LocalDateTime parseTime(String timeStr) {
                if (timeStr == null) return null;

                if (timeStr.contains("T24:00")) {
                        timeStr = timeStr.replace("T24:00", "T00:00");
                        return LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME).plusDays(1);
                }

                return LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
}