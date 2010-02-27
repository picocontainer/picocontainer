/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.Startable;
import org.picocontainer.Disposable;

/**
 * @author Joe Walnes
 * @author Aslak Helles&oslash;y
 * @author Paul Hammant
 * @author Mauro Talevi
 */
// TODO -- Perhaps the start/stop behavior should be moved to a decorator?
public abstract class AbstractContainerBuilder implements ContainerBuilder {

    private final LifecycleMode startMode;

    public AbstractContainerBuilder() {
        this(LifecycleMode.AUTO_LIFECYCLE);
    }

    public AbstractContainerBuilder(LifecycleMode startMode) {
        this.startMode = startMode;
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

        autoStart(container);

        return container;
    }

    protected void autoStart(PicoContainer container) {
        if (!startMode.isInvokeLifecycle()) {
            return;
        }

        if (container instanceof Startable) {
            ((Startable) container).start();
        }
    }

    public void killContainer(PicoContainer container) {
        if (startMode.isInvokeLifecycle()) {
            if (container instanceof Startable) {
                ((Startable) container).stop();
            }
        }

        if (container instanceof Disposable) {
            ((Disposable) container).dispose();
        }
        PicoContainer parent = container.getParent();
        if (parent != null && parent instanceof MutablePicoContainer) {
            // see comment in buildContainer
            synchronized (parent) {
                ((MutablePicoContainer) parent).removeChildContainer(container);
            }
        }
    }

    protected abstract PicoContainer createContainer(PicoContainer parentContainer, Object assemblyScope);
}
