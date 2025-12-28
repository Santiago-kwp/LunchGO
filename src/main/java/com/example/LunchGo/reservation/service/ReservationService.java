package com.example.LunchGo.reservation.service;

import com.example.LunchGo.reservation.dto.ReservationCreateRequest;
import com.example.LunchGo.reservation.dto.ReservationCreateResponse;

public interface ReservationService {
    ReservationCreateResponse create(ReservationCreateRequest request);
}
