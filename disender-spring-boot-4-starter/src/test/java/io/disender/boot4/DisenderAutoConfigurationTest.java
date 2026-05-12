package io.disender.boot4;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.disender.core.DisenderClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class DisenderAutoConfigurationTest {

    private WireMockServer wireMock;

    @BeforeEach
    void start() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        wireMock.stubFor(post(urlEqualTo("/webhook")).willReturn(aResponse().withStatus(204)));
    }

    @AfterEach
    void stop() {
        wireMock.stop();
    }

    private ApplicationContextRunner runner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(DisenderAutoConfiguration.class));
    }

    @Test
    void doesNotConfigureWhenWebhookUrlMissing() {
        runner().run(ctx -> assertThat(ctx).doesNotHaveBean(DisenderClient.class));
    }

    @Test
    void doesNotConfigureWhenDisabled() {
        runner()
                .withPropertyValues(
                        "disender.webhook-url=http://localhost:" + wireMock.port() + "/webhook",
                        "disender.enabled=false")
                .run(ctx -> assertThat(ctx).doesNotHaveBean(DisenderClient.class));
    }

    @Test
    void registersBeansWhenWebhookUrlPresent() {
        runner()
                .withPropertyValues("disender.webhook-url=http://localhost:" + wireMock.port() + "/webhook")
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(DisenderClient.class);
                    assertThat(ctx).hasSingleBean(DisenderEventListener.class);
                });
    }

    @Test
    void sendStartupMessageHitsWebhook() {
        runner()
                .withPropertyValues(
                        "disender.webhook-url=http://localhost:" + wireMock.port() + "/webhook",
                        "disender.application-name=test-app")
                .run(ctx -> {
                    DisenderClient client = ctx.getBean(DisenderClient.class);
                    client.send(ctx.getBean(io.disender.core.message.MessageFactory.class).startup());
                    wireMock.verify(postRequestedFor(urlEqualTo("/webhook")));
                });
    }
}
