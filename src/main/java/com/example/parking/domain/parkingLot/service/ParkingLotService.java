package com.example.parking.domain.parkingLot.service;


import com.example.parking.domain.parkingLot.dto.ParkingLotResDto;
import com.example.parking.domain.parkingLot.entity.ParkingLot;
import com.example.parking.domain.parkingLot.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;

    // [CUS-01] 전체 주차장 조회, 동 검색
    @Cacheable(value = "parkingLots", key = "#dong == null || #dong.isBlank() ? 'all' : #dong")
    public List<ParkingLotResDto> findAll(String dong) {
        List<ParkingLot> parkingLots;

        if (dong == null || dong.isBlank()) {
            parkingLots = parkingLotRepository.findAll();
        } else {
            parkingLots = parkingLotRepository.findByAddressContaining(dong);
        }

        return parkingLots.stream()
                .map(ParkingLotResDto::from)
                .toList();
    }

    // [CUS-01] 특정 주차장 조회
    @Cacheable(value = "parkingLot", key = "#id")
    public ParkingLotResDto findById(Long id) {
        ParkingLot parkingLot = parkingLotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 주차장이 없습니다."));

        return ParkingLotResDto.from(parkingLot);
    }
}
