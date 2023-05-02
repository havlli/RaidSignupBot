package com.github.havlli.raidsignupbot.events.editevent;

import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public abstract class EditHandler {

    protected EditHandler successor;
    public EditHandler(EditHandler successor) {
        this.successor = successor;
    }

    public abstract Mono<Message> handleEditEvent(EditField editField);
}
