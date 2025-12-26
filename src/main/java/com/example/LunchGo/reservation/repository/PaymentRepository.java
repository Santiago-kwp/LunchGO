package com.example.LunchGo.reservation.repository;

import com.example.LunchGo.reservation.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findTopByReservationIdOrderByCreatedAtDesc(Long reservationId);
    Optional<Payment> findTopByReservationIdAndStatusOrderByApprovedAtDesc(Long reservationId, String status);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    Optional<Payment> findByMerchantUid(String merchantUid);
    Optional<Payment> findByReservationIdAndPaymentType(Long reservationId, String paymentType);
}
