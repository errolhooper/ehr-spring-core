package com.ehr.springcore.service;

import com.ehr.springcore.model.EventRequest;
import com.ehr.springcore.model.MetricRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IngestionService {

    private static final Logger logger = LoggerFactory.getLogger(IngestionService.class);
    
    private final PayloadStorageService storageService;

    public IngestionService(PayloadStorageService storageService) {
        this.storageService = storageService;
    }

    public void ingestEvent(EventRequest event) {
        logger.info("Ingesting event: {}", event.getEventName());
        storageService.storePayload("EVENT", event);
    }

    public void ingestMetric(MetricRequest metric) {
        logger.info("Ingesting metric: {}", metric.getMetricName());
        storageService.storePayload("METRIC", metric);
    }
}
