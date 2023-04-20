package com.github.havlli.raidsignupbot.component;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.channel.TextChannel;

import java.util.ArrayList;
import java.util.List;

public class ChanelSelectMenu implements ActionRowComponent {

    private final List<TextChannel> textChannels;

    public ChanelSelectMenu(List<TextChannel> textChannels) {
        this.textChannels = textChannels;
    }

    @Override
    public ActionRow getActionRow() {
        SelectMenuGenerator selectMenuGenerator = new SelectMenuGenerator(
                "destination-channel",
                buildOptions(),
                "Choose channel to post signup in!",
                1,
                1,
                false
        );

        return selectMenuGenerator.generate();
    }

    private List<SelectMenu.Option> buildOptions() {
        List<SelectMenu.Option> selectOptions = new ArrayList<>();
        for (TextChannel textChannel : textChannels) {
            selectOptions.add(SelectMenu.Option.of(textChannel.getName(), textChannel.getId().asString()));
        }

        return selectOptions;
    }
}
