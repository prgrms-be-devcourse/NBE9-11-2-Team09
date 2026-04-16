package com.example.parking.domain.parkingLot.controller;

import com.example.parking.domain.parkingLot.dto.ParkingLotResDto;
import com.example.parking.domain.parkingLot.service.ParkingLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    @GetMapping
    public List<ParkingLotResDto> getParkingLots(
            @RequestParam(required = false) String dong) {
        return parkingLotService.findAll(dong);
    }

    @GetMapping("/{id}")
    public ParkingLotResDto getParkingLot(@PathVariable Long id) {
        return parkingLotService.findById(id);
    }
}
