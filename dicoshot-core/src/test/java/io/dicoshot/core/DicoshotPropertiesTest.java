package io.dicoshot.core;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DicoshotPropertiesTest {

    @Test
    void defaultsAreSensible() {
        DicoshotProperties p = DicoshotProperties.builder()
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
        assertThatThrownBy(() -> DicoshotProperties.builder().webhookUrl(" ").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("webhookUrl");
    }

    @Test
    void missingWebhookUrlIsRejected() {
        assertThatThrownBy(() -> DicoshotProperties.builder().build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
