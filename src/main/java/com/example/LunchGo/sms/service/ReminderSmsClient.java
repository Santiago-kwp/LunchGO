package com.example.LunchGo.sms.service;

import com.example.LunchGo.sms.config.ReminderProperties;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReminderSmsClient {

    private static final String BASE_URL = "https://sens.apigw.ntruss.com";

    private final ReminderProperties props;
    private final RestTemplateBuilder restTemplateBuilder;

    public Map<String, Object> sendSms(String to, String content) {
        String uri = "/sms/v2/services/" + props.getServiceId() + "/messages";
        String url = BASE_URL + uri;

        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = makeSignature("POST", uri, timestamp, props.getAccessKey(), props.getSecretKey());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-ncp-apigw-timestamp", timestamp);
        headers.set("x-ncp-iam-access-key", props.getAccessKey());
        headers.set("x-ncp-apigw-signature-v2", signature);

        Map<String, Object> body = Map.of(
                "type", props.getDefaultType(),
                "contentType", "COMM",
                "countryCode", "82",
                "from", props.getFrom(),
                "content", content,
                "messages", List.of(Map.of(
                        "to", normalizePhone(to),
                        "content", content
                ))
        );

        RestTemplate rt = restTemplateBuilder.build();
        return rt.postForObject(url, new HttpEntity<>(body, headers), Map.class);
    }

    private static String normalizePhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^0-9]", "");
    }

    static String makeSignature(String method, String uri, String timestamp, String accessKey, String secretKey) {
        try {
            String message = method + " " + uri + "\n" + timestamp + "\n" + accessKey;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create NCP signature", e);
        }
    }
}
