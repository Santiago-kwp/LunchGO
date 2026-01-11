package com.example.LunchGo.reservation.service;

import com.example.LunchGo.reservation.domain.ReservationStatus;
import com.example.LunchGo.reservation.entity.Payment;
import com.example.LunchGo.reservation.entity.Reservation;
import com.example.LunchGo.reservation.repository.PaymentRepository;
import com.example.LunchGo.reservation.repository.ReservationRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationPaymentExpiryService {
    private static final String PAYMENT_STATUS_FAILED = "FAILED";

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public int expireDuePayments(LocalDateTime now) {
        List<Reservation> targets = reservationRepository.findDueForPaymentExpiry(
            ReservationStatus.TEMPORARY,
            now
        );
        if (targets == null || targets.isEmpty()) {
            return 0;
        }

        Map<Long, Payment> latestPayments = loadLatestPayments(targets);
        int expired = 0;
        for (Reservation reservation : targets) {
            if (reservation == null || reservation.getReservationId() == null) {
                continue;
            }
            if (!ReservationStatus.TEMPORARY.equals(reservation.getStatus())) {
                continue;
            }
            reservation.setStatus(ReservationStatus.EXPIRED);
            log.info("결제 만료 처리: reservationId={}, holdExpiresAt={}, paymentDeadlineAt={}, now={}",
                reservation.getReservationId(),
                reservation.getHoldExpiresAt(),
                reservation.getPaymentDeadlineAt(),
                now);
            Payment payment = latestPayments.get(reservation.getReservationId());
            if (payment != null) {
                markPaymentFailed(payment, now);
            }
            expired++;
        }
        return expired;
    }

    private Map<Long, Payment> loadLatestPayments(List<Reservation> reservations) {
        Map<Long, Payment> latestMap = new HashMap<>();
        List<Long> reservationIds = reservations.stream()
            .map(Reservation::getReservationId)
            .filter(id -> id != null)
            .toList();
        if (reservationIds.isEmpty()) {
            return latestMap;
        }
        List<Payment> payments = paymentRepository.findByReservationIdInOrderByReservationIdAscCreatedAtDesc(
            reservationIds
        );
        for (Payment payment : payments) {
            if (payment == null || payment.getReservationId() == null) {
                continue;
            }
            latestMap.putIfAbsent(payment.getReservationId(), payment);
        }
        return latestMap;
    }

    private void markPaymentFailed(Payment payment, LocalDateTime now) {
        if (payment == null) {
            return;
        }
        payment.setStatus(PAYMENT_STATUS_FAILED);
        payment.setFailedAt(now);
    }
}
