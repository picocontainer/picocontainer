/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.script;

import com.picocontainer.Characteristics;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.PicoContainer;

/**
 * Default builder creates an empty caching DefaultPicoContainer.
 * <p>Note that assembly scope is ignored when creating the container.</p>
 */
public class DefaultContainerBuilder extends AbstractContainerBuilder {

    public DefaultContainerBuilder() {
        //default constructor
    }

    // TODO better solution to activate default caching
    @Override
    protected PicoContainer createContainer(final PicoContainer parentContainer, final Object assemblyScope) {
        return (new DefaultPicoContainer(parentContainer)).change(Characteristics.CACHE);
    }
}