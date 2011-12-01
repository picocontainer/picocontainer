/*
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.picocontainer.logging.store;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Class that extends base formatter to provide raw text output.
 * 
 * @author Peter Donald
 */
public class JdkRawFormatter extends Formatter {
    public String format(final LogRecord record) {
        return formatMessage(record) + "\n";
    }
}
