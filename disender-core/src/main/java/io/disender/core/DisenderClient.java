package io.disender.core;

import io.disender.core.message.DiscordMessage;

public interface DisenderClient {

    void send(DiscordMessage message);
}
