package com.example.parking.domain.reservation.repository;

import com.example.parking.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

}