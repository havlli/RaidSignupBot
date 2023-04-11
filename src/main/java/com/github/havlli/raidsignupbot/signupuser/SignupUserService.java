package com.github.havlli.raidsignupbot.signupuser;

import java.util.List;

public class SignupUserService {
    private final SignupUserDAO signupUserDAO;

    public SignupUserService(SignupUserDAO signupUserDAO) {
        this.signupUserDAO = signupUserDAO;
    }

    public SignupUser getSignupUser(String userId, List<SignupUser> signupUsers) {
        return signupUsers.stream()
                .filter(signupUser -> signupUser.getId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    public void addSignupUser(SignupUser signupUser, List<SignupUser> signupUsers, String embedEventId) {
        signupUsers.add(signupUser);
        signupUserDAO.insertSignupUser(signupUser, embedEventId);
    }

    public void updateSignupUser(SignupUser signupUser, String embedEventId, int fieldIndex) {
        signupUser.setFieldIndex(fieldIndex);
        signupUserDAO.updateSignupUserFieldIndex(signupUser.getId(), signupUser.getFieldIndex(), embedEventId);
    }
}
