package com.github.havlli.raidsignupbot.component;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.channel.TextChannel;

import java.util.ArrayList;
import java.util.List;

public class ChanelSelectMenu implements SelectMenuComponent {

    private static final String CUSTOM_ID = "destination-channel";
    private final List<TextChannel> textChannels;

    public ChanelSelectMenu(List<TextChannel> textChannels) {
        this.textChannels = textChannels;
    }

    @Override
    public ActionRow getActionRow() {
        SelectMenuGenerator selectMenuGenerator = new SelectMenuGenerator(
                CUSTOM_ID,
                buildOptions(),
                "Choose channel to post signup in!",
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
        for (TextChannel textChannel : textChannels) {
            selectOptions.add(SelectMenu.Option.of(textChannel.getName(), textChannel.getId().asString()));
        }

        return selectOptions;
    }
}
