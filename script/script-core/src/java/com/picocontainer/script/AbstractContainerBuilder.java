/*******************************************************************************
 * Copyright (C)  PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.script;

import com.picocontainer.script.util.MultiException;

import com.picocontainer.Disposable;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoContainer;
import com.picocontainer.Startable;

/**
 * Template implementation of a ContainerBuilder.
 * @author Joe Walnes
 * @author Aslak Helles&oslash;y
 * @author Paul Hammant
 * @author Mauro Talevi
 */
public abstract class AbstractContainerBuilder implements ContainerBuilder {

	private PostBuildContainerAction postBuildAction = new StartContainerPostBuildContainerAction();

    public AbstractContainerBuilder() {
    	super();
    }

    public final PicoContainer buildContainer(final PicoContainer parentContainer, final Object assemblyScope,
            final boolean addChildToParent) {
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

        container = postBuildAction.onNewContainer(container);

        return container;
    }

    /**
     * Removes the container from the parent if possible.
     */
    public void killContainer(final PicoContainer container) {
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


    /**
     * Allows you to set the post container build actions.  (After script is executed,
     * before container is returned).
     * @param action the action to perform, may not be null.
     * @return <em>this</em> to allow for method chaining.
     */
    public AbstractContainerBuilder setPostBuildAction(final PostBuildContainerAction action) {
    	if (action == null) {
    		throw new NullPointerException("action -- if you want no action taken, use " + NoOpPostBuildContainerAction.class.getName());
    	}
    	this.postBuildAction = action;
    	return this;
    }
}
