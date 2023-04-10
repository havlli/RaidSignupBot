package com.github.havlli.raidsignupbot.embedevent;

import com.github.havlli.raidsignupbot.signupuser.SignupUser;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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
    private String author;
    private boolean active;
    private List<SignupUser> signupUsers;

    public EmbedEvent() {
        reservingEnabled = false;
        signupUsers = new ArrayList<>();
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    public String getAuthor() {
        return author;
    }

    public List<SignupUser> getSignupUsers() {
        if (signupUsers == null) return new ArrayList<>();
        else return signupUsers;
    }

    public void setSignupUsers(List<SignupUser> signupUserList) {
        this.signupUsers = signupUserList;
    }

    public void addSignupUser(SignupUser signupUser) {
        signupUsers.add(signupUser);
    }

    public void removeSignupUser(SignupUser signupUser) {
        signupUsers.remove(signupUser);
    }
}
