package com.ehr.springcore.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "cookie_scans")
public class CookieScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(length = 2048)
    private String finalUrl;

    @Column(nullable = false)
    private boolean success;

    private Integer statusCode;

    private Integer cookieCount;

    @Column(columnDefinition = "TEXT")
    private String cookiesJson;

    @Column(length = 1024)
    private String error;

    @Column(nullable = false)
    private Instant timestamp;

    public CookieScan() {
        this.timestamp = Instant.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFinalUrl() {
        return finalUrl;
    }

    public void setFinalUrl(String finalUrl) {
        this.finalUrl = finalUrl;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getCookieCount() {
        return cookieCount;
    }

    public void setCookieCount(Integer cookieCount) {
        this.cookieCount = cookieCount;
    }

    public String getCookiesJson() {
        return cookiesJson;
    }

    public void setCookiesJson(String cookiesJson) {
        this.cookiesJson = cookiesJson;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
