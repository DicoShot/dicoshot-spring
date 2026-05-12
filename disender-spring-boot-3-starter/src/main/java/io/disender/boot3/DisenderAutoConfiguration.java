package io.disender.boot3;

import io.disender.core.DisenderClient;
import io.disender.core.DisenderProperties;
import io.disender.core.message.MessageFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@ConditionalOnProperty(prefix = "disender", name = "webhook-url")
@ConditionalOnExpression("${disender.enabled:true}")
@EnableConfigurationProperties(DisenderConfigurationProperties.class)
public class DisenderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "disenderProperties")
    DisenderProperties disenderProperties(DisenderConfigurationProperties config,
                                          @Value("${spring.application.name:application}") String appName) {
        return config.toCoreProperties(appName);
    }

    @Bean
    @ConditionalOnMissingBean(name = "disenderRestClient")
    RestClient disenderRestClient(DisenderProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutMs = (int) properties.getTimeout().toMillis();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return RestClient.builder().requestFactory(factory).build();
    }

    @Bean
    @ConditionalOnMissingBean
    DisenderClient disenderClient(RestClient disenderRestClient, DisenderProperties properties) {
        return new RestClientDisenderClient(disenderRestClient, properties.getWebhookUrl());
    }

    @Bean
    @ConditionalOnMissingBean
    MessageFactory disenderMessageFactory(DisenderProperties properties) {
        return new MessageFactory(properties.getApplicationName(), properties.getUsername());
    }

    @Bean
    @ConditionalOnMissingBean
    DisenderEventListener disenderEventListener(DisenderClient disenderClient,
                                                MessageFactory disenderMessageFactory,
                                                DisenderProperties properties) {
        return new DisenderEventListener(disenderClient, disenderMessageFactory, properties);
    }
}
