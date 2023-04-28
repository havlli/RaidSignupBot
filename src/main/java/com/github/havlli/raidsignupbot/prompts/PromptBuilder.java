package com.github.havlli.raidsignupbot.prompts;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.MessageCreateSpec;

public abstract class PromptBuilder<T extends PromptBuilder<T, P>, P extends Prompt> {
    protected final ChatInputInteractionEvent event;
    protected MessageCreateSpec promptMessage;
    protected MessageGarbageCollector garbageCollector;

    PromptBuilder(ChatInputInteractionEvent event) {
        this.event = event;
    }

    public T withPromptMessage(MessageCreateSpec promptMessage) {
        this.promptMessage = promptMessage;
        return self();
    }

    public T withPromptMessage(String promptMessage) {
        this.promptMessage = MessageCreateSpec.builder()
                .content(promptMessage)
                .build();
        return self();
    }

    public T withGarbageCollector(MessageGarbageCollector garbageCollector) {
        this.garbageCollector = garbageCollector;
        return self();
    }

    protected abstract T self();
    protected abstract P doBuild();

    public P build() {
        return doBuild();
    }
}
