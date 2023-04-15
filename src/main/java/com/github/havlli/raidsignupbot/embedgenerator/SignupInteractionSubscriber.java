package com.github.havlli.raidsignupbot.embedgenerator;

import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.signupuser.SignupUser;
import com.github.havlli.raidsignupbot.signupuser.SignupUserService;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.InteractionReplyEditSpec;
import reactor.core.publisher.Mono;

import java.util.List;

public class SignupInteractionSubscriber implements InteractionSubscriber {

    private final EmbedGenerator embedGenerator;
    private final SignupUserService signupUserService;

    public SignupInteractionSubscriber(
            EmbedGenerator embedGenerator,
            SignupUserService signupUserService
    ) {
        this.embedGenerator = embedGenerator;
        this.signupUserService = signupUserService;
    }

    @Override
    public Mono<Message> handleEvent(ButtonInteractionEvent event, EmbedEvent embedEvent) {
        List<SignupUser> signupUsers = embedEvent.getSignupUsers();
        User user = event.getInteraction().getUser();
        String userId = user.getId().asString();
        String embedEventId = embedEvent.getEmbedId();
        int fieldKey = getFieldKeyFromCustomId(event.getCustomId());

        SignupUser signupUser = signupUserService.getSignupUser(userId, signupUsers);
        if (signupUser == null) {
            int signupOrder = signupUsers.size() + 1;
            signupUser = new SignupUser(signupOrder, userId, user.getUsername(), fieldKey);
            signupUserService.addSignupUser(signupUser, signupUsers, embedEventId);
        } else {
            signupUserService.updateSignupUser(signupUser, embedEventId, fieldKey);
        }

        return event.deferEdit()
                .then(event.editReply(InteractionReplyEditSpec.builder()
                        .addEmbed(embedGenerator.generateEmbed(embedEvent))
                        .build())
                );
    }

    private int getFieldKeyFromCustomId(String customId) {
        return Integer.parseInt(customId.split(",")[1]);
    }
}
