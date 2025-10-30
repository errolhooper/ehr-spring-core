package com.ehr.springcore.service;

import com.ehr.springcore.config.LoggingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PayloadStorageService {

    private static final Logger logger = LoggerFactory.getLogger(PayloadStorageService.class);
    
    private final LoggingProperties loggingProperties;
    private final List<String> payloads = Collections.synchronizedList(new ArrayList<>());

    public PayloadStorageService(LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    public void storePayload(String type, Object payload) {
        if (!loggingProperties.isEnabled()) {
            return;
        }

        String logEntry = String.format("[%s] %s", type, payload.toString());
        logger.info("Storing payload: {}", logEntry);

        if (payloads.size() >= loggingProperties.getMaxSize()) {
            payloads.remove(0);
        }
        payloads.add(logEntry);
    }

    public List<String> getPayloads() {
        return new ArrayList<>(payloads);
    }

    public int getPayloadCount() {
        return payloads.size();
    }
}
