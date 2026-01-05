package com.example.LunchGo.reservation.service;

import com.example.LunchGo.member.entity.User;
import com.example.LunchGo.member.repository.UserRepository;
import com.example.LunchGo.reservation.dto.BusinessReservationDetailResponse;
import com.example.LunchGo.reservation.dto.BusinessReservationItemResponse;
import com.example.LunchGo.reservation.entity.Reservation;
import com.example.LunchGo.reservation.entity.ReservationSlot;
import com.example.LunchGo.reservation.mapper.ReservationMapper;
import com.example.LunchGo.reservation.mapper.row.BusinessReservationListRow;
import com.example.LunchGo.reservation.repository.ReservationRepository;
import com.example.LunchGo.reservation.repository.ReservationSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessReservationQueryService {

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final UserRepository userRepository;
    private final ReservationMapper reservationMapper;

    public List<BusinessReservationItemResponse> getList(Long restaurantId) {
        List<BusinessReservationListRow> rows = reservationMapper.selectBusinessReservationList(restaurantId);
        return rows.stream()
                .map(row -> BusinessReservationItemResponse.builder()
                        .id(row.getId())
                        .name(row.getName())
                        .phone(row.getPhone())
                        .datetime(row.getDatetime())
                        .guests(row.getGuests())
                        .amount(row.getAmount())
                        .status(row.getStatus())
                        .build())
                .toList();
    }

    public BusinessReservationDetailResponse getDetail(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(String.valueOf(reservationId)));

        ReservationSlot slot = reservationSlotRepository.findById(reservation.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("slot not found: " + reservation.getSlotId()));

        User user = userRepository.findById(reservation.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + reservation.getUserId()));

        String status = mapStatus(reservation.getStatus());
        String paymentType = "PREPAID_CONFIRMED".equals(String.valueOf(reservation.getStatus())) ? "prepaid" : "onsite";

        Integer amount = reservation.getTotalAmount();
        if (amount == null) {
            // total_amount 없으면 prepay/deposit 중 있는 값으로 표시
            amount = reservation.getPrepayAmount() != null ? reservation.getPrepayAmount() : reservation.getDepositAmount();
        }
        if (amount == null) amount = 0;

        return BusinessReservationDetailResponse.builder()
                .id(reservation.getReservationId())
                .name(user.getName())
                .phone(user.getPhone())
                .date(slot.getSlotDate().toString())
                .time(slot.getSlotTime().toString())
                .guests(reservation.getPartySize())
                .amount(amount)
                .status(status)
                .requestNote(reservation.getRequestMessage())
                .paymentType(paymentType)
                .preorderItems(Collections.emptyList())
                .build();
    }

    private String mapStatus(Object s) {
        if (s == null) return "pending";
        String status = String.valueOf(s);
        return switch (status) {
            case "TEMPORARY" -> "pending";
            case "CONFIRMED", "PREPAID_CONFIRMED" -> "confirmed";
            case "CANCELLED" -> "cancelled";
            case "EXPIRED", "NOSHOW" -> "refunded";
            default -> "pending";
        };
    }
}
