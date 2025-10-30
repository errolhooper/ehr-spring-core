package com.ehr.springcore.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private static final Logger logger = LoggerFactory.getLogger(SecurityProperties.class);
    private static final String DEFAULT_API_KEY = "default-api-key-change-in-production";

    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @PostConstruct
    public void validateConfig() {
        if (DEFAULT_API_KEY.equals(apiKey)) {
            logger.warn("***************************************************************");
            logger.warn("WARNING: Using default API key! This is NOT secure for production!");
            logger.warn("Please set the API_KEY environment variable to a secure value.");
            logger.warn("***************************************************************");
        }
    }
}
