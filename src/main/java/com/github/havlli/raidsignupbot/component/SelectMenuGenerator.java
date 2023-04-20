package com.github.havlli.raidsignupbot.component;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import java.util.List;

public class SelectMenuGenerator implements ActionRowGenerator {

    private final String menuId;
    private final List<SelectMenu.Option> options;
    private final String placeholder;
    private final int maxSelectedValues;
    private final int minSelectedValues;
    private final boolean isDisabled;

    public SelectMenuGenerator(String menuId,
                               List<SelectMenu.Option> options,
                               String placeholder,
                               int maxSelectedValues,
                               int minSelectedValues,
                               boolean isDisabled) {
        this.menuId = menuId;
        this.options = options;
        this.placeholder = placeholder;
        this.maxSelectedValues = maxSelectedValues;
        this.minSelectedValues = minSelectedValues;
        this.isDisabled = isDisabled;
    }

    @Override
    public ActionRow generate() {
        SelectMenu selectMenu;
        if (isDisabled) {
            selectMenu = SelectMenu.of(menuId, options)
                    .withPlaceholder(placeholder)
                    .disabled();
        } else {
            selectMenu = SelectMenu.of(menuId, options)
                    .withPlaceholder(placeholder)
                    .withMaxValues(maxSelectedValues)
                    .withMinValues(minSelectedValues);
        }

        return ActionRow.of(selectMenu);
    }
}
