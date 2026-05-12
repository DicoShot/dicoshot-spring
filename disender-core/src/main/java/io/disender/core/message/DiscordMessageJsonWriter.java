package io.disender.core.message;

import java.time.format.DateTimeFormatter;

public final class DiscordMessageJsonWriter {

    private DiscordMessageJsonWriter() {
    }

    public static String toJson(DiscordMessage message) {
        StringBuilder sb = new StringBuilder(256);
        sb.append('{');
        boolean first = true;
        if (message.getContent() != null) {
            first = appendString(sb, first, "content", message.getContent());
        }
        if (message.getUsername() != null) {
            first = appendString(sb, first, "username", message.getUsername());
        }
        if (!message.getEmbeds().isEmpty()) {
            if (!first) {
                sb.append(',');
            }
            sb.append("\"embeds\":[");
            for (int i = 0; i < message.getEmbeds().size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                appendEmbed(sb, message.getEmbeds().get(i));
            }
            sb.append(']');
        }
        sb.append('}');
        return sb.toString();
    }

    private static void appendEmbed(StringBuilder sb, DiscordEmbed embed) {
        sb.append('{');
        boolean first = true;
        if (embed.getTitle() != null) {
            first = appendString(sb, first, "title", embed.getTitle());
        }
        if (embed.getDescription() != null) {
            first = appendString(sb, first, "description", embed.getDescription());
        }
        if (embed.getColor() != null) {
            if (!first) {
                sb.append(',');
            }
            sb.append("\"color\":").append(embed.getColor().intValue());
            first = false;
        }
        if (embed.getTimestamp() != null) {
            String iso = embed.getTimestamp().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            first = appendString(sb, first, "timestamp", iso);
        }
        if (!embed.getFields().isEmpty()) {
            if (!first) {
                sb.append(',');
            }
            sb.append("\"fields\":[");
            for (int i = 0; i < embed.getFields().size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                DiscordEmbed.Field f = embed.getFields().get(i);
                sb.append('{');
                appendString(sb, true, "name", f.getName());
                sb.append(',');
                appendString(sb, true, "value", f.getValue());
                sb.append(",\"inline\":").append(f.isInline());
                sb.append('}');
            }
            sb.append(']');
        }
        sb.append('}');
    }

    private static boolean appendString(StringBuilder sb, boolean first, String key, String value) {
        if (!first) {
            sb.append(',');
        }
        sb.append('"').append(key).append("\":");
        appendQuoted(sb, value);
        return false;
    }

    private static void appendQuoted(StringBuilder sb, String value) {
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }
}
