/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.script;

import com.picocontainer.PicoException;

/**
 * Indicates that a given class for a builder was not found by the ScriptedContainerBuilderFactory
 * when trying to use its specified classloader.
 *
 * @author Michael Rimov
 */
@SuppressWarnings("serial")
public class BuilderClassNotFoundException extends PicoException {

    public BuilderClassNotFoundException(final String message) {
        super(message);
    }

    public BuilderClassNotFoundException(final Throwable cause) {
        super(cause);
    }

    public BuilderClassNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
