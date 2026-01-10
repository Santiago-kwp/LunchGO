package com.example.LunchGo.reservation.repository;

import com.example.LunchGo.reservation.domain.ReservationStatus;
import com.example.LunchGo.reservation.entity.Reservation;
import com.example.LunchGo.reservation.entity.ReservationSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
        select r
        from Reservation r
        join ReservationSlot s on s.slotId = r.slotId
        where s.slotDate = :slotDate
          and s.slotTime between :fromTime and :toTime
          and r.status in :statuses
          and r.reminderSentAt is null
    """)
    List<Reservation> findReminderTargets(
            @Param("slotDate") LocalDate slotDate,
            @Param("fromTime") LocalTime fromTime,
            @Param("toTime") LocalTime toTime,
            @Param("statuses") Collection<ReservationStatus> statuses
    );
}
