package com.ehr.springcore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cookie-scanner")
public class CookieScannerProperties {

    private String pythonScriptPath = "src/main/python/cookie_reader.py";
    private int timeoutSeconds = 30;

    public String getPythonScriptPath() {
        return pythonScriptPath;
    }

    public void setPythonScriptPath(String pythonScriptPath) {
        this.pythonScriptPath = pythonScriptPath;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
