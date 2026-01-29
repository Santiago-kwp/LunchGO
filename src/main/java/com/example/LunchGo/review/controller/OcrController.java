package com.example.LunchGo.review.controller;

import com.example.LunchGo.image.dto.ImageUploadResponse;
import com.example.LunchGo.image.service.LocalImageStorageService;
import com.example.LunchGo.review.dto.ReceiptDTO;
import com.example.LunchGo.review.entity.Receipt;
import com.example.LunchGo.review.service.OcrService;
import com.example.LunchGo.review.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;
    private final LocalImageStorageService localImageStorageService;
    private final ReceiptService receiptService;

    @PostMapping("/receipt")
    public ResponseEntity<?> uploadReceipt(
        @RequestParam("file") MultipartFile file,
        @RequestParam("reservationId") Long reservationId
    ) {
        localImageStorageService.validateUpload(file);

        ReceiptDTO result = ocrService.recognizeReceipt(file);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        ImageUploadResponse uploadResponse = localImageStorageService.upload("receipts", file);
        String receiptKey = uploadResponse.getKey();
        try {
            Receipt receipt = receiptService.createReceipt(reservationId, receiptKey, result);
            result.setReceiptId(receipt.getReceiptId());
            result.setReceiptImageKey(receiptKey);
        } catch (RuntimeException ex) {
            localImageStorageService.deleteObject(receiptKey);
            throw ex;
        }

        return ResponseEntity.ok(result);
    }
}
