package com.github.havlli.raidsignupbot.events.createevent;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;

import java.util.ArrayList;
import java.util.List;

public class RaidSelectMenu {
    public static ActionRow getRaidSelectMenu() {
        List<SelectMenu.Option> eventOptions = new ArrayList<>();
        eventOptions.add(SelectMenu.Option.of("Molten Core - Normal", "mc-n"));
        eventOptions.add(SelectMenu.Option.of("Molten Core - Heroic", "mc-h"));
        eventOptions.add(SelectMenu.Option.of("Molten Core - Mythic", "mc-m"));
        eventOptions.add(SelectMenu.Option.of("Molten Core - Ascended", "mc-a"));
        eventOptions.add(SelectMenu.Option.of("Onyxia - Normal", "ony-n"));
        SelectMenu selectMenu = SelectMenu.of("raid-select", eventOptions
        ).withMaxValues(3).withMinValues(1);
        return ActionRow.of(selectMenu);
    }
}
