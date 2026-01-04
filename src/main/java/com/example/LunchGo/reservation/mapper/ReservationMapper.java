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

    // 비관적 락을 적용하여 특정 예약 슬롯 조회
    ReservationSlot selectSlotForUpdate(
            @Param("restaurantId") Long restaurantId,
            @Param("slotDate") LocalDate slotDate,
            @Param("slotTime") LocalTime slotTime);

    int insertReservation(Reservation reservation);

    int updateReservationCode(
            @Param("reservationId") Long reservationId,
            @Param("reservationCode") String reservationCode
    );

    ReservationCreateRow selectReservationCreateRow(@Param("reservationId") Long reservationId);

    java.util.List<java.time.LocalTime> selectSlotTimesByDate(
            @Param("restaurantId") Long restaurantId,
            @Param("slotDate") java.time.LocalDate slotDate
    );

    int upsertSlot(
            @Param("restaurantId") Long restaurantId,
            @Param("slotDate") LocalDate slotDate,
            @Param("slotTime") LocalTime slotTime,
            @Param("maxCapacity") Integer maxCapacity
    );

    int sumPartySizeBySlotId(@Param("slotId") Long slotId);

    java.util.List<com.example.LunchGo.reservation.mapper.row.BusinessReservationListRow>
    selectBusinessReservationList(
            @org.apache.ibatis.annotations.Param("restaurantId") java.lang.Long restaurantId
    );

    java.util.List<com.example.LunchGo.reservation.mapper.row.AdminReservationListRow>
    selectAdminReservationList();
}
