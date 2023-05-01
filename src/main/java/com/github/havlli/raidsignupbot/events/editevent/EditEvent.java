package com.github.havlli.raidsignupbot.events.editevent;

import com.github.havlli.raidsignupbot.client.Dependencies;
import com.github.havlli.raidsignupbot.embedgenerator.EmbedGenerator;
import com.github.havlli.raidsignupbot.events.EventHandler;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public class EditEvent implements EventHandler {

    private static final String COMMAND_NAME = "edit-event";
    private static final String OPTION_MESSAGE_ID = "message-id";

    @Override
    public Class<? extends Event> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<?> handleEvent(Event event) {
        ChatInputInteractionEvent interactionEvent = (ChatInputInteractionEvent) event;
        if (interactionEvent.getCommandName().equals(COMMAND_NAME)) {
            return interactionEvent.deferReply()
                    .withEphemeral(true)
                    .then(deferredMessage(interactionEvent));
        }
        return Mono.empty();
    }

    private Mono<Message> deferredMessage(ChatInputInteractionEvent event) {
        Snowflake botId = event.getClient().getSelfId();
        Snowflake targetMessage = event.getOption(OPTION_MESSAGE_ID)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(value -> Snowflake.of(value.asString()))
                .orElse(Snowflake.of(0));

        return event.getInteraction().getGuild()
                .flatMapMany(guild -> guild.getChannels()
                        .filter(channel -> channel.getType() == Channel.Type.GUILD_TEXT)
                        .cast(TextChannel.class)
                        .flatMap(channel -> channel.getMessageById(targetMessage).flux()
                                .onErrorResume(error -> Mono.empty())
                        )
                )
                .switchIfEmpty(eventNotFoundResponse(event))
                .next()
                .flatMap(message -> {
                    Optional<User> author = message.getAuthor();
                    boolean authorIsThisBot = author.isPresent() && author.get().getId().equals(botId);
                    boolean isExpired = hasExpiredFlag(message.getComponents());
                    if (authorIsThisBot && !isExpired) {
                        return handleEventEdit(event, message);
                    } else {
                        return eventNotFoundResponse(event);
                    }
                });
    }

    private Mono<Message> handleEventEdit(ChatInputInteractionEvent event, Message message) {
        EmbedGenerator embedGenerator = new EmbedGenerator(
                Dependencies.getInstance().getEmbedEventService(),
                Dependencies.getInstance().getSignupUserService()
        );
        EditEventPrompt editEventPrompt = new EditEventPrompt(event, message, embedGenerator);

        return event.createFollowup("Initiated process of editing event in your DMs, please continue there!")
                .withEphemeral(true)
                .then(editEventPrompt.initiateEditEvent());
    }

    private Mono<Message> eventNotFoundResponse(ChatInputInteractionEvent event) {
        return event.createFollowup("Event is already expired, not found or not posted by this bot!")
                .withEphemeral(true);
    }

    private boolean hasExpiredFlag(List<LayoutComponent> layoutComponents) {
        return layoutComponents.stream()
                .flatMap(components -> components.getChildren().stream())
                .filter(component -> component instanceof SelectMenu)
                .map(component -> (SelectMenu) component)
                .anyMatch(selectMenu -> selectMenu.getCustomId().equals("expired-menu"));
    }
}
