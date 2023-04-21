package com.github.havlli.raidsignupbot.component;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;

import java.util.ArrayList;
import java.util.List;

public class MemberSizeSelectMenu implements SelectMenuComponent {

    private static final String CUSTOM_ID = "raid-size";
    @Override
    public ActionRow getActionRow() {
        SelectMenuGenerator selectMenuGenerator = new SelectMenuGenerator(
                CUSTOM_ID,
                buildOptions(),
                "Choose maximum raid size!",
                1,
                1,
                false
        );

        return selectMenuGenerator.generate();
    }

    @Override
    public String getCustomId() {
        return CUSTOM_ID;
    }

    private List<SelectMenu.Option> buildOptions() {
        List<SelectMenu.Option> selectOptions = new ArrayList<>();
        selectOptions.add(SelectMenu.Option.of("10", "10"));
        selectOptions.add(SelectMenu.Option.of("15", "15"));
        selectOptions.add(SelectMenu.Option.of("20", "20"));
        selectOptions.add(SelectMenu.Option.of("25", "25"));

        return selectOptions;
    }
}
