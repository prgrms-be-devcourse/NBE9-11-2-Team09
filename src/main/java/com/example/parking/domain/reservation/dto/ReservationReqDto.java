package com.example.parking.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ReservationReqDto(
        @NotNull(message = "주차장 ID는 필수입니다.")
        Long parkingLotId,

        @NotNull(message = "주차 자리 ID는 필수입니다.")
        Long parkingSpotId,

        //Jackson 에러를 피하기 위해 일차적으로 String으로 받음
        @NotNull(message = "시작 시간은 필수입니다.")
        String startTime,

        @NotNull(message = "종료 시간은 필수입니다.")
        String endTime
) {
        //1. 'T' 대신 공백을 사용하는 커스텀 포맷터를 선언
        private static final DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        //DTO 내부에서 24:00 처리를 포함하여 안전하게 변환해 주는 메서드
        public LocalDateTime getParsedStartTime() {
                return parseTime(this.startTime);
        }

        public LocalDateTime getParsedEndTime() {
                return parseTime(this.endTime);
        }

        // 실제 파싱 로직
        private LocalDateTime parseTime(String timeStr) {
                if (timeStr == null) return null;

                //2. 'T24' 대신 공백이 들어간 ' 24'를 찾음
                if (timeStr.contains(" 24:00")) {
                        timeStr = timeStr.replace(" 24:00", " 00:00");
                        //3. ISO 표준 대신 우리가 만든 포맷터로 파싱
                        return LocalDateTime.parse(timeStr, CUSTOM_FORMATTER).plusDays(1);
                }

                //4. 일반적인 시간도 우리가 만든 포맷터로 파싱
                return LocalDateTime.parse(timeStr, CUSTOM_FORMATTER);
        }
}