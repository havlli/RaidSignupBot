package com.github.havlli.raidsignupbot.component;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;

import java.util.ArrayList;
import java.util.List;

public class RaidSelectMenu implements ActionRowComponent{

    @Override
    public ActionRow getActionRow() {
        SelectMenuGenerator selectMenuGenerator = new SelectMenuGenerator(
                "raid-select",
                buildOptions(),
                "Choose Raids for this event!",
                3,
                1,
                false
        );

        return selectMenuGenerator.generate();
    }

    private List<SelectMenu.Option> buildOptions() {
        List<SelectMenu.Option> selectOptions = new ArrayList<>();
        selectOptions.add(SelectMenu.Option.of("Molten Core - Normal", "Molten Core - Normal"));
        selectOptions.add(SelectMenu.Option.of("Molten Core - Heroic", "Molten Core - Heroic"));
        selectOptions.add(SelectMenu.Option.of("Molten Core - Mythic", "Molten Core - Mythic"));
        selectOptions.add(SelectMenu.Option.of("Molten Core - Ascended", "Molten Core - Ascended"));
        selectOptions.add(SelectMenu.Option.of("Onyxia - Normal", "Onyxia - Normal"));

        return selectOptions;
    }
}
