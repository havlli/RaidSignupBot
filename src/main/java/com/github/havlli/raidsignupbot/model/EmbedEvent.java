package com.github.havlli.raidsignupbot.model;

import discord4j.core.object.entity.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class EmbedEvent {
    private String name;
    private String description;
    private LocalDate date;
    private LocalTime time;
    private List<String> instances;
    private String memberSize;
    private boolean reservingEnabled;
    private Long destinationChannelId;
    private Long embedId;
    private final User author;

    public EmbedEvent(User author) {
        this.author = author;
        reservingEnabled = false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setInstances(List<String> instances) {
        this.instances = instances;
    }

    public void setMemberSize(String memberSize) {
        this.memberSize = memberSize;
    }

    public void setReservingEnabled(boolean reservingEnabled) {
        this.reservingEnabled = reservingEnabled;
    }

    public void setDestinationChannelId(Long destinationChannelId) {
        this.destinationChannelId = destinationChannelId;
    }

    public void setEmbedId(Long embedId) {
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

    public List<String> getInstances() {
        return instances;
    }

    public String getMemberSize() {
        return memberSize;
    }

    public boolean isReservingEnabled() {
        return reservingEnabled;
    }

    public Long getDestinationChannelId() {
        return destinationChannelId;
    }

    public Long getEmbedId() {
        return embedId;
    }

    public User getAuthor() {
        return author;
    }
}
