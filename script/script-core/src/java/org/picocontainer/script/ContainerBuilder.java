/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ----------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script;

import org.picocontainer.PicoContainer;

/**
 * The responsibility of a ContainerBuilder is to build containers. Composition
 * of containers is generally a separate reponsibility, although the some
 * builders may make use of the assembly scope.
 * 
 * @author Joe Walnes
 * @author Mauro Talevi
 */
public interface ContainerBuilder {

    /**
     * Builds a new container
     * 
     * @param parentContainer the parent PicoContainer (may be <code>null</code>).
     * @param assemblyScope a hint about the assembly scope (may be
     *            <code>null</code>)
     * @param addChildToParent boolean flag, <code>true</code> if the child is
     *            to be added to the parent
     * @return A PicoContainer
     */
    PicoContainer buildContainer(PicoContainer parentContainer, Object assemblyScope, boolean addChildToParent);

    /**
     * Destroys a container.
     * 
     * @param container the PicoContainer to be killed
     */
    void killContainer(PicoContainer container);

}
