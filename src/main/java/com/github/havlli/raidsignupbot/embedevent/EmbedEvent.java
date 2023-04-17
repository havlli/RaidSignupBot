package com.github.havlli.raidsignupbot.embedevent;

import com.github.havlli.raidsignupbot.signupuser.SignupUser;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmbedEvent {
    private String name;
    private String description;
    private LocalDate date;
    private LocalTime time;
    private String instances;
    private String memberSize;
    private boolean reservingEnabled;
    private String destinationChannelId;
    private String embedId;
    private String author;
    private boolean active;
    private List<SignupUser> signupUsers;

    private EmbedEvent(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.date = builder.date;
        this.time = builder.time;
        this.instances = builder.instances;
        this.memberSize = builder.memberSize;
        this.reservingEnabled = builder.reservingEnabled;
        this.destinationChannelId = builder.destinationChannelId;
        this.embedId = builder.embedId;
        this.author = builder.author;
        active = true;
        signupUsers = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getDateTimeString(String delimiter) {
        return date.toString() + delimiter + time.toString();
    }

    public String getInstances() {
        return instances;
    }

    public String getMemberSize() {
        return memberSize;
    }

    public boolean isReservingEnabled() {
        return reservingEnabled;
    }

    public String getDestinationChannelId() {
        return destinationChannelId;
    }

    public String getEmbedId() {
        return embedId;
    }

    public String getAuthor() {
        return author;
    }

    public List<SignupUser> getSignupUsers() {
        return signupUsers;
    }

    public void setSignupUsers(List<SignupUser> signupUserList) {
        signupUsers = signupUserList;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
        private LocalDate date;
        private LocalTime time;
        private String instances;
        private String memberSize;
        private boolean reservingEnabled;
        private String destinationChannelId;
        private String embedId;
        private String author;
        private EmbedEvent embedEvent;

        public Builder addName(String name) {
            this.name = name;
            return this;
        }

        public Builder addName(Message message) {
            this.name = message.getContent();
            return this;
        }

        public Builder addDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder addDescription(Message message) {
            this.description = message.getContent();
            return this;
        }

        public Builder addDate(String date) {
            this.date = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return this;
        }

        public Builder addDate(Message message) {
            this.date = LocalDate.parse(message.getContent(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return this;
        }

        public Builder addTime(String time) {
            this.time = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
            return this;
        }

        public Builder addTime(Message message) {
            this.time = LocalTime.parse(message.getContent(), DateTimeFormatter.ofPattern("HH:mm"));
            return this;
        }

        public Builder addInstances(String instances) {
            this.instances = instances;
            return this;
        }

        public Builder addInstances(List<String> instances) {
            this.instances = String.join(", ", instances);
            return this;
        }

        public Builder addMemberSize(String memberSize) {
            this.memberSize = memberSize;
            return this;
        }

        public Builder addMemberSize(List<String> memberSize, String defaultSize) {
            this.memberSize = memberSize.stream()
                    .findFirst()
                    .orElse(defaultSize);
            return this;
        }

        public Builder addReservingEnabled(boolean reservingEnabled) {
            this.reservingEnabled = reservingEnabled;
            return this;
        }

        public Builder addReservingEnabled(int reservingEnabled) {
            this.reservingEnabled = reservingEnabled == 1;
            return this;
        }

        public Builder addDestinationChannel(List<String> destinationChannel, String defaultChannel) {
            this.destinationChannelId = destinationChannel.stream()
                    .findFirst()
                    .orElse(defaultChannel);
            return this;
        }

        public Builder addDestinationChannel(String destinationChannel) {
            this.destinationChannelId = destinationChannel;
            return this;
        }

        public Builder addEmbedId(Snowflake embedId) {
            this.embedId = embedId.asString();
            return this;
        }

        public Builder addEmbedId(String embedId) {
            this.embedId = embedId;
            return this;
        }

        public Builder addAuthor(String author) {
            this.author = author;
            return this;
        }

        public Builder addAuthor(User user) {
            this.author = String.format("%s#%s", user.getUsername(), user.getDiscriminator());
            return this;
        }

        public EmbedEvent build() {
            this.embedEvent = new EmbedEvent(this);
            return embedEvent;
        }

        public String getDestinationChannelId() {
            return this.destinationChannelId;
        }

        public EmbedEvent getEmbedEvent() {
            return this.embedEvent;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public LocalDate getDate() {
            return date;
        }

        public LocalTime getTime() {
            return time;
        }

        public String getInstances() {
            return instances;
        }

        public String getMemberSize() {
            return memberSize;
        }

        public boolean isReservingEnabled() {
            return reservingEnabled;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EmbedEvent\n");

        for (Field field : getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(this);
                if (value != null) {
                    sb.append(field.getName())
                            .append(": ")
                            .append(value)
                            .append("\n");
                }
            } catch (IllegalAccessException e) {

            }
        }

        return sb.toString();
    }
}
