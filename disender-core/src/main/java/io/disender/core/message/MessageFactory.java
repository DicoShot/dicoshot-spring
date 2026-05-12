package io.disender.core.message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class MessageFactory {

    private static final int COLOR_STARTUP = 0x2ECC71;
    private static final int COLOR_SHUTDOWN = 0xE74C3C;

    private final String applicationName;
    private final String username;

    public MessageFactory(String applicationName, String username) {
        this.applicationName = applicationName != null ? applicationName : "application";
        this.username = username;
    }

    public DiscordMessage startup() {
        return build("Application started", COLOR_STARTUP);
    }

    public DiscordMessage shutdown() {
        return build("Application shutting down", COLOR_SHUTDOWN);
    }

    private DiscordMessage build(String title, int color) {
        DiscordEmbed embed = DiscordEmbed.builder()
                .title(title)
                .color(color)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .addField("Service", applicationName, true)
                .addField("Host", resolveHost(), true)
                .build();

        DiscordMessage.Builder b = DiscordMessage.builder().addEmbed(embed);
        if (username != null && !username.isBlank()) {
            b.username(username);
        }
        return b.build();
    }

    private static String resolveHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
