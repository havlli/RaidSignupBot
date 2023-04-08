package com.github.havlli.raidsignupbot.signupuser;

import java.util.Collections;
import java.util.List;

public class SignupUserService {
    private final List<SignupUser> signupUsers;
    private final SignupUserDAO signupUserDAO;

    public SignupUserService(List<SignupUser> signupUsers, SignupUserDAO signupUserDAO) {
        this.signupUsers = signupUsers;
        this.signupUserDAO = signupUserDAO;
    }

    public List<SignupUser> getSignupUsers() {
        return Collections.unmodifiableList(signupUsers);
    }

    public SignupUser getSignupUser(String userId) {
        return signupUsers.stream()
                .filter(signupUser -> signupUser.getId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    public void addSignupUser(SignupUser signupUser, String embedEventId) {
        signupUsers.add(signupUser);
        signupUserDAO.insertSignupUser(signupUser, embedEventId);
    }

    public void updateSignupUser(SignupUser signupUser, String embedEventId) {
        signupUserDAO.updateSignupUserFieldIndex(signupUser.getId(), signupUser.getFieldIndex(), embedEventId);
    }
}
