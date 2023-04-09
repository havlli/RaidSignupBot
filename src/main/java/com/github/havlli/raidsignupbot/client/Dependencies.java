package com.github.havlli.raidsignupbot.client;

import com.github.havlli.raidsignupbot.database.JdbcConnectionProvider;
import com.github.havlli.raidsignupbot.embedevent.EmbedEventDAO;
import com.github.havlli.raidsignupbot.signupuser.SignupUserDAO;

public class Dependencies {

    private static Dependencies instance;
    private final JdbcConnectionProvider jdbcProvider;
    private final SignupUserDAO signupUserDAO;
    private final EmbedEventDAO embedEventDAO;

    private Dependencies() {
        this.jdbcProvider = new JdbcConnectionProvider();
        this.signupUserDAO = new SignupUserDAO(jdbcProvider);
        this.embedEventDAO = new EmbedEventDAO(jdbcProvider);
    }

    public static Dependencies getInstance() {
        if (instance == null) instance = new Dependencies();

        return instance;
    }

    public SignupUserDAO getSignupUserDAO() {
        return signupUserDAO;
    }

    public EmbedEventDAO getEmbedEventDAO() {
        return embedEventDAO;
    }
}
