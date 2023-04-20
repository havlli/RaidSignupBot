package com.github.havlli.raidsignupbot.component;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;

import java.util.ArrayList;
import java.util.List;

public class ExpiredSelectMenu implements ActionRowComponent {

    @Override
    public ActionRow getActionRow() {
        SelectMenuGenerator selectMenuGenerator = new SelectMenuGenerator(
                "expired-menu",
                buildOptions(),
                "This event is closed.",
                0,
                0,
                true
        );

        return selectMenuGenerator.generate();
    }

    private List<SelectMenu.Option> buildOptions() {
        List<SelectMenu.Option> selectOptions = new ArrayList<>();
        selectOptions.add(SelectMenu.Option.of("This option wont be visible", "because SelectMenu is disabled"));

        return selectOptions;
    }
}
