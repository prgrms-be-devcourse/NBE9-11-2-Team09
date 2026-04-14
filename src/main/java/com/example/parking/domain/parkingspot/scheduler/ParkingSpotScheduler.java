package com.example.parking.domain.parkingspot.scheduler;

import com.example.parking.domain.parkingspot.dto.ParkingSpotDto;
import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.parkingspot.entity.SpotStatus;
import com.example.parking.domain.parkingspot.repository.ParkingSpotRepository;
import com.example.parking.global.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ParkingSpotScheduler {

  private final ParkingSpotRepository parkingSpotRepository;
  private final SseEmitterManager sseEmitterManager;

  @Scheduled(fixedDelay = 60000) // 1분마다 실행
  @Transactional
  public void releaseExpiredSpots() {
    LocalDateTime deadline = LocalDateTime.now().minusMinutes(5);

    List<ParkingSpot> expiredSpots = parkingSpotRepository
        .findByStatusAndReservedAtBefore(SpotStatus.OCCUPIED, deadline);

    for (ParkingSpot spot : expiredSpots) {
      spot.release();
      sseEmitterManager.notify(
          spot.getParkingLot().getId(),
          new ParkingSpotDto(spot)
      );
    }
  }
}