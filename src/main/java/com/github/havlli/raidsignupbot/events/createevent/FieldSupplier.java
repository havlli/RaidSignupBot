package com.github.havlli.raidsignupbot.events.createevent;

import discord4j.core.spec.EmbedCreateFields;

import java.util.List;

public interface FieldSupplier {
    List<EmbedCreateFields.Field> getPopulatedFields();
}
