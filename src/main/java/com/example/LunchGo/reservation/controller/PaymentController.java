package com.example.LunchGo.reservation.controller;

import com.example.LunchGo.reservation.dto.PortoneCompleteRequest;
import com.example.LunchGo.reservation.dto.PortoneFailRequest;
import com.example.LunchGo.reservation.dto.PortoneWebhookRequest;
import com.example.LunchGo.reservation.service.PortoneWebhookVerifier;
import com.example.LunchGo.reservation.service.ReservationPaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PaymentController {
    private final ReservationPaymentService reservationPaymentService;
    private final PortoneWebhookVerifier portoneWebhookVerifier;
    private final ObjectMapper objectMapper;

    @PostMapping("/payments/portone/complete")
    public ResponseEntity<?> completePayment(@RequestBody PortoneCompleteRequest request) {
        reservationPaymentService.completePayment(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/payments/portone/fail")
    public ResponseEntity<?> failPayment(@RequestBody PortoneFailRequest request) {
        reservationPaymentService.failPayment(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/payments/portone/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody String rawBody, WebRequest webRequest) {
        portoneWebhookVerifier.verify(rawBody, webRequest);
        try {
            PortoneWebhookRequest request = objectMapper.readValue(rawBody, PortoneWebhookRequest.class);
            String eventType = request.getType();
            String paymentId = request.getData() != null ? request.getData().getPaymentId() : null;

            if ("Transaction.Paid".equalsIgnoreCase(eventType)) {
                reservationPaymentService.handleWebhookPaid(paymentId);
            } else if ("Transaction.Failed".equalsIgnoreCase(eventType)) {
                reservationPaymentService.handleWebhookFailed(paymentId);
            } else if ("Transaction.Cancelled".equalsIgnoreCase(eventType)) {
                reservationPaymentService.handleWebhookCancelled(paymentId);
            }
        } catch (Exception ignored) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
