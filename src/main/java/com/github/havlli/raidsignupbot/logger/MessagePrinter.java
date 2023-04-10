package com.github.havlli.raidsignupbot.logger;

import java.io.PrintWriter;

public class MessagePrinter {

    public void writeMessage(Message message, Formatter formatter, PrintWriter printWriter) {
        printWriter.println(formatter.format(message));
        printWriter.flush();
    }
}
