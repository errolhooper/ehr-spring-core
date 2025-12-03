package com.ehr.springcore.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Response containing cookie scan results")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CookieScanResponse {

    @Schema(description = "Whether the scan was successful", example = "true")
    private boolean success;

    @Schema(description = "The URL that was scanned", example = "https://www.example.com")
    private String url;

    @Schema(description = "The final URL after redirects", example = "https://www.example.com/")
    private String finalUrl;

    @Schema(description = "HTTP status code of the response", example = "200")
    private Integer statusCode;

    @Schema(description = "Number of cookies found", example = "5")
    private Integer cookieCount;

    @Schema(description = "List of cookies found")
    private List<CookieData> cookies;

    @Schema(description = "Timestamp when the scan was performed")
    private Instant timestamp;

    @Schema(description = "Error message if scan failed")
    private String error;

    @Schema(description = "Message about the result")
    private String message;

    public CookieScanResponse() {
    }

    public CookieScanResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = Instant.now();
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    public List<CookieData> getCookies() {
        return cookies;
    }

    public void setCookies(List<CookieData> cookies) {
        this.cookies = cookies;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Schema(description = "Cookie data structure")
    public static class CookieData {
        @Schema(description = "Cookie name", example = "session_id")
        private String name;

        @Schema(description = "Cookie value", example = "abc123xyz")
        private String value;

        @Schema(description = "Cookie domain", example = ".example.com")
        private String domain;

        @Schema(description = "Cookie path", example = "/")
        private String path;

        @Schema(description = "Whether the cookie is secure", example = "true")
        private boolean secure;

        @Schema(description = "Whether the cookie is HTTP only", example = "true")
        private boolean httpOnly;

        @Schema(description = "Cookie expiration date")
        private String expires;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isSecure() {
            return secure;
        }

        public void setSecure(boolean secure) {
            this.secure = secure;
        }

        public boolean isHttpOnly() {
            return httpOnly;
        }

        public void setHttpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
        }

        public String getExpires() {
            return expires;
        }

        public void setExpires(String expires) {
            this.expires = expires;
        }
    }
}
