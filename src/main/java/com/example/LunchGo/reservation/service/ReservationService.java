package com.example.LunchGo.reservation.service;

import com.example.LunchGo.reservation.dto.ReservationCreateRequest;
import com.example.LunchGo.reservation.dto.ReservationCreateResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationService {
    ReservationCreateResponse create(ReservationCreateRequest request);

    List<LocalTime> slotTimes(Long restaurantId, LocalDate slotDate);

}
