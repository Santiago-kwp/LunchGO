package com.example.LunchGo.reservation.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.WebRequest;

@Service
public class PortoneWebhookVerifier {
    private static final String HMAC_SHA256 = "HmacSHA256";

    @Value("${portone.webhook-secret:}")
    private String webhookSecret;

    public void verify(String rawBody, WebRequest request) {
        if (!StringUtils.hasText(webhookSecret)) {
            throw new IllegalStateException("PortOne Webhook Secret이 설정되지 않았습니다.");
        }

        String webhookId = headerValue(request, "webhook-id");
        String webhookTimestamp = headerValue(request, "webhook-timestamp");
        String webhookSignature = headerValue(request, "webhook-signature");

        if (!StringUtils.hasText(webhookId) ||
            !StringUtils.hasText(webhookTimestamp) ||
            !StringUtils.hasText(webhookSignature)) {
            throw new IllegalArgumentException("웹훅 서명 검증에 필요한 헤더가 누락되었습니다.");
        }

        String payload = webhookId + "." + webhookTimestamp + "." + rawBody;
        String expectedSignature = computeSignature(payload, webhookSecret);

        List<String> signatures = parseSignatures(webhookSignature);
        boolean matched = signatures.stream().anyMatch(sig -> secureEquals(sig, expectedSignature));
        if (!matched) {
            throw new IllegalArgumentException("웹훅 서명이 일치하지 않습니다.");
        }
    }

    private String headerValue(WebRequest request, String name) {
        String value = request.getHeader(name);
        if (value != null) {
            return value;
        }
        return request.getHeader(name.toUpperCase());
    }

    private String computeSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("웹훅 서명 생성에 실패했습니다.");
        }
    }

    private List<String> parseSignatures(String headerValue) {
        String[] parts = headerValue.split(" ");
        List<String> signatures = new ArrayList<>();
        for (String part : parts) {
            String[] kv = part.split(",", 2);
            if (kv.length == 2 && "v1".equalsIgnoreCase(kv[0])) {
                signatures.add(kv[1]);
            }
        }
        if (signatures.isEmpty()) {
            for (String part : headerValue.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    signatures.add(trimmed);
                }
            }
        }
        return signatures;
    }

    private boolean secureEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        if (aBytes.length != bBytes.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        return result == 0;
    }
}
