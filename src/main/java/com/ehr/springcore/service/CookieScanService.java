package com.ehr.springcore.service;

import com.ehr.springcore.config.CookieScannerProperties;
import com.ehr.springcore.entity.CookieScan;
import com.ehr.springcore.model.CookieRequest;
import com.ehr.springcore.model.CookieScanResponse;
import com.ehr.springcore.repository.CookieScanRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class CookieScanService {

    private static final Logger logger = LoggerFactory.getLogger(CookieScanService.class);

    private final CookieScanRepository cookieScanRepository;
    private final ObjectMapper objectMapper;
    private final CookieScannerProperties properties;

    public CookieScanService(CookieScanRepository cookieScanRepository, 
                           ObjectMapper objectMapper,
                           CookieScannerProperties properties) {
        this.cookieScanRepository = cookieScanRepository;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public CookieScanResponse scanWebsiteForCookies(CookieRequest request) {
        logger.info("Starting cookie scan for URL: {}", request.getUrl());

        try {
            // Validate URL to prevent command injection
            String validatedUrl = validateUrl(request.getUrl());
            if (validatedUrl == null) {
                logger.error("Invalid URL format: {}", request.getUrl());
                return createErrorResponse(request.getUrl(), "Invalid URL format");
            }

            // Find the Python script path
            String scriptPath = findPythonScript();
            if (scriptPath == null) {
                logger.error("Python script not found at: {}", properties.getPythonScriptPath());
                return createErrorResponse(request.getUrl(), "Cookie reader script not found");
            }

            // Execute the Python script with validated URL
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python3",
                    scriptPath,
                    validatedUrl
            );
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            boolean completed = process.waitFor(properties.getTimeoutSeconds(), TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                logger.error("Python process timed out for URL: {}", request.getUrl());
                return createErrorResponse(request.getUrl(), "Request timeout - process took too long");
            }

            // Read the output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.exitValue();
            String jsonOutput = output.toString().trim();

            logger.info("Python script completed with exit code: {} for URL: {}", exitCode, request.getUrl());

            // Parse the JSON response from Python
            CookieScanResponse response = objectMapper.readValue(jsonOutput, CookieScanResponse.class);
            response.setTimestamp(Instant.now());

            // Save to database
            saveScanResult(response);

            if (response.isSuccess()) {
                response.setMessage("Cookie scan completed successfully");
                logger.info("Successfully scanned {} cookies from {}", response.getCookieCount(), request.getUrl());
            } else {
                logger.warn("Cookie scan failed for {}: {}", request.getUrl(), response.getError());
            }

            return response;

        } catch (Exception e) {
            logger.error("Error scanning website for cookies: {}", request.getUrl(), e);
            return createErrorResponse(request.getUrl(), "Failed to scan website: " + e.getMessage());
        }
    }

    private String validateUrl(String url) {
        try {
            // Parse and validate the URL
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            
            // Only allow http and https schemes
            if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
                return null;
            }
            
            // Ensure host is present
            if (uri.getHost() == null || uri.getHost().isEmpty()) {
                return null;
            }
            
            // Return the validated URL
            return uri.toString();
        } catch (Exception e) {
            logger.error("URL validation failed for: {}", url, e);
            return null;
        }
    }

    private String findPythonScript() {
        // Use configured path
        File file = new File(properties.getPythonScriptPath());
        if (file.exists() && file.canRead()) {
            logger.debug("Found Python script at: {}", file.getAbsolutePath());
            return file.getAbsolutePath();
        }

        // Try relative to current directory as fallback
        File fallbackFile = new File("./" + properties.getPythonScriptPath());
        if (fallbackFile.exists() && fallbackFile.canRead()) {
            logger.debug("Found Python script at fallback location: {}", fallbackFile.getAbsolutePath());
            return fallbackFile.getAbsolutePath();
        }

        logger.error("Python script not found at configured path: {}", properties.getPythonScriptPath());
        return null;
    }

    private CookieScanResponse createErrorResponse(String url, String errorMessage) {
        CookieScanResponse response = new CookieScanResponse(false, "Cookie scan failed");
        response.setUrl(url);
        response.setError(errorMessage);
        response.setTimestamp(Instant.now());

        // Save error result to database
        saveScanResult(response);

        return response;
    }

    private void saveScanResult(CookieScanResponse response) {
        try {
            CookieScan scan = new CookieScan();
            scan.setUrl(response.getUrl());
            scan.setFinalUrl(response.getFinalUrl());
            scan.setSuccess(response.isSuccess());
            scan.setStatusCode(response.getStatusCode());
            scan.setCookieCount(response.getCookieCount());
            scan.setError(response.getError());
            scan.setTimestamp(response.getTimestamp());

            // Convert cookies list to JSON string for storage
            if (response.getCookies() != null) {
                scan.setCookiesJson(objectMapper.writeValueAsString(response.getCookies()));
            }

            cookieScanRepository.save(scan);
            logger.debug("Saved cookie scan result to database for URL: {}", response.getUrl());
        } catch (Exception e) {
            logger.error("Failed to save cookie scan result to database", e);
        }
    }
}
