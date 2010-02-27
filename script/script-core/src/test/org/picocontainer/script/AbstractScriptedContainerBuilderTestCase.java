/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script;

import org.picocontainer.PicoContainer;
import org.picocontainer.script.ScriptedContainerBuilder;

/**
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
public abstract class AbstractScriptedContainerBuilderTestCase {

    protected PicoContainer buildContainer(ScriptedContainerBuilder builder, PicoContainer parentContainer,
            Object assemblyScope) {
        return buildContainer(builder, parentContainer, assemblyScope, true);
    }

    protected PicoContainer buildContainer(ScriptedContainerBuilder builder, PicoContainer parentContainer,
            Object assemblyScope, boolean addChildToParent) {
        return builder.buildContainer(parentContainer, assemblyScope, addChildToParent);
    }

}
