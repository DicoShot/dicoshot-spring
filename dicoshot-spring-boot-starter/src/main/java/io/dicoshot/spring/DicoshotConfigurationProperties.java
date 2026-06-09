package io.dicoshot.spring;

import io.dicoshot.core.DicoshotProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "dicoshot")
public class DicoshotConfigurationProperties {

    private String webhookUrl;
    private boolean enabled = true;
    private boolean notifyOnStartup = true;
    private boolean notifyOnShutdown = true;
    private String applicationName;
    private String username;
    private Duration timeout = Duration.ofSeconds(5);

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isNotifyOnStartup() {
        return notifyOnStartup;
    }

    public void setNotifyOnStartup(boolean notifyOnStartup) {
        this.notifyOnStartup = notifyOnStartup;
    }

    public boolean isNotifyOnShutdown() {
        return notifyOnShutdown;
    }

    public void setNotifyOnShutdown(boolean notifyOnShutdown) {
        this.notifyOnShutdown = notifyOnShutdown;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public DicoshotProperties toCoreProperties(String fallbackApplicationName) {
        String resolvedName = (applicationName != null && !applicationName.isBlank())
                ? applicationName
                : fallbackApplicationName;
        return DicoshotProperties.builder()
                .webhookUrl(webhookUrl)
                .enabled(enabled)
                .notifyOnStartup(notifyOnStartup)
                .notifyOnShutdown(notifyOnShutdown)
                .applicationName(resolvedName)
                .username(username)
                .timeout(timeout)
                .build();
    }
}
