package com.example.parking.domain.parkingLot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record ParkingApiResDto(

    @JsonProperty("DESCRIPTION")
    Map<String, String> description,

    @JsonProperty("DATA")
    List<ParkingApiItem> data

) {
}
