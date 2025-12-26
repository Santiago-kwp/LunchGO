package com.example.LunchGo.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationSummaryResponse {
    private RestaurantInfo restaurant;
    private BookingInfo booking;
    private PaymentInfo payment;

    @Getter
    @Builder
    public static class RestaurantInfo {
        private String name;
        private String address;
    }

    @Getter
    @Builder
    public static class BookingInfo {
        private String date;
        private String time;
        private Integer partySize;
    }

    @Getter
    @Builder
    public static class PaymentInfo {
        private String type;
        private Integer amount;
    }
}
