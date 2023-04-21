package com.github.havlli.raidsignupbot.promptkit;

import discord4j.core.object.entity.Message;
import java.util.ArrayList;
import java.util.List;

public class MessageGarbageCollector {

    private final List<Long> messageList;

    public MessageGarbageCollector() {
        this.messageList = new ArrayList<>();
    }

    public void collectMessage(Message message) {
        Long messageId = message.getId().asLong();
        messageList.add(messageId);
    }


}
