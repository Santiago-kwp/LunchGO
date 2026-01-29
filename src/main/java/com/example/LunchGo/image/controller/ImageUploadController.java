package com.example.LunchGo.image.controller;

import com.example.LunchGo.image.dto.ImageUploadResponse;
import com.example.LunchGo.image.service.LocalImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class ImageUploadController {

    private final LocalImageStorageService localImageStorageService;

    @PostMapping("/upload/{domain}")
    public ResponseEntity<ImageUploadResponse> upload(
        @PathVariable String domain,
        @RequestParam("file") MultipartFile file
    ) {
        ImageUploadResponse response = localImageStorageService.upload(domain, file);
        return ResponseEntity.ok(response);
    }
}
