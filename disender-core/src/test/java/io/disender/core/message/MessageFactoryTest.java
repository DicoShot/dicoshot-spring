package io.disender.core.message;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageFactoryTest {

    @Test
    void startupMessageContainsApplicationNameAndStartupTitle() {
        MessageFactory factory = new MessageFactory("my-service", null, "prod");

        DiscordMessage msg = factory.startup();

        assertThat(msg.getEmbeds()).hasSize(1);
        DiscordEmbed embed = msg.getEmbeds().get(0);
        assertThat(embed.getTitle()).isEqualTo("Application started");
        assertThat(embed.getColor()).isEqualTo(0x2ECC71);
        assertThat(embed.getFields())
                .extracting(DiscordEmbed.Field::getName)
                .containsExactly("Service", "Host", "PID", "Profile");
        assertThat(embed.getFields().get(0).getValue()).isEqualTo("my-service");
    }

    @Test
    void shutdownMessageUsesShutdownTitleAndColor() {
        MessageFactory factory = new MessageFactory("my-service", null, null);

        DiscordMessage msg = factory.shutdown();

        DiscordEmbed embed = msg.getEmbeds().get(0);
        assertThat(embed.getTitle()).isEqualTo("Application shutting down");
        assertThat(embed.getColor()).isEqualTo(0xE74C3C);
    }

    @Test
    void shutdownMessageContainsUptimeField() {
        MessageFactory factory = new MessageFactory("my-service", null, "prod");

        DiscordMessage msg = factory.shutdown();

        assertThat(msg.getEmbeds().get(0).getFields())
                .extracting(DiscordEmbed.Field::getName)
                .contains("Uptime");
    }

    @Test
    void usernameAppliedWhenProvided() {
        MessageFactory factory = new MessageFactory("my-service", "Disender Bot", null);

        DiscordMessage msg = factory.startup();

        assertThat(msg.getUsername()).isEqualTo("Disender Bot");
    }

    @Test
    void usernameIsNullWhenBlank() {
        MessageFactory factory = new MessageFactory("my-service", " ", null);

        DiscordMessage msg = factory.startup();

        assertThat(msg.getUsername()).isNull();
    }

    @Test
    void nullApplicationNameFallsBackToDefault() {
        MessageFactory factory = new MessageFactory(null, null, null);

        DiscordMessage msg = factory.startup();

        assertThat(msg.getEmbeds().get(0).getFields().get(0).getValue()).isEqualTo("application");
    }

    @Test
    void nullProfileFallsBackToDefault() {
        MessageFactory factory = new MessageFactory("my-service", null, null);

        DiscordMessage msg = factory.startup();

        assertThat(msg.getEmbeds().get(0).getFields())
                .filteredOn(f -> f.getName().equals("Profile"))
                .extracting(DiscordEmbed.Field::getValue)
                .containsExactly("default");
    }
}
