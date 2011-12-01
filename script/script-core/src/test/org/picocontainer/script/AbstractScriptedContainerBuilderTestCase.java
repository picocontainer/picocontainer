/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script;

import org.picocontainer.PicoContainer;

/**
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
public abstract class AbstractScriptedContainerBuilderTestCase {

    protected PicoContainer buildContainer(ContainerBuilder builder, PicoContainer parentContainer,
            Object assemblyScope) {
        return buildContainer(builder, parentContainer, assemblyScope, true);
    }

    protected PicoContainer buildContainer(ContainerBuilder builder, PicoContainer parentContainer,
            Object assemblyScope, boolean addChildToParent) {
        return builder.buildContainer(parentContainer, assemblyScope, addChildToParent);
    }

}
