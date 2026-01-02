package com.example.LunchGo.review.exception;

import java.util.Map;
import java.util.Optional;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.LunchGo.review.controller.ReviewController;

@RestControllerAdvice(basePackageClasses = ReviewController.class)
public class ReviewValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = Optional.ofNullable(ex.getBindingResult().getFieldErrors())
            .flatMap(errors -> errors.stream().findFirst())
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .orElse("요청이 올바르지 않습니다.");
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }
}
