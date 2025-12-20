package com.example.LunchGo.review.controller;

import com.example.LunchGo.review.dto.ReceiptDTO;
import com.example.LunchGo.review.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;

    @PostMapping("/receipt")
    public ResponseEntity<?> uploadReceipt(@RequestParam("file") MultipartFile file) {
        ReceiptDTO result = ocrService.recognizeReceipt(file);
        if (result == null) {
            // OCR 인식 실패 시 500 에러나 다른 에러 처리를 할 수 있습니다.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok(result);
    }
}