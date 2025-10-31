package com.ehr.springcore.controller;

import com.ehr.springcore.model.EventRequest;
import com.ehr.springcore.model.MetricRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${security.api-key}")
    private String apiKey;

    @Test
    void testIngestEvent_Success() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("userId", "123");
        properties.put("action", "login");

        EventRequest event = new EventRequest("user.login", Instant.now(), properties);

        mockMvc.perform(post("/api/v1/ingest/events")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Event ingested successfully"));
    }

    @Test
    void testIngestEvent_MissingApiKey() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("userId", "123");

        EventRequest event = new EventRequest("user.login", Instant.now(), properties);

        mockMvc.perform(post("/api/v1/ingest/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testIngestEvent_InvalidRequest() throws Exception {
        EventRequest event = new EventRequest(null, null, null);

        mockMvc.perform(post("/api/v1/ingest/events")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void testIngestMetric_Success() throws Exception {
        MetricRequest metric = new MetricRequest("cpu.usage", 75.5, Instant.now(), "percent");

        mockMvc.perform(post("/api/v1/ingest/metrics")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metric)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Metric ingested successfully"));
    }

    @Test
    void testIngestMetric_InvalidRequest() throws Exception {
        MetricRequest metric = new MetricRequest(null, null, null, null);

        mockMvc.perform(post("/api/v1/ingest/metrics")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metric)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }
}
