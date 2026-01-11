package com.example.LunchGo.reservation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.example.LunchGo.reservation")
public class ReservationExceptionHandler {

    @ExceptionHandler({DuplicateReservationException.class, SlotCapacityExceededException.class})
    public ResponseEntity<Map<String, String>> handleReservationExceptions(RuntimeException e) {
        Map<String, String> response = new HashMap<>();
        response.put("message", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGlobalException(Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
