package com.github.havlli.raidsignupbot.logger;

import java.io.PrintWriter;

public class ConsoleLogger implements Logger {

    private final MessagePrinter messagePrinter;
    private final Formatter formatter;
    private final PrintWriter printWriter;

    public ConsoleLogger(
            MessagePrinter messagePrinter,
            Formatter formatter
    ) {
        this.messagePrinter = messagePrinter;
        this.formatter = formatter;
        this.printWriter = new PrintWriter(System.out);
    }

    @Override
    public void log(String messageString) {

        Message message = new Message(messageString);
        messagePrinter.writeMessage(message, formatter, printWriter);
    }
}
