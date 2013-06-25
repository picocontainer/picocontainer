/*
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.picocontainer.logging.loggers;

import static org.apache.log4j.Level.DEBUG;
import static org.apache.log4j.Level.ERROR;
import static org.apache.log4j.Level.FATAL;
import static org.apache.log4j.Level.INFO;
import static org.apache.log4j.Level.WARN;

import com.picocontainer.logging.Logger;

/**
 * Logger implementation that delegates to Apache Log4J logger, using the
 * following mapping to the Log4J log levels:
 * <ul>
 * <li>trace ==&gt; debug</li>
 * <li>debug ==&gt; debug</li>
 * <li>info ==&gt; info</li>
 * <li>warn ==&gt; warn</li>
 * <li>error ==&gt; error</li>
 * <li>fatal ==&gt; fatal</li>
 * </ul>
 */
public class Log4JLogger implements Logger {
    /**
     * The fully qualified name to be included in the Log4J logs
     */
    private static final String FQCN = Log4JLogger.class.getName();

    /**
     * The log4j logger instance.
     */
    private final org.apache.log4j.Logger logger;

    /**
     * Create a Log4JLogger with a given log4j logger
     * 
     * @param logger the log4j logger
     */
    public Log4JLogger(final org.apache.log4j.Logger logger) {
        if (null == logger) {
            throw new NullPointerException("logger");
        }
        this.logger = logger;
    }

    /**
     * Log a trace message.
     * 
     * @param message the message
     */
    public void trace(final Object message) {
        this.logger.log(FQCN, DEBUG, message, null);
    }

    /**
     * Log a trace message with an associated throwable.
     * 
     * @param message the message
     * @param throwable the throwable
     */
    public void trace(final Object message, final Throwable throwable) {
        this.logger.log(FQCN, DEBUG, message, throwable);
    }

    /**
     * Return true if a trace message will be logged.
     * 
     * @return true if message will be logged
     */
    public boolean isTraceEnabled() {
        return this.logger.isDebugEnabled();
    }

    /**
     * Log a debug message.
     * 
     * @param message the message
     */
    public void debug(final Object message) {
        this.logger.log(FQCN, DEBUG, message, null);
    }

    /**
     * Log a debug message with an associated throwable.
     * 
     * @param message the message
     * @param throwable the throwable
     */
    public void debug(final Object message, final Throwable throwable) {
        this.logger.log(FQCN, DEBUG, message, throwable);
    }

    /**
     * Return true if a debug message will be logged.
     * 
     * @return true if message will be logged
     */
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    /**
     * Log a info message.
     * 
     * @param message the message
     */
    public void info(final Object message) {
        this.logger.log(FQCN, INFO, message, null);
    }

    /**
     * Log a info message with an associated throwable.
     * 
     * @param message the message
     * @param throwable the throwable
     */
    public void info(final Object message, final Throwable throwable) {
        this.logger.log(FQCN, INFO, message, throwable);
    }

    /**
     * Return true if an info message will be logged.
     * 
     * @return true if message will be logged
     */
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    /**
     * Log a warn message.
     * 
     * @param message the message
     */
    public void warn(final Object message) {
        this.logger.log(FQCN, WARN, message, null);
    }

    /**
     * Log a warn message with an associated throwable.
     * 
     * @param message the message
     * @param throwable the throwable
     */
    public void warn(final Object message, final Throwable throwable) {
        this.logger.log(FQCN, WARN, message, throwable);
    }

    /**
     * Return true if a warn message will be logged.
     * 
     * @return true if message will be logged
     */
    public boolean isWarnEnabled() {
        return this.logger.isEnabledFor(WARN);
    }

    /**
     * Log a error message.
     * 
     * @param message the message
     */
    public void error(final Object message) {
        this.logger.log(FQCN, ERROR, message, null);
    }

    /**
     * Log a error message with an associated throwable.
     * 
     * @param message the message
     * @param throwable the throwable
     */
    public void error(final Object message, final Throwable throwable) {
        this.logger.log(FQCN, ERROR, message, throwable);
    }

    /**
     * Return true if a error message will be logged.
     * 
     * @return true if message will be logged
     */
    public boolean isErrorEnabled() {
        return this.logger.isEnabledFor(ERROR);
    }

    /**
     * Log a fatal message.
     * 
     * @param message the message
     */
    public void fatal(final Object message) {
        this.logger.log(FQCN, FATAL, message, null);
    }

    /**
     * Log a fatal message with an associated throwable.
     * 
     * @param message the message
     * @param throwable the throwable
     */
    public void fatal(final Object message, final Throwable throwable) {
        this.logger.log(FQCN, FATAL, message, throwable);
    }

    /**
     * Return true if a fatal message will be logged.
     * 
     * @return true if message will be logged
     */
    public boolean isFatalEnabled() {
        return this.logger.isEnabledFor(FATAL);
    }

    /**
     * Get the child logger with specified name.
     * 
     * @param name the name of child logger
     * @return the child logger
     */
    public Logger getChildLogger(final String name) {
        return new Log4JLogger(org.apache.log4j.Logger.getLogger(this.logger.getName() + "." + name));
    }
}
