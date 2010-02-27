/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.ObjectReference;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.ComponentLifecycle;

import java.lang.reflect.Type;
import java.io.Serializable;

/*
 * behaviour for all behaviours wishing to store
 * their component in "awkward places" ( object references )
 *
 * @author Konstantin Pribluda
 */
@SuppressWarnings("serial")
public class Stored<T> extends AbstractBehavior<T> {

    private final ObjectReference<Instance<T>> instanceReference;
    private final ComponentLifecycle lifecycleDelegate;

    public Stored(ComponentAdapter<T> delegate, ObjectReference<Instance<T>> reference) {
        super(delegate);
        instanceReference = reference;
        this.lifecycleDelegate = hasLifecycle(delegate)
                ? new RealComponentLifecycle<T>() : new NoComponentLifecycle<T>();
    }

    private void guardInstRef() {
        if (instanceReference.get() == null) {
            instanceReference.set(new Instance<T>());
        }
    }

    public boolean componentHasLifecycle() {
        return lifecycleDelegate.componentHasLifecycle();
    }

    /**
     * Disposes the cached component instance
     * {@inheritDoc}
     */
    public void dispose(PicoContainer container) {
        lifecycleDelegate.dispose(container);
    }

    /**
     * Retrieves the stored reference.  May be null if it has
     * never been set, or possibly if the reference has been
     * flushed.
     *
     * @return the stored object or null.
     */
    public T getStoredObject() {
        guardInstRef();
        return instanceReference.get().instance;
    }

    /**
     * Flushes the cache.
     * If the component instance is started is will stop and dispose it before
     * flushing the cache.
     */
    public void flush() {
        Instance<T> inst = instanceReference.get();
        if (inst != null) {
            Object instance = inst.instance;
            if (instance != null && instanceReference.get().started) {
                stop(instance);
                dispose(instance);
            }
            instanceReference.set(null);
        }
    }

    public T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
        guardInstRef();
        T instance = instanceReference.get().instance;
        if (instance == null) {
            instance = super.getComponentInstance(container, into);
            instanceReference.get().instance = instance;
        }
        return instance;
    }

    public String getDescriptor() {
        return "Stored" + getLifecycleDescriptor();
    }

    protected String getLifecycleDescriptor() {
        return (lifecycleDelegate.componentHasLifecycle() ? "+Lifecycle" : "");
    }

    /**
     * Starts the cached component instance
     * {@inheritDoc}
     */
    public void start(PicoContainer container) {
        lifecycleDelegate.start(container);
    }

    /**
     * Stops the cached component instance
     * {@inheritDoc}
     */
    public void stop(PicoContainer container) {
        lifecycleDelegate.stop(container);
    }

    public boolean isStarted() {
        return lifecycleDelegate.isStarted();
    }

    private class RealComponentLifecycle<T> implements ComponentLifecycle<T>, Serializable {

        public void start(PicoContainer container) {
            guardInstRef();
            guardAlreadyDisposed();
            guardStartState(true, "already started");
            // Lazily make the component if applicable
            Stored.this.start(getComponentInstance(container, NOTHING.class));
            instanceReference.get().started = true;
        }

        public void stop(PicoContainer container) {
            guardInstRef();
            guardAlreadyDisposed();
            guardNotInstantiated();
            guardStartState(false, "not started");
            Stored.this.stop(instanceReference.get().instance);
            instanceReference.get().started = false;

        }

        public void dispose(PicoContainer container) {
            guardInstRef();
            Instance<?> instance = instanceReference.get();
            if (instance.instance != null) {
                guardAlreadyDisposed();
                Stored.this.dispose(instance.instance);
                instance.disposed = true;
            }
        }


        private void guardNotInstantiated() {
            if (instanceReference.get().instance == null)
                throw new IllegalStateException("'" + getComponentKey() + "' not instantiated");
        }

        private void guardStartState(boolean unexpectedStartState, String message) {
            if (instanceReference.get().started == unexpectedStartState)
                throw new IllegalStateException("'" + getComponentKey() + "' " + message);
        }

        private void guardAlreadyDisposed() {
            if (instanceReference.get().disposed)
                throw new IllegalStateException("'" + getComponentKey() + "' already disposed");
        }

        public boolean componentHasLifecycle() {
            return true;
        }

        public boolean isStarted() {
            guardInstRef();
            return instanceReference.get().started;
        }
    }

    private static class NoComponentLifecycle<T> implements ComponentLifecycle<T>, Serializable {
        public void start(PicoContainer container) {
        }

        public void stop(PicoContainer container) {
        }

        public void dispose(PicoContainer container) {
        }

        public boolean componentHasLifecycle() {
            return false;
        }

        public boolean isStarted() {
            return false;
        }
    }

    private static boolean hasLifecycle(ComponentAdapter delegate) {
        return delegate instanceof LifecycleStrategy
                && ((LifecycleStrategy) delegate).hasLifecycle(delegate.getComponentImplementation());
    }

    public static class Instance<T> implements Serializable {
        private T instance;
        protected boolean started;
        protected boolean disposed;
    }

}
