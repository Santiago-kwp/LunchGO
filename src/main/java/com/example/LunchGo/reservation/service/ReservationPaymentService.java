package com.example.LunchGo.reservation.service;

import com.example.LunchGo.reservation.domain.ReservationStatus;
import com.example.LunchGo.reservation.dto.CreatePaymentRequest;
import com.example.LunchGo.reservation.dto.CreatePaymentResponse;
import com.example.LunchGo.reservation.dto.PortoneCompleteRequest;
import com.example.LunchGo.reservation.dto.PortoneFailRequest;
import com.example.LunchGo.reservation.dto.ReservationConfirmationResponse;
import com.example.LunchGo.reservation.dto.ReservationSummaryResponse;
import com.example.LunchGo.reservation.entity.Payment;
import com.example.LunchGo.reservation.entity.Reservation;
import com.example.LunchGo.reservation.entity.ReservationSlot;
import com.example.LunchGo.reservation.mapper.ReservationSummaryMapper;
import com.example.LunchGo.reservation.mapper.row.ReservationMenuItemRow;
import com.example.LunchGo.reservation.repository.PaymentRepository;
import com.example.LunchGo.reservation.repository.ReservationRepository;
import com.example.LunchGo.reservation.repository.ReservationSlotRepository;
import com.example.LunchGo.reservation.service.PortoneVerificationService;
import com.example.LunchGo.restaurant.entity.Restaurant;
import com.example.LunchGo.restaurant.repository.RestaurantRepository;
import com.example.LunchGo.restaurant.stats.RestaurantStatsEventService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationPaymentService {
    private static final String PAYMENT_STATUS_READY = "READY";
    private static final String PAYMENT_STATUS_REQUESTED = "REQUESTED";
    private static final String PAYMENT_STATUS_PAID = "PAID";
    private static final String PAYMENT_STATUS_FAILED = "FAILED";
    private static final String PAYMENT_METHOD_CARD = "CARD";
    private static final String CARD_TYPE_UNKNOWN = "UNKNOWN";
    private static final String PG_PROVIDER_PORTONE = "PORTONE";

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final PaymentRepository paymentRepository;
    private final RestaurantRepository restaurantRepository;
    private final PortoneVerificationService portoneVerificationService;
    private final RestaurantStatsEventService statsEventService;
    private final ReservationSummaryMapper reservationSummaryMapper;

    @Transactional
    public CreatePaymentResponse createPayment(Long reservationId, CreatePaymentRequest request) {
        Reservation reservation = getReservation(reservationId);
        Integer expectedAmount = resolveExpectedAmount(reservation, request.getPaymentType());

        if (expectedAmount == null || !expectedAmount.equals(request.getAmount())) {
            throw new IllegalArgumentException("결제 금액이 예약 정보와 일치하지 않습니다.");
        }

        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<Payment> idempotentPayment = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (idempotentPayment.isPresent()) {
                Payment payment = idempotentPayment.get();
                if (!payment.getReservationId().equals(reservationId)
                    || !payment.getPaymentType().equals(request.getPaymentType())
                    || !payment.getAmount().equals(request.getAmount())) {
                    throw new IllegalArgumentException("이미 처리된 결제 요청입니다.");
                }
                return toCreateResponse(payment);
            }
        }

        Optional<Payment> existing = paymentRepository.findByReservationIdAndPaymentType(
            reservationId,
            request.getPaymentType()
        );
        if (existing.isPresent()) {
            Payment payment = existing.get();
            return toCreateResponse(payment);
        }

        String merchantUid = "RSV-" + reservationId + "-" + System.currentTimeMillis();
        Payment payment = Payment.builder()
            .reservationId(reservationId)
            .paymentType(request.getPaymentType())
            .status(PAYMENT_STATUS_READY)
            .method(resolveMethod(request.getMethod()))
            .cardType(CARD_TYPE_UNKNOWN)
            .amount(request.getAmount())
            .currency(reservation.getCurrency())
            .pgProvider(PG_PROVIDER_PORTONE)
            .merchantUid(merchantUid)
            .idempotencyKey(request.getIdempotencyKey())
            .requestedAt(LocalDateTime.now())
            .build();

        Payment saved = paymentRepository.save(payment);
        return toCreateResponse(saved);
    }

    @Transactional
    public void markPaymentRequested(String merchantUid) {
        if (merchantUid == null || merchantUid.isBlank()) {
            log.warn("결제 요청 시작 기록 실패: merchantUid 누락");
            throw new IllegalArgumentException("결제 정보를 찾을 수 없습니다.");
        }

        Optional<Payment> paymentOptional = paymentRepository.findByMerchantUid(merchantUid);
        if (paymentOptional.isEmpty()) {
            log.warn("결제 요청 시작 기록 실패: 결제 정보 없음 (merchantUid={})", merchantUid);
            throw new IllegalArgumentException("결제 정보를 찾을 수 없습니다.");
        }
        Payment payment = paymentOptional.get();

        if (PAYMENT_STATUS_READY.equals(payment.getStatus())) {
            payment.setStatus(PAYMENT_STATUS_REQUESTED);
            payment.setRequestedAt(LocalDateTime.now());
        } else {
            log.warn("결제 요청 시작 기록 스킵: 상태={} (merchantUid={})", payment.getStatus(), merchantUid);
        }
    }

    @Transactional
    public void completePayment(PortoneCompleteRequest request) {
        Payment payment = paymentRepository.findByMerchantUid(request.getMerchantUid())
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        if (!payment.getAmount().equals(request.getPaidAmount())) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        PortoneVerificationService.PortonePaymentInfo info = portoneVerificationService.verifyPayment(
            payment.getMerchantUid(),
            request.getPaidAmount()
        );

        payment.setStatus(PAYMENT_STATUS_PAID);
        payment.setImpUid(request.getImpUid());
        if (info.getPgTid() != null) {
            payment.setPgTid(info.getPgTid());
        }
        if (info.getMethod() != null) {
            payment.setMethod(normalizeMethod(info.getMethod()));
        }
        if (info.getCardType() != null) {
            payment.setCardType(normalizeCardType(info.getCardType()));
        }
        payment.setApprovedAt(LocalDateTime.now());

        Reservation reservation = getReservation(payment.getReservationId());
        reservation.setStatus(resolveReservationStatus(payment.getPaymentType()));

        recordConfirmAfterCommit(payment, reservation);
    }

    @Transactional
    public void failPayment(PortoneFailRequest request) {
        paymentRepository.findTopByReservationIdOrderByCreatedAtDesc(request.getReservationId())
            .ifPresent(payment -> {
                payment.setStatus(PAYMENT_STATUS_FAILED);
                payment.setFailedAt(LocalDateTime.now());
            });
    }

    @Transactional
    public void expirePayment(Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        reservation.setStatus(ReservationStatus.EXPIRED);

        paymentRepository.findTopByReservationIdOrderByCreatedAtDesc(reservationId)
            .ifPresent(payment -> {
                payment.setStatus(PAYMENT_STATUS_FAILED);
                payment.setFailedAt(LocalDateTime.now());
            });
    }

    @Transactional
    public void handleWebhookPaid(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("결제 정보를 찾을 수 없습니다.");
        }

        Payment payment = paymentRepository.findByMerchantUid(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        if (PAYMENT_STATUS_PAID.equals(payment.getStatus())) {
            return;
        }

        PortoneVerificationService.PortonePaymentInfo info = portoneVerificationService.verifyPayment(
            payment.getMerchantUid(),
            payment.getAmount()
        );

        payment.setStatus(PAYMENT_STATUS_PAID);
        payment.setApprovedAt(LocalDateTime.now());
        if (info.getPgTid() != null) {
            payment.setPgTid(info.getPgTid());
        }
        if (info.getMethod() != null) {
            payment.setMethod(normalizeMethod(info.getMethod()));
        }
        if (info.getCardType() != null) {
            payment.setCardType(normalizeCardType(info.getCardType()));
        }

        Reservation reservation = getReservation(payment.getReservationId());
        reservation.setStatus(resolveReservationStatus(payment.getPaymentType()));

        recordConfirmAfterCommit(payment, reservation);
    }

    @Transactional
    public void handleWebhookFailed(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("결제 정보를 찾을 수 없습니다.");
        }

        Payment payment = paymentRepository.findByMerchantUid(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        payment.setStatus(PAYMENT_STATUS_FAILED);
        payment.setFailedAt(LocalDateTime.now());

        Reservation reservation = getReservation(payment.getReservationId());
        if (ReservationStatus.TEMPORARY.equals(reservation.getStatus())) {
            reservation.setStatus(ReservationStatus.EXPIRED);
        }
    }

    @Transactional
    public void handleWebhookCancelled(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("결제 정보를 찾을 수 없습니다.");
        }

        Payment payment = paymentRepository.findByMerchantUid(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        payment.setStatus("CANCELLED");
        payment.setCancelledAt(LocalDateTime.now());

        Reservation reservation = getReservation(payment.getReservationId());
        reservation.setStatus(ReservationStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public ReservationConfirmationResponse getConfirmation(Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        ReservationSlot slot = getSlot(reservation.getSlotId());
        Restaurant restaurant = getRestaurant(slot.getRestaurantId());

        Payment payment = paymentRepository.findTopByReservationIdAndStatusOrderByApprovedAtDesc(
                reservationId,
                PAYMENT_STATUS_PAID
            )
            .orElseGet(() -> paymentRepository.findTopByReservationIdOrderByCreatedAtDesc(reservationId)
                .orElse(null));

        return ReservationConfirmationResponse.builder()
            .reservationCode(reservation.getReservationCode())
            .restaurant(ReservationConfirmationResponse.RestaurantInfo.builder()
                .name(restaurant.getName())
                .address(formatAddress(restaurant))
                .phone(restaurant.getPhone())
                .build())
            .booking(ReservationConfirmationResponse.BookingInfo.builder()
                .date(slot.getSlotDate().toString())
                .time(slot.getSlotTime().toString())
                .partySize(reservation.getPartySize())
                .requestNote(reservation.getRequestMessage())
                .build())
            .payment(ReservationConfirmationResponse.PaymentInfo.builder()
                .amount(payment != null ? payment.getAmount() : 0)
                .method(formatMethod(payment != null ? payment.getMethod() : null))
                .paidAt(formatPaidAt(payment != null ? payment.getApprovedAt() : null))
                .build())
            .build();
    }

    @Transactional(readOnly = true)
    public ReservationSummaryResponse getSummary(Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        ReservationSlot slot = getSlot(reservation.getSlotId());
        Restaurant restaurant = getRestaurant(slot.getRestaurantId());

        Payment payment = paymentRepository.findTopByReservationIdOrderByCreatedAtDesc(reservationId)
            .orElse(null);

        List<ReservationMenuItemRow> menuRows = reservationSummaryMapper.selectReservationMenuItems(reservationId);
        List<ReservationSummaryResponse.MenuItem> menuItems = new ArrayList<>();
        Integer menuTotal = null;
        if (menuRows != null && !menuRows.isEmpty()) {
            int sum = 0;
            for (ReservationMenuItemRow row : menuRows) {
                Integer lineAmount = row.getLineAmount();
                if (lineAmount != null) {
                    sum += lineAmount;
                }
                menuItems.add(ReservationSummaryResponse.MenuItem.builder()
                    .name(row.getName())
                    .quantity(row.getQuantity())
                    .unitPrice(row.getUnitPrice())
                    .lineAmount(row.getLineAmount())
                    .build());
            }
            menuTotal = sum;
        }

        Integer visitCount = reservationSummaryMapper.selectVisitCount(
            restaurant.getRestaurantId(),
            reservation.getUserId()
        );

        Integer totalAmount = menuTotal != null && menuTotal > 0
            ? menuTotal
            : reservation.getTotalAmount();
        if (totalAmount == null) {
            totalAmount = payment != null ? payment.getAmount() : 0;
        }

        return ReservationSummaryResponse.builder()
            .restaurant(ReservationSummaryResponse.RestaurantInfo.builder()
                .name(restaurant.getName())
                .address(formatAddress(restaurant))
                .build())
            .booking(ReservationSummaryResponse.BookingInfo.builder()
                .date(slot.getSlotDate().toString())
                .time(slot.getSlotTime().toString())
                .partySize(reservation.getPartySize())
                .requestNote(reservation.getRequestMessage())
                .build())
            .payment(ReservationSummaryResponse.PaymentInfo.builder()
                .type(payment != null ? payment.getPaymentType() : null)
                .amount(payment != null ? payment.getAmount() : 0)
                .build())
            .requestNote(reservation.getRequestMessage())
            .totalAmount(totalAmount)
            .visitCount(visitCount)
            .menuItems(menuItems.isEmpty() ? null : menuItems)
            .build();
    }

    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));
    }

    private ReservationSlot getSlot(Long slotId) {
        return reservationSlotRepository.findById(slotId)
            .orElseThrow(() -> new IllegalArgumentException("예약 슬롯 정보를 찾을 수 없습니다."));
    }

    private Restaurant getRestaurant(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("식당 정보를 찾을 수 없습니다."));
    }

    private Integer resolveExpectedAmount(Reservation reservation, String paymentType) {
        if ("DEPOSIT".equals(paymentType)) {
            return reservation.getDepositAmount();
        }
        if ("PREPAID_FOOD".equals(paymentType)) {
            return reservation.getPrepayAmount();
        }
        return null;
    }

    private ReservationStatus resolveReservationStatus(String paymentType) {
        if ("DEPOSIT".equals(paymentType)) {
            return ReservationStatus.CONFIRMED;
        }
        if ("PREPAID_FOOD".equals(paymentType)) {
            return ReservationStatus.PREPAID_CONFIRMED;
        }
        return ReservationStatus.CONFIRMED;
    }

    private String resolveMethod(String method) {
        if (method == null || method.isBlank()) {
            return PAYMENT_METHOD_CARD;
        }
        return method;
    }

    private String normalizeMethod(String method) {
        if (method == null || method.isBlank()) {
            return PAYMENT_METHOD_CARD;
        }
        String normalized = method.trim().toUpperCase();
        if (normalized.contains("CARD")) {
            return "CARD";
        }
        if (normalized.contains("TRANSFER") || normalized.contains("BANK")) {
            return "TRANSFER";
        }
        if (normalized.contains("VIRTUAL")) {
            return "VIRTUAL_ACCOUNT";
        }
        return "UNKNOWN";
    }

    private String normalizeCardType(String cardType) {
        if (cardType == null || cardType.isBlank()) {
            return CARD_TYPE_UNKNOWN;
        }
        String normalized = cardType.trim().toUpperCase();
        if (normalized.contains("CORPORATE") || normalized.contains("BUSINESS")) {
            return "CORPORATE";
        }
        if (normalized.contains("PERSONAL") || normalized.contains("PRIVATE")) {
            return "PERSONAL";
        }
        return CARD_TYPE_UNKNOWN;
    }

    private String formatAddress(Restaurant restaurant) {
        String detail = restaurant.getDetailAddress();
        if (detail == null || detail.isBlank()) {
            return restaurant.getRoadAddress();
        }
        return restaurant.getRoadAddress() + " " + detail;
    }

    private String formatMethod(String method) {
        if (method == null) {
            return "신용카드";
        }
        if ("CARD".equalsIgnoreCase(method)) {
            return "신용카드";
        }
        if ("TRANSFER".equalsIgnoreCase(method)) {
            return "계좌이체";
        }
        return method;
    }

    private String formatPaidAt(LocalDateTime approvedAt) {
        if (approvedAt == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm");
        return approvedAt.format(formatter);
    }

    private void recordConfirmAfterCommit(Payment payment, Reservation reservation) {
        if (payment == null || reservation == null) {
            return;
        }
        Long paymentId = payment.getPaymentId();
        if (paymentId == null) {
            return;
        }
        ReservationSlot slot = getSlot(reservation.getSlotId());
        Long restaurantId = slot.getRestaurantId();
        String dedupeKey = paymentId.toString();

        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            statsEventService.recordConfirm(restaurantId, dedupeKey);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                statsEventService.recordConfirm(restaurantId, dedupeKey);
            }
        });
    }

    private CreatePaymentResponse toCreateResponse(Payment payment) {
        return CreatePaymentResponse.builder()
            .paymentId(payment.getPaymentId())
            .merchantUid(payment.getMerchantUid())
            .amount(payment.getAmount())
            .pgProvider(payment.getPgProvider())
            .currency(payment.getCurrency())
            .build();
    }
}
