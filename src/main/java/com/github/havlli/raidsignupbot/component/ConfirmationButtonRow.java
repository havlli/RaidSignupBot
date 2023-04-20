package com.github.havlli.raidsignupbot.component;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import java.util.ArrayList;
import java.util.List;

public class ConfirmationButtonRow implements ActionRowComponent {

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
}
