package io.disender.core.message;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordMessageJsonWriterTest {

    @Test
    void writesMinimalMessageWithOnlyContent() {
        DiscordMessage msg = DiscordMessage.builder().content("hello").build();

        String json = DiscordMessageJsonWriter.toJson(msg);

        assertThat(json).isEqualTo("{\"content\":\"hello\"}");
    }

    @Test
    void escapesSpecialCharactersInContent() {
        DiscordMessage msg = DiscordMessage.builder()
                .content("line1\nquote:\"\\backslash")
                .build();

        String json = DiscordMessageJsonWriter.toJson(msg);

        assertThat(json).isEqualTo("{\"content\":\"line1\\nquote:\\\"\\\\backslash\"}");
    }

    @Test
    void writesEmbedWithFieldsAndTimestamp() {
        OffsetDateTime ts = OffsetDateTime.of(2026, 5, 12, 10, 30, 0, 0, ZoneOffset.UTC);
        DiscordEmbed embed = DiscordEmbed.builder()
                .title("Started")
                .color(0x2ECC71)
                .timestamp(ts)
                .addField("Service", "my-app", true)
                .build();
        DiscordMessage msg = DiscordMessage.builder()
                .username("Disender")
                .addEmbed(embed)
                .build();

        String json = DiscordMessageJsonWriter.toJson(msg);

        assertThat(json).contains("\"username\":\"Disender\"");
        assertThat(json).contains("\"title\":\"Started\"");
        assertThat(json).contains("\"color\":3066993");
        assertThat(json).contains("\"timestamp\":\"2026-05-12T10:30:00Z\"");
        assertThat(json).contains("\"name\":\"Service\"");
        assertThat(json).contains("\"value\":\"my-app\"");
        assertThat(json).contains("\"inline\":true");
    }

    @Test
    void omitsNullFields() {
        DiscordMessage msg = DiscordMessage.builder()
                .addEmbed(DiscordEmbed.builder().title("only-title").build())
                .build();

        String json = DiscordMessageJsonWriter.toJson(msg);

        assertThat(json).doesNotContain("content");
        assertThat(json).doesNotContain("username");
        assertThat(json).doesNotContain("description");
        assertThat(json).doesNotContain("color");
        assertThat(json).doesNotContain("timestamp");
        assertThat(json).contains("\"title\":\"only-title\"");
    }
}
