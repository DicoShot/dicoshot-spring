package io.disender.core.message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class MessageFactory {

    private static final int COLOR_STARTUP = 0x2ECC71;
    private static final int COLOR_SHUTDOWN = 0xE74C3C;

    private final String applicationName;
    private final String username;
    private final String profile;
    private final OffsetDateTime startedAt;

    public MessageFactory(String applicationName, String username, String profile) {
        this.applicationName = applicationName != null ? applicationName : "application";
        this.username = username;
        this.profile = (profile != null && !profile.isBlank()) ? profile : "default";
        this.startedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public DiscordMessage startup() {
        DiscordEmbed embed = DiscordEmbed.builder()
                .title("Application started")
                .color(COLOR_STARTUP)
                .timestamp(startedAt)
                .addField("Service", applicationName, true)
                .addField("Host", resolveHost(), true)
                .addField("PID", String.valueOf(ProcessHandle.current().pid()), true)
                .addField("Profile", profile, true)
                .build();

        return buildMessage(embed);
    }

    public DiscordMessage shutdown() {
        DiscordEmbed embed = DiscordEmbed.builder()
                .title("Application shutting down")
                .color(COLOR_SHUTDOWN)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .addField("Service", applicationName, true)
                .addField("Host", resolveHost(), true)
                .addField("PID", String.valueOf(ProcessHandle.current().pid()), true)
                .addField("Profile", profile, true)
                .addField("Uptime", formatUptime(), false)
                .build();

        return buildMessage(embed);
    }

    private DiscordMessage buildMessage(DiscordEmbed embed) {
        DiscordMessage.Builder b = DiscordMessage.builder().addEmbed(embed);
        if (username != null && !username.isBlank()) {
            b.username(username);
        }
        return b.build();
    }

    private String formatUptime() {
        Duration uptime = Duration.between(startedAt, OffsetDateTime.now(ZoneOffset.UTC));
        long hours = uptime.toHours();
        long minutes = uptime.toMinutesPart();
        long seconds = uptime.toSecondsPart();
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    private static String resolveHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
