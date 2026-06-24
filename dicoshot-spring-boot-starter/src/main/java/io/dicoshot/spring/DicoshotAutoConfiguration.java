package io.dicoshot.spring;

import io.dicoshot.core.DicoshotClient;
import io.dicoshot.core.DicoshotProperties;
import io.dicoshot.core.message.MessageFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@ConditionalOnProperty(prefix = "dicoshot", name = "webhook-url")
@ConditionalOnExpression("${dicoshot.enabled:true}")
@EnableConfigurationProperties(DicoshotConfigurationProperties.class)
@Import(DicoshotAutoConfiguration.AopConfiguration.class)
public class DicoshotAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "dicoshotProperties")
    DicoshotProperties dicoshotProperties(DicoshotConfigurationProperties config,
                                          @Value("${spring.application.name:application}") String appName) {
        return config.toCoreProperties(appName);
    }

    @Bean
    @ConditionalOnMissingBean(name = "dicoshotRestClient")
    RestClient dicoshotRestClient(DicoshotProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutMs = (int) properties.getTimeout().toMillis();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return RestClient.builder().requestFactory(factory).build();
    }

    @Bean
    @ConditionalOnMissingBean
    DicoshotClient dicoshotClient(RestClient dicoshotRestClient, DicoshotProperties properties) {
        return new RestClientDicoshotClient(dicoshotRestClient, properties.getWebhookUrl());
    }

    @Bean
    @ConditionalOnMissingBean
    MessageFactory dicoshotMessageFactory(DicoshotProperties properties, Environment environment) {
        String[] profiles = environment.getActiveProfiles();
        String profile = profiles.length > 0 ? String.join(", ", profiles) : null;
        return new MessageFactory(properties.getApplicationName(), properties.getUsername(), profile);
    }

    @Bean
    @ConditionalOnMissingBean
    DicoshotEventListener dicoshotEventListener(DicoshotClient dicoshotClient,
                                                MessageFactory dicoshotMessageFactory,
                                                DicoshotProperties properties) {
        return new DicoshotEventListener(dicoshotClient, dicoshotMessageFactory, properties);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    static class AopConfiguration {

        @Bean
        @ConditionalOnMissingBean
        DicoshotNotifyAspect dicoshotNotifyAspect(DicoshotClient dicoshotClient) {
            return new DicoshotNotifyAspect(dicoshotClient);
        }
    }
}
