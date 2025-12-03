package com.ehr.springcore.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request to scan a website for cookies")
public class CookieRequest {

    @NotBlank(message = "URL is required")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    @Schema(description = "The URL of the website to scan for cookies", example = "https://www.example.com")
    private String url;

    public CookieRequest() {
    }

    public CookieRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "CookieRequest{" +
                "url='" + url + '\'' +
                '}';
    }
}
