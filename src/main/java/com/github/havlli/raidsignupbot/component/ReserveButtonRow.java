package com.github.havlli.raidsignupbot.component;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;

import java.util.ArrayList;
import java.util.List;

public class ReserveButtonRow implements ActionRowComponent {

    @Override
    public ActionRow getActionRow() {
        return ActionRow.of(buildButtons());
    }

    private List<Button> buildButtons() {
        Button buttonYes = Button.primary("reserveYes","Yes");
        Button buttonNo = Button.danger("reserveNo", "No");

        List<Button> buttons = new ArrayList<>();
        buttons.add(buttonYes);
        buttons.add(buttonNo);

        return buttons;
    }
}
