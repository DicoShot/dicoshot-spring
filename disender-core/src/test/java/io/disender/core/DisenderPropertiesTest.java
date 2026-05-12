package io.disender.core;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisenderPropertiesTest {

    @Test
    void defaultsAreSensible() {
        DisenderProperties p = DisenderProperties.builder()
                .webhookUrl("https://discord.com/api/webhooks/x/y")
                .build();

        assertThat(p.isEnabled()).isTrue();
        assertThat(p.isNotifyOnStartup()).isTrue();
        assertThat(p.isNotifyOnShutdown()).isTrue();
        assertThat(p.getTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(p.getApplicationName()).isNull();
        assertThat(p.getUsername()).isNull();
    }

    @Test
    void blankWebhookUrlIsRejected() {
        assertThatThrownBy(() -> DisenderProperties.builder().webhookUrl(" ").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("webhookUrl");
    }

    @Test
    void missingWebhookUrlIsRejected() {
        assertThatThrownBy(() -> DisenderProperties.builder().build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
