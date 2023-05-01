package com.github.havlli.raidsignupbot.commands;

import java.util.List;

public class Commands {

    public static final List<String> COMMANDS = List.of(
            "create-event.json",
            "delete-event.json",
            "edit-event.json",
            "test.json"
    );

    public static List<String> getCommandsList() {
        return COMMANDS;
    }

}
