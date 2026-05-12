package io.disender.boot3;

import io.disender.core.DisenderClient;
import io.disender.core.DisenderProperties;
import io.disender.core.message.MessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

public class DisenderEventListener {

    private static final Logger log = LoggerFactory.getLogger(DisenderEventListener.class);

    private final DisenderClient client;
    private final MessageFactory messageFactory;
    private final DisenderProperties properties;

    public DisenderEventListener(DisenderClient client, MessageFactory messageFactory, DisenderProperties properties) {
        this.client = client;
        this.messageFactory = messageFactory;
        this.properties = properties;
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
        safeSend(messageFactory.shutdown(), "shutdown");
    }

    private void safeSend(io.disender.core.message.DiscordMessage message, String phase) {
        try {
            client.send(message);
        } catch (RuntimeException e) {
            log.warn("Disender {} notification failed: {}", phase, e.getMessage());
        }
    }
}
