package io.dicoshot.boot3;

import io.dicoshot.core.DicoshotClient;
import io.dicoshot.core.DicoshotProperties;
import io.dicoshot.core.message.MessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

public class DicoshotEventListener implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(DicoshotEventListener.class);

    private final DicoshotClient client;
    private final MessageFactory messageFactory;
    private final DicoshotProperties properties;
    private ApplicationContext applicationContext;

    public DicoshotEventListener(DicoshotClient client, MessageFactory messageFactory, DicoshotProperties properties) {
        this.client = client;
        this.messageFactory = messageFactory;
        this.properties = properties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @EventListener
    public void onStartup(ApplicationReadyEvent event) {
        if (!properties.isNotifyOnStartup()) {
            return;
        }
        safeSend(messageFactory.startup(), "startup");
    }

    @EventListener
    public void onShutdown(ContextClosedEvent event) {
        if (!properties.isNotifyOnShutdown()) {
            return;
        }
        // ContextClosedEvent bubbles up from child contexts — only react to our own context closing
        if (event.getApplicationContext() != applicationContext) {
            return;
        }
        safeSend(messageFactory.shutdown(), "shutdown");
    }

    private void safeSend(io.dicoshot.core.message.DiscordMessage message, String phase) {
        try {
            client.send(message);
        } catch (RuntimeException e) {
            log.warn("Dicoshot {} notification failed: {}", phase, e.getMessage());
        }
    }
}
