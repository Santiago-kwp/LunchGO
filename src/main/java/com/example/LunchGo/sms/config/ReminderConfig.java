package com.example.LunchGo.sms.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ReminderProperties.class)
public class ReminderConfig {
}
