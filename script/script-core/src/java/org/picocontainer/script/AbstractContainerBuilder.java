/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script;

import org.picocontainer.Disposable;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.Startable;
import org.picocontainer.script.util.MultiException;

/**
 * Template implementation of a ContainerBuilder.
 * @author Joe Walnes
 * @author Aslak Helles&oslash;y
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public abstract class AbstractContainerBuilder implements ContainerBuilder {

    public AbstractContainerBuilder() {
    	super();
    }

    public final PicoContainer buildContainer(PicoContainer parentContainer, Object assemblyScope,
            boolean addChildToParent) {
        PicoContainer container = createContainer(parentContainer, assemblyScope);

        if (parentContainer != null && parentContainer instanceof MutablePicoContainer) {
            MutablePicoContainer mutableParentContainer = (MutablePicoContainer) parentContainer;

            if (addChildToParent) {
                // this synchronization is necessary to avoid
                // race conditions for concurrent requests
                synchronized (mutableParentContainer) {
                    // register the child in the parent so that lifecycle can be
                    // propagated down the hierarchy
                    mutableParentContainer.addChildContainer(container);
                }
            }
        }

        return container;
    }

    /**
     * Removes the container from the parent if possible.
     */
    public void killContainer(PicoContainer container) {
    	MultiException ex = new MultiException("killContainer(" + container + ")");
        try {
			if (container instanceof Startable) {
			    ((Startable) container).stop();
			}
		} catch (Exception e) {
			ex.addException(e);
		}

        try {
        	if (container instanceof Disposable) {
        		((Disposable) container).dispose();
        	}
		} catch (Exception e) {
			ex.addException(e);
		}
    	
    	
        PicoContainer parent = container.getParent();
        if (parent != null && parent instanceof MutablePicoContainer) {
            // see comment in buildContainer
            try {
				synchronized (parent) {
				    ((MutablePicoContainer) parent).removeChildContainer(container);
				}
			} catch (Exception e) {
				ex.addException(e);
			}
        }
        
        if (ex.getErrorCount() > 0) {
        	throw ex;
        }
    }

    protected abstract PicoContainer createContainer(PicoContainer parentContainer, Object assemblyScope);
}
