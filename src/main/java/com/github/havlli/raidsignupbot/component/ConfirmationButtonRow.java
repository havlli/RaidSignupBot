package com.github.havlli.raidsignupbot.component;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfirmationButtonRow implements ButtonRowComponent {

    @Override
    public ActionRow getActionRow() {
        return ActionRow.of(buildButtons());
    }

    private List<Button> buildButtons() {
        Button confirmButton = Button.primary("confirm","Confirm");
        Button confirmCancel = Button.danger("cancel", "Cancel");

        List<Button> buttons = new ArrayList<>();
        buttons.add(confirmButton);
        buttons.add(confirmCancel);

        return buttons;
    }

    @Override
    public List<String> getCustomIds() {
        return buildButtons().stream()
                .map(button -> button.getCustomId().orElse("no-id"))
                .collect(Collectors.toList());
    }
}
