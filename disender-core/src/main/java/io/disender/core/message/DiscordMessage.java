package io.disender.core.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DiscordMessage {

    private final String content;
    private final String username;
    private final List<DiscordEmbed> embeds;

    private DiscordMessage(Builder b) {
        this.content = b.content;
        this.username = b.username;
        this.embeds = Collections.unmodifiableList(new ArrayList<>(b.embeds));
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }

    public List<DiscordEmbed> getEmbeds() {
        return embeds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String content;
        private String username;
        private final List<DiscordEmbed> embeds = new ArrayList<>();

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder addEmbed(DiscordEmbed embed) {
            this.embeds.add(embed);
            return this;
        }

        public DiscordMessage build() {
            return new DiscordMessage(this);
        }
    }
}
