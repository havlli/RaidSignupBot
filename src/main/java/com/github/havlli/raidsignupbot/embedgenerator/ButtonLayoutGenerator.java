package com.github.havlli.raidsignupbot.embedgenerator;

import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ButtonLayoutGenerator {

    private ButtonLayoutGenerator() { }

    public static List<LayoutComponent> generateButtons(EmbedEvent embedEvent, String delimiter) {
        return Arrays.asList(
                ActionRow.of(generateRoleButtons(embedEvent, delimiter)),
                ActionRow.of(generateDefaultButtons(embedEvent, delimiter))
        );
    }

    private static List<Button> generateRoleButtons(EmbedEvent embedEvent, String delimiter) {
        return EmbedFields.getFieldsMap()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() > 0)
                .map(entry -> {
                    String customId = embedEvent.getEmbedId() + delimiter + entry.getKey();
                    return Button.primary(customId, entry.getValue());
                })
                .collect(Collectors.toList());
    }

    private static List<Button> generateDefaultButtons(EmbedEvent embedEvent, String delimiter) {
        return EmbedFields.getFieldsMap()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() <= 0)
                .map(entry -> {
                    String customId = embedEvent.getEmbedId() + delimiter + entry.getKey();
                    return Button.secondary(customId, entry.getValue());
                })
                .collect(Collectors.toList());
    }
}
