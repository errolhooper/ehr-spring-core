package com.ehr.springcore.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

public class EventRequest {

    @NotBlank(message = "Event name is required")
    private String eventName;

    @NotNull(message = "Timestamp is required")
    private Instant timestamp;

    private Map<String, Object> properties;

    public EventRequest() {
    }

    public EventRequest(String eventName, Instant timestamp, Map<String, Object> properties) {
        this.eventName = eventName;
        this.timestamp = timestamp;
        this.properties = properties;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "EventRequest{" +
                "eventName='" + eventName + '\'' +
                ", timestamp=" + timestamp +
                ", properties=" + properties +
                '}';
    }
}
