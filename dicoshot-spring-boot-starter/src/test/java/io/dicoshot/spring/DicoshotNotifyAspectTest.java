package io.dicoshot.spring;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.dicoshot.core.message.DiscordEmbed;
import io.dicoshot.core.message.DiscordMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class DicoshotNotifyAspectTest {

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
                .withConfiguration(AutoConfigurations.of(DicoshotAutoConfiguration.class))
                .withUserConfiguration(NotifyConfig.class)
                .withPropertyValues("dicoshot.webhook-url=http://localhost:" + wireMock.port() + "/webhook");
    }

    @Test
    void registersAspect() {
        runner().run(ctx -> assertThat(ctx).hasSingleBean(DicoshotNotifyAspect.class));
    }

    @Test
    void sendsReturnedMessage() {
        runner().run(ctx -> {
            ctx.getBean(NotifyService.class).notifyAlways();
            wireMock.verify(postRequestedFor(urlEqualTo("/webhook")));
        });
    }

    @Test
    void skipsWhenReturningNull() {
        runner().run(ctx -> {
            ctx.getBean(NotifyService.class).notifyNever();
            wireMock.verify(exactly(0), postRequestedFor(urlEqualTo("/webhook")));
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAspectJAutoProxy
    static class NotifyConfig {
        @Bean
        NotifyService notifyService() {
            return new NotifyService();
        }
    }

    static class NotifyService {

        @DicoshotNotify
        DiscordMessage notifyAlways() {
            return DiscordMessage.builder()
                    .addEmbed(DiscordEmbed.builder().title("hello").build())
                    .build();
        }

        @DicoshotNotify
        DiscordMessage notifyNever() {
            return null;
        }
    }
}