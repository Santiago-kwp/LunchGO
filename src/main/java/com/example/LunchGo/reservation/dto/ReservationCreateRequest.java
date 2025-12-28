package com.example.LunchGo.reservation.dto;

import com.example.LunchGo.reservation.domain.ReservationType;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationCreateRequest {

    private Long userId;

    private Long restaurantId;
    private LocalDate slotDate;
    private LocalTime slotTime;

    private Integer partySize;

    private ReservationType reservationType;

    // 선택 입력 (<= 50)
    private String requestMessage;
}
