package com.example.parking.domain.parkingLot.service;


import com.example.parking.domain.parkingLot.dto.ParkingLotResDto;
import com.example.parking.domain.parkingLot.entity.ParkingLot;
import com.example.parking.domain.parkingLot.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;

    public List<ParkingLotResDto> findAll() {
        return parkingLotRepository.findAll().stream()
                .map(ParkingLotResDto::from)
                .toList();
    }

    public ParkingLotResDto findById(Long id) {
        ParkingLot parkingLot = parkingLotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 주차장이 없습니다."));

        return ParkingLotResDto.from(parkingLot);
    }
}
