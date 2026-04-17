package com.example.parking.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailCheckResDto {

    private boolean available;
    private String message;
}