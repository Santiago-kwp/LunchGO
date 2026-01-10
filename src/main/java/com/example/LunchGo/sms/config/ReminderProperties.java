package com.example.LunchGo.sms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ncp.sens")
public class ReminderProperties {
    private String accessKey;
    private String secretKey;
    private String serviceId;
    private String from;
    private String defaultType = "SMS"; // SMS/LMS
}
