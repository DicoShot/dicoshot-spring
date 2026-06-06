package io.dicoshot.core;

import java.time.Duration;

public final class DicoshotProperties {

    private final String webhookUrl;
    private final boolean enabled;
    private final boolean notifyOnStartup;
    private final boolean notifyOnShutdown;
    private final String applicationName;
    private final String username;
    private final Duration timeout;

    private DicoshotProperties(Builder b) {
        this.webhookUrl = b.webhookUrl;
        this.enabled = b.enabled;
        this.notifyOnStartup = b.notifyOnStartup;
        this.notifyOnShutdown = b.notifyOnShutdown;
        this.applicationName = b.applicationName;
        this.username = b.username;
        this.timeout = b.timeout;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isNotifyOnStartup() {
        return notifyOnStartup;
    }

    public boolean isNotifyOnShutdown() {
        return notifyOnShutdown;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getUsername() {
        return username;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String webhookUrl;
        private boolean enabled = true;
        private boolean notifyOnStartup = true;
        private boolean notifyOnShutdown = true;
        private String applicationName;
        private String username;
        private Duration timeout = Duration.ofSeconds(5);

        public Builder webhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder notifyOnStartup(boolean notifyOnStartup) {
            this.notifyOnStartup = notifyOnStartup;
            return this;
        }

        public Builder notifyOnShutdown(boolean notifyOnShutdown) {
            this.notifyOnShutdown = notifyOnShutdown;
            return this;
        }

        public Builder applicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public DicoshotProperties build() {
            if (webhookUrl == null || webhookUrl.isBlank()) {
                throw new IllegalArgumentException("webhookUrl must not be blank");
            }
            return new DicoshotProperties(this);
        }
    }
}
