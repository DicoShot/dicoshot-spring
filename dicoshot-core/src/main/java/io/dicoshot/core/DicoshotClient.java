package io.dicoshot.core;

import io.dicoshot.core.message.DiscordMessage;

public interface DicoshotClient {

    void send(DiscordMessage message);
}
