package com.github.havlli.raidsignupbot.promptkit;

import com.github.havlli.raidsignupbot.component.SelectMenuComponent;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

public class PrivateSelectPrompt implements PromptStep {

    private final ChatInputInteractionEvent event;
    private final String promptMessage;
    private Consumer<List<String>> inputHandler;
    private SelectMenuComponent selectMenuComponent;
    private EmbedCreateSpec previewHandler;

    public PrivateSelectPrompt(ChatInputInteractionEvent event, String promptMessage) {
        this.event = event;
        this.promptMessage = promptMessage;
        this.inputHandler = null;
    }

    public PrivateSelectPrompt withInputHandler(Consumer<List<String>> inputHandler) {
        this.inputHandler = inputHandler;
        return this;
    }

    public PrivateSelectPrompt withSelectMenuComponent(SelectMenuComponent selectMenuComponent) {
        this.selectMenuComponent = selectMenuComponent;
        return this;
    }

    public PrivateSelectPrompt withPreviewHandler(EmbedCreateSpec previewHandler) {
        this.previewHandler = previewHandler;
        return this;
    }

    @Override
    public Mono<Message> getMono() {
        User user = event.getInteraction().getUser();
        EventDispatcher eventDispatcher = event.getClient().getEventDispatcher();
        Mono<PrivateChannel> privateChannelMono = user.getPrivateChannel();

        return privateChannelMono
                .flatMap(channel -> channel.createMessage(promptMessage)
                        .withComponents(selectMenuComponent.getActionRow()))
                .flatMap(message -> eventDispatcher.on(SelectMenuInteractionEvent.class)
                        .filter(event -> event.getCustomId().equals(selectMenuComponent.getCustomId()) && event.getInteraction().getUser().equals(user))
                        .next()
                        .flatMap(event -> {
                            inputHandler.accept(event.getValues());
                            return event.deferEdit()
                                    .then(event.editReply(InteractionReplyEditSpec.builder()
                                            /*.addEmbed(previewHandler)*/
                                            /*.addComponent(memberSizeSelectMenu.getActionRow())*/
                                            .components(List.of())
                                            .contentOrNull("You have selected " + String.join(", ", event.getValues()))
                                            .build()));
                        }));
    }
}
