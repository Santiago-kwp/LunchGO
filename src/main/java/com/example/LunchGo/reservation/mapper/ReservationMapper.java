package com.example.LunchGo.reservation.mapper;

import com.example.LunchGo.reservation.domain.Reservation;
import com.example.LunchGo.reservation.domain.ReservationSlot;
import com.example.LunchGo.reservation.mapper.row.ReservationCreateRow;
import java.time.LocalDate;
import java.time.LocalTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReservationMapper {

    ReservationSlot selectSlot(
            @Param("restaurantId") Long restaurantId,
            @Param("slotDate") LocalDate slotDate,
            @Param("slotTime") LocalTime slotTime
    );

    int insertReservation(Reservation reservation);

    int updateReservationCode(
            @Param("reservationId") Long reservationId,
            @Param("reservationCode") String reservationCode
    );

    ReservationCreateRow selectReservationCreateRow(@Param("reservationId") Long reservationId);
}
