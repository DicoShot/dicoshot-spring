package io.dicoshot.core.message;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DiscordEmbed {

    public static final class Field {
        private final String name;
        private final String value;
        private final boolean inline;

        public Field(String name, String value, boolean inline) {
            this.name = Objects.requireNonNull(name, "name");
            this.value = Objects.requireNonNull(value, "value");
            this.inline = inline;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public boolean isInline() {
            return inline;
        }
    }

    private final String title;
    private final String description;
    private final Integer color;
    private final OffsetDateTime timestamp;
    private final List<Field> fields;

    private DiscordEmbed(Builder b) {
        this.title = b.title;
        this.description = b.description;
        this.color = b.color;
        this.timestamp = b.timestamp;
        this.fields = Collections.unmodifiableList(new ArrayList<>(b.fields));
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getColor() {
        return color;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public List<Field> getFields() {
        return fields;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String title;
        private String description;
        private Integer color;
        private OffsetDateTime timestamp;
        private final List<Field> fields = new ArrayList<>();

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder timestamp(OffsetDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder addField(String name, String value, boolean inline) {
            this.fields.add(new Field(name, value, inline));
            return this;
        }

        public DiscordEmbed build() {
            return new DiscordEmbed(this);
        }
    }
}
