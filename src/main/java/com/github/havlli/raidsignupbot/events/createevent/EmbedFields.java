package com.github.havlli.raidsignupbot.events.createevent;

import java.util.HashMap;
import java.util.Map;

public class EmbedFields {

    private static final HashMap<Integer, String> fieldsMap = new HashMap<>(Map.of(
            -1, "Absence",
            -2, "Late",
            -3, "Tentative",
            1, "Tank",
            2, "Melee",
            3, "Ranged",
            4, "Healer",
            5, "Support"
    ));

    public static HashMap<Integer, String> getFieldsMap() {
        return fieldsMap;
    }
}
