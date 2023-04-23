package com.github.havlli.raidsignupbot.promptkit;

import com.github.havlli.raidsignupbot.logger.Logger;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class MessageGarbageCollector {

    private final List<Long> messageList;
    private final Logger logger;

    public MessageGarbageCollector(Logger logger) {
        this.messageList = new ArrayList<>();
        this.logger = logger;
    }

    public void collectMessage(Message message) {
        Long messageId = message.getId().asLong();
        messageList.add(messageId);
    }

    public Mono<Void> cleanup(Mono<MessageChannel> messageChannelMono) {
        Mono<Void> chainedMono = Mono.empty();
        for (Long id : messageList) {
            chainedMono = chainedMono.then(messageChannelMono
                    .flatMap(channel -> channel.getMessageById(Snowflake.of(id)))
                    .flatMap(this::deleteMessage));
        }

        return chainedMono;
    }

    private Mono<Void> deleteMessage(Message message) {
        return message.delete().onErrorResume(error -> {
            logger.log("Failed to delete message " + message.getId().asString() + " : " + error.getMessage());
            return Mono.empty();
        });
    }
}
