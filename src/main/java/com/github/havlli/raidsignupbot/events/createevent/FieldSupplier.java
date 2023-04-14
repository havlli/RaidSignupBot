package com.github.havlli.raidsignupbot.events.createevent;

import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import discord4j.core.spec.EmbedCreateFields;

import java.util.List;

public interface FieldSupplier {
    List<EmbedCreateFields.Field> getPopulatedFields(EmbedEvent embedEvent);
}
