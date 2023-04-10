package com.github.havlli.raidsignupbot.client;

import com.github.havlli.raidsignupbot.database.ConnectionProvider;
import com.github.havlli.raidsignupbot.database.JdbcConnectionProvider;
import com.github.havlli.raidsignupbot.embedevent.EmbedEventDAO;
import com.github.havlli.raidsignupbot.logger.ConsoleLogger;
import com.github.havlli.raidsignupbot.logger.Logger;
import com.github.havlli.raidsignupbot.logger.MessagePrinter;
import com.github.havlli.raidsignupbot.logger.TextFormatter;
import com.github.havlli.raidsignupbot.signupuser.SignupUserDAO;

public class Dependencies {

    private static Dependencies instance;
    private final SignupUserDAO signupUserDAO;
    private final EmbedEventDAO embedEventDAO;
    private final Logger logger;


    private Dependencies() {
        ConnectionProvider connectionProvider = new JdbcConnectionProvider();
        this.signupUserDAO = new SignupUserDAO(connectionProvider);
        this.embedEventDAO = new EmbedEventDAO(connectionProvider);
        this.logger = new ConsoleLogger(new MessagePrinter(), new TextFormatter());

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

    public Logger getLogger() {
        return logger;
    }
}
