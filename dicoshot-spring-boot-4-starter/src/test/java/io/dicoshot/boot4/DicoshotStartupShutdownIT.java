package io.dicoshot.boot4;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class DicoshotStartupShutdownIT {

    private static WireMockServer wireMock;

    @SpringBootApplication
    static class TestApp {
    }

    @BeforeAll
    static void start() {
        wireMock = new WireMockServer(wireMockConfig().dynamicPort());
        wireMock.start();
        wireMock.stubFor(post(urlEqualTo("/webhook")).willReturn(aResponse().withStatus(204)));
    }

    @AfterAll
    static void stop() {
        wireMock.stop();
    }

    @BeforeEach
    void resetRequests() {
        wireMock.resetRequests();
    }

    @Test
    void startupAndShutdownEventsBothFireWebhook() {
        ConfigurableApplicationContext ctx = new SpringApplication(TestApp.class).run(
                "--spring.main.web-application-type=none",
                "--dicoshot.webhook-url=" + wireMock.baseUrl() + "/webhook",
                "--dicoshot.application-name=integration-test");

        int countAfterStartup = wireMock.findAll(postRequestedFor(urlEqualTo("/webhook"))).size();
        assertThat(countAfterStartup).isEqualTo(1);

        ctx.close();

        int countAfterShutdown = wireMock.findAll(postRequestedFor(urlEqualTo("/webhook"))).size();
        assertThat(countAfterShutdown).isEqualTo(2);
    }

    @Test
    void notifyOnStartupFalseSuppressesStartupCall() {
        ConfigurableApplicationContext ctx = new SpringApplication(TestApp.class).run(
                "--spring.main.web-application-type=none",
                "--dicoshot.webhook-url=" + wireMock.baseUrl() + "/webhook",
                "--dicoshot.notify-on-startup=false",
                "--dicoshot.notify-on-shutdown=false");
        try {
            assertThat(wireMock.findAll(postRequestedFor(urlEqualTo("/webhook")))).isEmpty();
        } finally {
            ctx.close();
        }
    }

    @Test
    void webhookFailureDoesNotBreakStartup() {
        wireMock.stubFor(post(urlEqualTo("/webhook")).willReturn(aResponse().withStatus(500)));
        try {
            ConfigurableApplicationContext ctx = new SpringApplication(TestApp.class).run(
                    "--spring.main.web-application-type=none",
                    "--dicoshot.webhook-url=" + wireMock.baseUrl() + "/webhook",
                    "--dicoshot.notify-on-shutdown=false");
            assertThat(ctx.isRunning()).isTrue();
            ctx.close();
        } finally {
            wireMock.stubFor(post(urlEqualTo("/webhook")).willReturn(aResponse().withStatus(204)));
        }
    }

    @Test
    void startupBodyContainsApplicationName() {
        ConfigurableApplicationContext ctx = new SpringApplication(TestApp.class).run(
                "--spring.main.web-application-type=none",
                "--dicoshot.webhook-url=" + wireMock.baseUrl() + "/webhook",
                "--dicoshot.application-name=my-svc",
                "--dicoshot.notify-on-shutdown=false");
        try {
            assertThat(wireMock.findAll(postRequestedFor(urlEqualTo("/webhook"))))
                    .anyMatch(req -> req.getBodyAsString().contains("my-svc"));
        } finally {
            ctx.close();
        }
    }
}
