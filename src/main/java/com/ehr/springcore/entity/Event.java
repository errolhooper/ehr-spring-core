package com.ehr.springcore.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    /**
     * Event properties stored as key-value pairs.
     * Property values are stored in PostgreSQL TEXT columns which have no explicit size limit
     * (up to 1GB in practice). For very large property values, consider storing references
     * to external storage instead.
     */
    @ElementCollection
    @CollectionTable(name = "event_properties", joinColumns = @JoinColumn(name = "event_id"))
    @MapKeyColumn(name = "property_key")
    @Column(name = "property_value", columnDefinition = "TEXT")
    private Map<String, String> properties = new HashMap<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // Constructors
    public Event() {
    }

    public Event(String eventName, Instant timestamp, Map<String, String> properties) {
        this.eventName = eventName;
        this.timestamp = timestamp;
        this.properties = properties != null ? properties : new HashMap<>();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
