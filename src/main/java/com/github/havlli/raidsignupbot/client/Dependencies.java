package com.github.havlli.raidsignupbot.client;

import com.github.havlli.raidsignupbot.database.ConnectionProvider;
import com.github.havlli.raidsignupbot.database.JdbcConnectionProvider;
import com.github.havlli.raidsignupbot.embedevent.EmbedEventDAO;
import com.github.havlli.raidsignupbot.embedevent.EmbedEventPersistence;
import com.github.havlli.raidsignupbot.embedevent.EmbedEventService;
import com.github.havlli.raidsignupbot.logger.ConsoleLogger;
import com.github.havlli.raidsignupbot.logger.Logger;
import com.github.havlli.raidsignupbot.logger.MessagePrinter;
import com.github.havlli.raidsignupbot.logger.TextFormatter;
import com.github.havlli.raidsignupbot.signupuser.SignupUserDAO;
import com.github.havlli.raidsignupbot.signupuser.SignupUserService;

public class Dependencies {

    private static Dependencies instance;
    private final SignupUserDAO signupUserDAO;
    private final SignupUserService signupUserService;
    private final EmbedEventDAO embedEventDAO;
    private final EmbedEventPersistence embedEventPersistence;
    private final EmbedEventService embedEventService;
    private final Logger logger;


    private Dependencies() {
        ConnectionProvider connectionProvider = new JdbcConnectionProvider();
        this.signupUserDAO = new SignupUserDAO(connectionProvider);
        this.signupUserService = new SignupUserService(signupUserDAO);
        this.embedEventDAO = new EmbedEventDAO(connectionProvider);
        this.embedEventPersistence = new EmbedEventPersistence(embedEventDAO, signupUserDAO);
        this.embedEventService = new EmbedEventService(embedEventDAO, embedEventPersistence);
        this.logger = new ConsoleLogger(new MessagePrinter(), new TextFormatter());

    }

    public static Dependencies getInstance() {
        if (instance == null) instance = new Dependencies();

        return instance;
    }

    public SignupUserDAO getSignupUserDAO() {
        return signupUserDAO;
    }

    public SignupUserService getSignupUserService() {
        return signupUserService;
    }

    public EmbedEventDAO getEmbedEventDAO() {
        return embedEventDAO;
    }

    public EmbedEventPersistence getEmbedEventPersistence() {
        return embedEventPersistence;
    }

    public EmbedEventService getEmbedEventService() {
        return embedEventService;
    }

    public Logger getLogger() {
        return logger;
    }
}
