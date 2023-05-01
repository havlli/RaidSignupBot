package com.github.havlli.raidsignupbot.events.editevent;

public enum EditField {
    NAME("name"),
    DESCRIPTION("description"),
    DATE("date"),
    TIME("time"),
    INSTANCES("instances"),
    RESERVE("reserve");

    private final String stringValue;
    EditField(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public static EditField fromStringValue(String type) {
        for (EditField editField : EditField.values()) {
            if (editField.stringValue.equals(type)) {
                return editField;
            }
        }
        throw new IllegalArgumentException("Invalid string value: " + type);
    }

    @Override
    public String toString() {
        return "EditField{" +
                "=" + stringValue +
                "}";
    }
}
