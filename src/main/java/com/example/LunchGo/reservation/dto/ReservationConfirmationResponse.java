package com.example.LunchGo.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationConfirmationResponse {
    private String reservationCode;
    private RestaurantInfo restaurant;
    private BookingInfo booking;
    private PaymentInfo payment;

    @Getter
    @Builder
    public static class RestaurantInfo {
        private String name;
        private String address;
        private String phone;
    }

    @Getter
    @Builder
    public static class BookingInfo {
        private String date;
        private String time;
        private Integer partySize;
        private String requestNote;
    }

    @Getter
    @Builder
    public static class PaymentInfo {
        private Integer amount;
        private String method;
        private String paidAt;
    }
}
