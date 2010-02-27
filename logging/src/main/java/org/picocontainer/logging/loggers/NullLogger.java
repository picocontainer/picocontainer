/*
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.picocontainer.logging.loggers;

import org.picocontainer.logging.Logger;

public class NullLogger implements Logger {

    public void trace(Object message) {
    }

    public void trace(Object message, Throwable throwable) {
    }

    public boolean isTraceEnabled() {
        return false;
    }

    public void debug(Object message) {
    }

    public void debug(Object message, Throwable throwable) {
    }

    public boolean isDebugEnabled() {
        return false;
    }

    public void info(Object message) {
    }

    public void info(Object message, Throwable throwable) {
    }

    public boolean isInfoEnabled() {
        return false;
    }

    public void warn(Object message) {
    }

    public void warn(Object message, Throwable throwable) {
    }

    public boolean isWarnEnabled() {
        return false;
    }

    public void error(Object message) {
    }

    public void error(Object message, Throwable throwable) {
    }

    public boolean isErrorEnabled() {
        return false;
    }

    public void fatal(Object message) {
    }

    public void fatal(Object message, Throwable throwable) {
    }

    public boolean isFatalEnabled() {
        return false;
    }

    public Logger getChildLogger(String name) {
        return this;
    }
}
