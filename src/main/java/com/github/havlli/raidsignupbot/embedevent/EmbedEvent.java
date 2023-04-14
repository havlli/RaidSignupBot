package com.github.havlli.raidsignupbot.embedevent;

import com.github.havlli.raidsignupbot.signupuser.SignupUser;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;

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

    public EmbedEvent() {
        active = true;
        reservingEnabled = false;
        signupUsers = new ArrayList<>();
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    private void setAuthor(String author) {
        this.author = author;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    private void setDate(LocalDate date) {
        this.date = date;
    }

    private void setTime(LocalTime time) {
        this.time = time;
    }

    private void setInstances(String instances) {
        this.instances = instances;
    }

    private void setMemberSize(String memberSize) {
        this.memberSize = memberSize;
    }

    private void setReservingEnabled(boolean reservingEnabled) {
        this.reservingEnabled = reservingEnabled;
    }

    private void setDestinationChannelId(String destinationChannelId) {
        this.destinationChannelId = destinationChannelId;
    }

    private void setEmbedId(String embedId) {
        this.embedId = embedId;
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

    public static EmbedEventBuilder builder() {
        return new EmbedEventBuilder();
    }

    public static class EmbedEventBuilder {
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

        public EmbedEventBuilder addName(String name) {
            this.name = name;
            return this;
        }

        public EmbedEventBuilder addName(Message message) {
            this.name = message.getContent();
            return this;
        }

        public EmbedEventBuilder addDescription(String description) {
            this.description = description;
            return this;
        }

        public EmbedEventBuilder addDescription(Message message) {
            this.description = message.getContent();
            return this;
        }

        public EmbedEventBuilder addDate(String date) {
            this.date = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return this;
        }

        public EmbedEventBuilder addDate(Message message) {
            this.date = LocalDate.parse(message.getContent(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return this;
        }

        public EmbedEventBuilder addTime(String time) {
            this.time = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
            return this;
        }

        public EmbedEventBuilder addTime(Message message) {
            this.time = LocalTime.parse(message.getContent(), DateTimeFormatter.ofPattern("HH:mm"));
            return this;
        }

        public EmbedEventBuilder addInstances(String instances) {
            this.instances = instances;
            return this;
        }

        public EmbedEventBuilder addInstances(List<String> instances) {
            this.instances = String.join(", ", instances);
            return this;
        }

        public EmbedEventBuilder addMemberSize(String memberSize) {
            this.memberSize = memberSize;
            return this;
        }

        public EmbedEventBuilder addMemberSize(List<String> memberSize, String defaultSize) {
            this.memberSize = memberSize.stream()
                    .findFirst()
                    .orElse(defaultSize);
            return this;
        }

        public EmbedEventBuilder addReservingEnabled(boolean reservingEnabled) {
            this.reservingEnabled = reservingEnabled;
            return this;
        }

        public EmbedEventBuilder addReservingEnabled(int reservingEnabled) {
            this.reservingEnabled = reservingEnabled == 1;
            return this;
        }

        public EmbedEventBuilder addDestinationChannel(List<String> destinationChannel, String defaultChannel) {
            this.destinationChannelId = destinationChannel.stream()
                    .findFirst()
                    .orElse(defaultChannel);
            return this;
        }

        public EmbedEventBuilder addDestinationChannel(String destinationChannel) {
            this.destinationChannelId = destinationChannel;
            return this;
        }

        public EmbedEventBuilder addEmbedId(Snowflake embedId) {
            this.embedId = embedId.asString();
            return this;
        }

        public EmbedEventBuilder addEmbedId(String embedId) {
            this.embedId = embedId;
            return this;
        }

        public EmbedEventBuilder addAuthor(String author) {
            this.author = author;
            return this;
        }

        public EmbedEventBuilder addAuthor(User user) {
            this.author = String.format("%s#%s", user.getUsername(), user.getDiscriminator());
            return this;
        }

        public String getDestinationChannelId() {
            return this.destinationChannelId;
        }

        public EmbedEvent build() {
            this.embedEvent = new EmbedEvent();
            embedEvent.setName(name);
            embedEvent.setDescription(description);
            embedEvent.setDate(date);
            embedEvent.setTime(time);
            embedEvent.setInstances(instances);
            embedEvent.setMemberSize(memberSize);
            embedEvent.setReservingEnabled(reservingEnabled);
            embedEvent.setDestinationChannelId(destinationChannelId);
            embedEvent.setEmbedId(embedId);
            embedEvent.setAuthor(author);
            return this.embedEvent;
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
}
