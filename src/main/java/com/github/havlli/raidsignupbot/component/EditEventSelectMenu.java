package com.github.havlli.raidsignupbot.component;

import com.github.havlli.raidsignupbot.events.editevent.EditField;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;

import java.util.ArrayList;
import java.util.List;

public class EditEventSelectMenu implements SelectMenuComponent {

    private static final String CUSTOM_ID = "edit_event_select";


    @Override
    public ActionRow getActionRow() {
        SelectMenuGenerator selectMenuGenerator = new SelectMenuGenerator(
                CUSTOM_ID,
                buildOptions(),
                "Choose option you would like to change!",
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
        List<SelectMenu.Option> selectMenuOptions = new ArrayList<>();
        selectMenuOptions.add(SelectMenu.Option.of("Name", EditField.NAME.getStringValue()));
        selectMenuOptions.add(SelectMenu.Option.of("Description", EditField.DESCRIPTION.getStringValue()));
        selectMenuOptions.add(SelectMenu.Option.of("Date", EditField.DATE.getStringValue()));
        selectMenuOptions.add(SelectMenu.Option.of("Time", EditField.TIME.getStringValue()));
        selectMenuOptions.add(SelectMenu.Option.of("Instances", EditField.INSTANCES.getStringValue()));
        selectMenuOptions.add(SelectMenu.Option.of("Raid size", EditField.MEMBER_SIZE.getStringValue()));
        selectMenuOptions.add(SelectMenu.Option.of("Reserve Option", EditField.RESERVE.getStringValue()));

        return selectMenuOptions;
    }
}
