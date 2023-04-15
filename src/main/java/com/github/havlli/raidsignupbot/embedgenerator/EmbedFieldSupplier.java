package com.github.havlli.raidsignupbot.embedgenerator;

import com.github.havlli.raidsignupbot.embedevent.EmbedEvent;
import com.github.havlli.raidsignupbot.signupuser.SignupUser;
import discord4j.core.spec.EmbedCreateFields;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmbedFieldSupplier implements FieldSupplier {

    @Override
    public List<EmbedCreateFields.Field> getPopulatedFields(EmbedEvent embedEvent) {
        List<EmbedCreateFields.Field> populatedFields = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : EmbedFields.getFieldsMap().entrySet()) {
            int fieldIndex = entry.getKey();
            String fieldName = entry.getValue();

            List<SignupUser> signupUsers = embedEvent.getSignupUsers();
            List<SignupUser> matchingUsers = getMatchingUsers(fieldIndex, signupUsers);
            if (!matchingUsers.isEmpty()) {
                boolean isOneLineField = fieldIndex < 0;
                String fieldConcat = buildFieldConcat(fieldName, matchingUsers, isOneLineField);

                if (isOneLineField) {
                    populatedFields.add(EmbedCreateFields.Field.of("", fieldConcat, false));
                } else {
                    populatedFields.add(EmbedCreateFields.Field.of(fieldConcat, "", true));
                }
            }
        }

        return populatedFields;
    }

    private List<SignupUser> getMatchingUsers(int fieldIndex, List<SignupUser> signupUsers) {
        return signupUsers.stream()
                .filter(user -> user.getFieldIndex() == fieldIndex)
                .collect(Collectors.toList());
    }

    private String buildFieldConcat(String fieldName, List<SignupUser> matchingUsers, boolean isOneLineField) {
        int count = matchingUsers.size();
        String lineSeparator = isOneLineField ? ", " : "\n";

        return String.format("%s (%d):%s%s",
                fieldName,
                count,
                isOneLineField ? " " : "\n",
                matchingUsers.stream()
                        .map(user -> String.format("`%d`%s", user.getOrder(), user.getUsername()))
                        .collect(Collectors.joining(lineSeparator))
        );
    }
}
