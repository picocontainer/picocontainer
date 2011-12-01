/*
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.picocontainer.logging;


/**
 * Facade for different Logger systems.
 * 
 * @author Mauro Talevi
 * @author Peter Donald
 */
public interface Logger {

    /**
     * Log a trace message.
     * 
     * @param message the message
     */
    void trace(Object message);

    /**
     * Log a trace message with an associated throwable.
     * 
     * @param message the message
     * @param throwable the throwable
     */
    void trace(Object message, Throwable throwable);

    /**
     * Return true if a trace message will be logged.
     * 
     * @return true if message will be logged
     */
    boolean isTraceEnabled();

    /**
     * Log a debug message.
     * 
     * @param message the message
     */
    void debug(Object message);

    /**
     * Log a debug message with an associated throwable.
     * 
     * @param message the message
     * @param throwable the throwable
     */
    void debug(Object message, Throwable throwable);

    /**
     * Return true if a debug message will be logged.
     * 
     * @return true if message will be logged
     */
    boolean isDebugEnabled();

    /**
     * Log a info message.
     * 
     * @param message the message
     */
    void info(Object message);

    /**
     * Log a info message with an associated throwable.
     * 
     * @param message the message
     * @param throwable the throwable
     */
    void info(Object message, Throwable throwable);

    /**
     * Return true if an info message will be logged.
     * 
     * @return true if message will be logged
     */
    boolean isInfoEnabled();

    /**
     * Log a warn message.
     * 
     * @param message the message
     */
    void warn(Object message);

    /**
     * Log a warn message with an associated throwable.
     * 
     * @param message the message
     * @param throwable the throwable
     */
    void warn(Object message, Throwable throwable);

    /**
     * Return true if a warn message will be logged.
     * 
     * @return true if message will be logged
     */
    boolean isWarnEnabled();

    /**
     * Log a error message.
     * 
     * @param message the message
     */
    void error(Object message);

    /**
     * Log a error message with an associated throwable.
     * 
     * @param message the message
     * @param throwable the throwable
     */
    void error(Object message, Throwable throwable);

    /**
     * Return true if a error message will be logged.
     * 
     * @return true if message will be logged
     */
    boolean isErrorEnabled();

    /**
     * Log a fatal message.
     * 
     * @param message the message
     */
    void fatal(Object message);

    /**
     * Log a fatal message with an associated throwable.
     * 
     * @param message the message
     * @param throwable the throwable
     */
    void fatal(Object message, Throwable throwable);

    /**
     * Return true if a fatal message will be logged.
     * 
     * @return true if message will be logged
     */
    boolean isFatalEnabled();

    /**
     * Get the child logger with specified name.
     * 
     * @param name the name of child logger
     * @return the child logger
     */
    Logger getChildLogger(String name);
}
