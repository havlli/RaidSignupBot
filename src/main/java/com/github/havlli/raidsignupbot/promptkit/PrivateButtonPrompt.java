package com.github.havlli.raidsignupbot.promptkit;

import com.github.havlli.raidsignupbot.component.ButtonRowComponent;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.InteractionReplyEditSpec;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

public class PrivateButtonPrompt implements PromptStep {

    private final ChatInputInteractionEvent event;
    private final String promptMessage;
    private Consumer<ButtonInteractionEvent> buttonHandler;
    private ButtonRowComponent buttonRowComponent;

    public PrivateButtonPrompt(ChatInputInteractionEvent event, String promptMessage) {
        this.event = event;
        this.promptMessage = promptMessage;
    }

    public PrivateButtonPrompt withButtonHandler(Consumer<ButtonInteractionEvent> buttonHandler) {
        this.buttonHandler = buttonHandler;
        return this;
    }

    public PrivateButtonPrompt withButtonRowComponent(ButtonRowComponent buttonRowComponent) {
        this.buttonRowComponent = buttonRowComponent;
        return this;
    }

    @Override
    public Mono<Message> getMono() {
        User user = event.getInteraction().getUser();
        EventDispatcher eventDispatcher = event.getClient().getEventDispatcher();
        Mono<PrivateChannel> privateChannelMono = user.getPrivateChannel();

        return privateChannelMono
                .flatMap(channel -> channel.createMessage(promptMessage)
                        .withComponents(buttonRowComponent.getActionRow()))
                .flatMap(message -> eventDispatcher.on(ButtonInteractionEvent.class)
                        .filter(event -> event.getInteraction().getUser().equals(user))
                        .filter(this::checkIfButtonIdMatches)
                        .next()
                        .flatMap(event -> {
                            buttonHandler.accept(event);
                            return event.deferEdit()
                                    .then(event.editReply(InteractionReplyEditSpec.builder()
                                            /*.addEmbed(previewHandler)*/
                                            /*.addComponent(memberSizeSelectMenu.getActionRow())*/
                                            .components(List.of())
                                            .contentOrNull("You have selected " + event.getCustomId())
                                            .build()));
                        }));
    }

    private boolean checkIfButtonIdMatches(ButtonInteractionEvent event) {
        return buttonRowComponent.getCustomIds()
                .stream()
                .anyMatch(customId -> customId.equals(event.getCustomId()));
    }
}
