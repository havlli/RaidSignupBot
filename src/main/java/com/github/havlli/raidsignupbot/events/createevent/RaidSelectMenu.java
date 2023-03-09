package com.github.havlli.raidsignupbot.events.createevent;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;

import java.util.ArrayList;
import java.util.List;

public class RaidSelectMenu {
    public static ActionRow getRaidSelectMenu() {
        List<SelectMenu.Option> eventOptions = new ArrayList<>();
        eventOptions.add(SelectMenu.Option.of("Molten Core - Normal", "Molten Core - Normal"));
        eventOptions.add(SelectMenu.Option.of("Molten Core - Heroic", "Molten Core - Heroic"));
        eventOptions.add(SelectMenu.Option.of("Molten Core - Mythic", "Molten Core - Mythic"));
        eventOptions.add(SelectMenu.Option.of("Molten Core - Ascended", "Molten Core - Ascended"));
        eventOptions.add(SelectMenu.Option.of("Onyxia - Normal", "Onyxia - Normal"));
        SelectMenu selectMenu = SelectMenu.of("raid-select", eventOptions)
                .withMaxValues(3).withMinValues(1);
        return ActionRow.of(selectMenu);
    }

    public static ActionRow getRaidSizeSelect() {
        List<SelectMenu.Option> selectOptions = new ArrayList<>();
        selectOptions.add(SelectMenu.Option.of("10", "10"));
        selectOptions.add(SelectMenu.Option.of("15", "15"));
        selectOptions.add(SelectMenu.Option.of("20", "20"));
        selectOptions.add(SelectMenu.Option.of("25", "25"));
        SelectMenu selectMenu = SelectMenu.of("raid-size", selectOptions)
                .withMaxValues(1).withMinValues(1);
        return ActionRow.of(selectMenu);
    }
}
