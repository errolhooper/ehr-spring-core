package com.ehr.springcore.service;

import com.ehr.springcore.entity.Event;
import com.ehr.springcore.entity.Metric;
import com.ehr.springcore.model.EventRequest;
import com.ehr.springcore.model.MetricRequest;
import com.ehr.springcore.repository.EventRepository;
import com.ehr.springcore.repository.MetricRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DatabaseServiceTest {

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MetricRepository metricRepository;

    @Test
    void testSaveEvent_Success() {
        // Arrange
        Map<String, Object> properties = new HashMap<>();
        properties.put("userId", "123");
        properties.put("action", "login");
        
        EventRequest eventRequest = new EventRequest(
            "user.login",
            Instant.now(),
            properties
        );

        // Act
        Event savedEvent = databaseService.saveEvent(eventRequest);

        // Assert
        assertNotNull(savedEvent);
        assertNotNull(savedEvent.getId());
        assertEquals("user.login", savedEvent.getEventName());
        assertEquals("123", savedEvent.getProperties().get("userId"));
        assertEquals("login", savedEvent.getProperties().get("action"));
        assertNotNull(savedEvent.getCreatedAt());
    }

    @Test
    void testSaveEvent_WithNullProperties() {
        // Arrange
        EventRequest eventRequest = new EventRequest(
            "simple.event",
            Instant.now(),
            null
        );

        // Act
        Event savedEvent = databaseService.saveEvent(eventRequest);

        // Assert
        assertNotNull(savedEvent);
        assertNotNull(savedEvent.getId());
        assertEquals("simple.event", savedEvent.getEventName());
        assertTrue(savedEvent.getProperties().isEmpty());
    }

    @Test
    void testSaveMetric_Success() {
        // Arrange
        MetricRequest metricRequest = new MetricRequest(
            "cpu.usage",
            75.5,
            Instant.now(),
            "percent"
        );

        // Act
        Metric savedMetric = databaseService.saveMetric(metricRequest);

        // Assert
        assertNotNull(savedMetric);
        assertNotNull(savedMetric.getId());
        assertEquals("cpu.usage", savedMetric.getMetricName());
        assertEquals(75.5, savedMetric.getValue());
        assertEquals("percent", savedMetric.getUnit());
        assertNotNull(savedMetric.getCreatedAt());
    }

    @Test
    void testSaveMetric_WithNullUnit() {
        // Arrange
        MetricRequest metricRequest = new MetricRequest(
            "request.count",
            100.0,
            Instant.now(),
            null
        );

        // Act
        Metric savedMetric = databaseService.saveMetric(metricRequest);

        // Assert
        assertNotNull(savedMetric);
        assertNotNull(savedMetric.getId());
        assertEquals("request.count", savedMetric.getMetricName());
        assertEquals(100.0, savedMetric.getValue());
        assertNull(savedMetric.getUnit());
    }

    @Test
    void testFindEventsByName() {
        // Arrange
        EventRequest eventRequest = new EventRequest(
            "test.event",
            Instant.now(),
            new HashMap<>()
        );
        databaseService.saveEvent(eventRequest);

        // Act
        List<Event> events = eventRepository.findByEventName("test.event");

        // Assert
        assertFalse(events.isEmpty());
        assertEquals("test.event", events.get(0).getEventName());
    }

    @Test
    void testFindMetricsByName() {
        // Arrange
        MetricRequest metricRequest = new MetricRequest(
            "test.metric",
            50.0,
            Instant.now(),
            "units"
        );
        databaseService.saveMetric(metricRequest);

        // Act
        List<Metric> metrics = metricRepository.findByMetricName("test.metric");

        // Assert
        assertFalse(metrics.isEmpty());
        assertEquals("test.metric", metrics.get(0).getMetricName());
        assertEquals(50.0, metrics.get(0).getValue());
    }
}
