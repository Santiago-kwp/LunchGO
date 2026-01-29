package com.example.LunchGo.image.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Configuration
@EnableConfigurationProperties(LocalImageStorageProperties.class)
@RequiredArgsConstructor
public class LocalImageStorageConfig {

    private final LocalImageStorageProperties properties;

    @PostConstruct
    public void init() {
        Path baseDir = Paths.get(properties.getBaseDir());
        if (!Files.exists(baseDir)) {
            try {
                Files.createDirectories(baseDir);
                log.info("Created image storage base directory: {}", baseDir);
            } catch (IOException e) {
                log.error("Failed to create image storage base directory: {}", baseDir, e);
                throw new IllegalStateException("Cannot create image storage directory", e);
            }
        }
    }
}
