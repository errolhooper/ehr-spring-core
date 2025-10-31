package com.ehr.springcore.service;

import com.ehr.springcore.entity.Event;
import com.ehr.springcore.entity.Metric;
import com.ehr.springcore.model.EventRequest;
import com.ehr.springcore.model.MetricRequest;
import com.ehr.springcore.repository.EventRepository;
import com.ehr.springcore.repository.MetricRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    private final EventRepository eventRepository;
    private final MetricRepository metricRepository;
    private final ObjectMapper objectMapper;

    public DatabaseService(EventRepository eventRepository, MetricRepository metricRepository, ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.metricRepository = metricRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Event saveEvent(EventRequest eventRequest) {
        logger.info("Persisting event to database: {}", eventRequest.getEventName());
        
        // Convert Map<String, Object> to Map<String, String>
        // For complex objects, use JSON serialization for data integrity
        Map<String, String> stringProperties = new HashMap<>();
        if (eventRequest.getProperties() != null) {
            eventRequest.getProperties().forEach((key, value) -> {
                if (value == null) {
                    stringProperties.put(key, null);
                } else if (value instanceof String) {
                    stringProperties.put(key, (String) value);
                } else if (value instanceof Number || value instanceof Boolean) {
                    stringProperties.put(key, value.toString());
                } else {
                    // Serialize complex objects to JSON
                    try {
                        stringProperties.put(key, objectMapper.writeValueAsString(value));
                    } catch (JsonProcessingException e) {
                        logger.warn("Failed to serialize property '{}', using toString(): {}", key, e.getMessage());
                        stringProperties.put(key, value.toString());
                    }
                }
            });
        }
        
        Event event = new Event(
            eventRequest.getEventName(),
            eventRequest.getTimestamp(),
            stringProperties
        );
        
        Event savedEvent = eventRepository.save(event);
        logger.info("Event persisted with ID: {}", savedEvent.getId());
        
        return savedEvent;
    }

    @Transactional
    public Metric saveMetric(MetricRequest metricRequest) {
        logger.info("Persisting metric to database: {}", metricRequest.getMetricName());
        
        Metric metric = new Metric(
            metricRequest.getMetricName(),
            metricRequest.getValue(),
            metricRequest.getTimestamp(),
            metricRequest.getUnit()
        );
        
        Metric savedMetric = metricRepository.save(metric);
        logger.info("Metric persisted with ID: {}", savedMetric.getId());
        
        return savedMetric;
    }
}
