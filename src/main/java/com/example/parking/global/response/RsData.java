package com.example.parking.global.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record RsData<T>(
        String msg,
        String resultCode,
        T data
) {
    public RsData(String msg, String resultCode) {
        this(msg, resultCode, null);
    }

    @JsonIgnore
    public int getStatusCode() {
        return Integer.parseInt(resultCode.split("-")[0]);
    }

}
