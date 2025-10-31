package com.ehr.springcore.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class MetricRequest {

    @NotBlank(message = "Metric name is required")
    private String metricName;

    @NotNull(message = "Value is required")
    private Double value;

    @NotNull(message = "Timestamp is required")
    private Instant timestamp;

    private String unit;

    public MetricRequest() {
    }

    public MetricRequest(String metricName, Double value, Instant timestamp, String unit) {
        this.metricName = metricName;
        this.value = value;
        this.timestamp = timestamp;
        this.unit = unit;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "MetricRequest{" +
                "metricName='" + metricName + '\'' +
                ", value=" + value +
                ", timestamp=" + timestamp +
                ", unit='" + unit + '\'' +
                '}';
    }
}
