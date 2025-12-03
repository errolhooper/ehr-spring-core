package com.ehr.springcore.controller;

import com.ehr.springcore.model.CookieRequest;
import com.ehr.springcore.model.CookieScanResponse;
import com.ehr.springcore.service.CookieScanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CookieScanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CookieScanService cookieScanService;

    @Test
    void testScanWebsite_Success() throws Exception {
        // Arrange
        CookieRequest request = new CookieRequest("https://www.example.com");
        
        CookieScanResponse mockResponse = new CookieScanResponse(true, "Cookie scan completed successfully");
        mockResponse.setUrl("https://www.example.com");
        mockResponse.setFinalUrl("https://www.example.com/");
        mockResponse.setStatusCode(200);
        mockResponse.setCookieCount(3);
        mockResponse.setCookies(new ArrayList<>());
        mockResponse.setTimestamp(Instant.now());
        
        when(cookieScanService.scanWebsiteForCookies(any(CookieRequest.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/cookies/scan")
                        .header("X-API-Key", "default-api-key-change-in-production")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.url").value("https://www.example.com"))
                .andExpect(jsonPath("$.cookieCount").value(3))
                .andExpect(jsonPath("$.message").value("Cookie scan completed successfully"));
    }

    @Test
    void testScanWebsite_InvalidUrl() throws Exception {
        // Arrange
        CookieRequest request = new CookieRequest("invalid-url");

        // Act & Assert
        mockMvc.perform(post("/api/v1/cookies/scan")
                        .header("X-API-Key", "default-api-key-change-in-production")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testScanWebsite_MissingUrl() throws Exception {
        // Arrange
        CookieRequest request = new CookieRequest("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/cookies/scan")
                        .header("X-API-Key", "default-api-key-change-in-production")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testScanWebsite_Unauthorized() throws Exception {
        // Arrange
        CookieRequest request = new CookieRequest("https://www.example.com");

        // Act & Assert
        mockMvc.perform(post("/api/v1/cookies/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testScanWebsite_Failed() throws Exception {
        // Arrange
        CookieRequest request = new CookieRequest("https://www.example.com");
        
        CookieScanResponse mockResponse = new CookieScanResponse(false, "Cookie scan failed");
        mockResponse.setUrl("https://www.example.com");
        mockResponse.setError("Connection timeout");
        mockResponse.setTimestamp(Instant.now());
        
        when(cookieScanService.scanWebsiteForCookies(any(CookieRequest.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/cookies/scan")
                        .header("X-API-Key", "default-api-key-change-in-production")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Connection timeout"));
    }
}
