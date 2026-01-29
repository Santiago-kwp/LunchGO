package com.example.LunchGo.image.config;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "image.storage")
public class LocalImageStorageProperties {
    private String baseDir;
    private String baseUrl;
    private long maxSizeBytes;
    private List<String> allowedContentTypes;
}
