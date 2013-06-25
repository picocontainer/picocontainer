/*
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.picocontainer.logging.store;

import com.picocontainer.logging.Logger;
import com.picocontainer.logging.store.stores.AbstractLoggerStore;

/**
 * @author Peter Donald
 */
public class MalformedLoggerStore extends AbstractLoggerStore {
    protected Logger createLogger(String name) {
        return null;
    }

    public void close() {
    }
}
