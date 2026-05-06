package com.jftse.emulator.common.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

public final class LogConfigurator {
    public static void setConsoleOutput(String loggerName, boolean enabled) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);
        if (loggerConfig != null) {
            final boolean hasConsoleAppender = loggerConfig.getAppenders().containsKey("Console");
            if (enabled && !hasConsoleAppender) {
                loggerConfig.addAppender(config.getAppender("Console"), null, null);
            } else if (!enabled && hasConsoleAppender) {
                loggerConfig.removeAppender("Console");
            }
            ctx.updateLoggers();
        }
    }
}
