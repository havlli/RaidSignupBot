package com.github.havlli.raidsignupbot.events;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public class BasePermissionChecker {

    private final ChatInputInteractionEvent interactionEvent;
    private final Permission permission;

    public BasePermissionChecker(ChatInputInteractionEvent interactionEvent, Permission permission) {
        this.interactionEvent = interactionEvent;
        this.permission = permission;
    }

    public Mono<Message> followupWithMessage(Mono<Message> messageMono) {
        Member member = interactionEvent.getInteraction().getMember().orElse(null);
        if (member == null)
            return interactionEvent.createFollowup("You are not valid Member to use this command")
                    .withEphemeral(true);

        return member.getBasePermissions()
                .flatMap(permissionSet -> {
                    if (permissionSet.contains(permission)) {
                        return messageMono;
                    }

                    return interactionEvent.createFollowup("You do not have permission to use this command.")
                            .withEphemeral(true);
                });
    }
}
