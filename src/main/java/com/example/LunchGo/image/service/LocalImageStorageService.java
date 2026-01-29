package com.example.LunchGo.image.service;

import com.example.LunchGo.image.config.LocalImageStorageProperties;
import com.example.LunchGo.image.dto.ImageUploadResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LocalImageStorageService {

    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MM");

    private final LocalImageStorageProperties properties;

    public ImageUploadResponse upload(String domain, MultipartFile file) {
        validateUpload(file);

        String contentType = resolveContentType(file);
        String extension = resolveExtension(file, contentType);
        String key = buildKey(domain, extension);

        Path targetPath = Paths.get(properties.getBaseDir(), key);

        try {
            Files.createDirectories(targetPath.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("failed to upload file", e);
        }

        String fileUrl = properties.getBaseUrl() + "/" + key;
        return new ImageUploadResponse(fileUrl, key, contentType, file.getSize());
    }

    public void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is required");
        }
        String contentType = resolveContentType(file);
        validateFile(file, contentType);
    }

    public void deleteObject(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        String normalizedKey = normalizeKey(key);
        if (normalizedKey == null) {
            return;
        }
        Path targetPath = Paths.get(properties.getBaseDir(), normalizedKey);
        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            // Log but don't throw - deletion failure shouldn't break the flow
        }
    }

    public String normalizeKey(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return null;
        }
        String baseUrl = properties.getBaseUrl();
        if (baseUrl != null && storedValue.startsWith(baseUrl + "/")) {
            return storedValue.substring(baseUrl.length() + 1);
        }
        if (storedValue.startsWith("/images/")) {
            return storedValue.substring("/images/".length());
        }
        return storedValue;
    }

    public String buildPublicUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String normalizedKey = normalizeKey(key);
        return properties.getBaseUrl() + "/" + normalizedKey;
    }

    private String buildKey(String domain, String extension) {
        LocalDate today = LocalDate.now();
        String year = YEAR_FORMAT.format(today);
        String month = MONTH_FORMAT.format(today);
        String uuid = UUID.randomUUID().toString();

        return switch (domain.toLowerCase(Locale.ROOT)) {
            case "reviews" -> String.format("reviews/%s/%s/%s.%s", year, month, uuid, extension);
            case "receipts" -> String.format("receipts/%s/%s/%s.%s", year, month, uuid, extension);
            case "cafeteria" -> String.format("cafeteria/%s/%s/%s.%s", year, month, uuid, extension);
            case "restaurants" -> String.format("restaurants/%s/%s/%s.%s", year, month, uuid, extension);
            case "menus" -> String.format("menus/%s/%s/%s.%s", year, month, uuid, extension);
            case "profile" -> String.format("profile/%s/%s/%s.%s", year, month, uuid, extension);
            default -> throw new IllegalArgumentException("unsupported domain: " + domain);
        };
    }

    private String resolveExtension(MultipartFile file, String contentType) {
        String original = file.getOriginalFilename();
        if (original != null) {
            int idx = original.lastIndexOf('.');
            if (idx > -1 && idx + 1 < original.length()) {
                return original.substring(idx + 1).toLowerCase(Locale.ROOT);
            }
        }

        if (MediaType.IMAGE_JPEG_VALUE.equals(contentType)) {
            return "jpg";
        }
        if (MediaType.IMAGE_PNG_VALUE.equals(contentType)) {
            return "png";
        }
        if (MediaType.IMAGE_GIF_VALUE.equals(contentType)) {
            return "gif";
        }
        if ("image/webp".equals(contentType)) {
            return "webp";
        }

        return "bin";
    }

    private String resolveContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return contentType;
    }

    private void validateFile(MultipartFile file, String contentType) {
        long maxSize = properties.getMaxSizeBytes();
        if (maxSize > 0 && file.getSize() > maxSize) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file exceeds max size");
        }

        List<String> allowedTypes = properties.getAllowedContentTypes();
        if (allowedTypes != null && !allowedTypes.isEmpty()) {
            boolean allowed = allowedTypes.stream()
                .anyMatch(type -> type.equalsIgnoreCase(contentType));
            if (!allowed) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported content type");
            }
        }
    }
}
