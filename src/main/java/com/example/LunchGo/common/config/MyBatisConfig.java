package com.example.LunchGo.common.config;

import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class MyBatisConfig {

    @Bean
    public ConfigurationCustomizer myBatisConfigurationCustomizer() {
        return new ConfigurationCustomizer() {
            @Override
            public void customize(Configuration configuration) {
                configuration.setMapUnderscoreToCamelCase(true);
                // Enable cache to reduce repeated reads for identical queries.
                configuration.setCacheEnabled(true);
                // Allow lazy loading; fetch related data only when accessed.
                configuration.setLazyLoadingEnabled(true);
                // Avoid eager cascading; only load when the property is accessed.
                configuration.setAggressiveLazyLoading(false);
                // Sensible default fetch size for cursor-based reads.
                configuration.setDefaultFetchSize(100);
            }
        };
    }
}
