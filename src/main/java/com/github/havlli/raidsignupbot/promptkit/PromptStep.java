package com.github.havlli.raidsignupbot.promptkit;


import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public interface PromptStep {
    Mono<Message> getMono();
}
