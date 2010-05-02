/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package org.picocontainer.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentLifecycle;
import org.picocontainer.ObjectReference;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoContainer;
import org.picocontainer.references.ThreadLocalMapObjectReference;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
//import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class Storing extends AbstractBehavior {

    private final StoreThreadLocal mapThreadLocalObjectReference = new StoreThreadLocal();

    public <T> ComponentAdapter<T>  createComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycle, Properties componentProps, final Object key, Class<T> impl, Parameter... parameters)

            throws PicoCompositionException {
        if (removePropertiesIfPresent(componentProps, Characteristics.NO_CACHE)) {
            return super.createComponentAdapter(monitor,
                                                                             lifecycle,
                                                                             componentProps,
                                                                             key,
                                                                             impl,
                                                                             parameters);
        }
        removePropertiesIfPresent(componentProps, Characteristics.CACHE);
        ThreadLocalMapObjectReference threadLocalMapObjectReference = new ThreadLocalMapObjectReference(mapThreadLocalObjectReference, key);

        return monitor.newBehavior(new Stored<T>(super.createComponentAdapter(monitor, lifecycle,
                                                                componentProps, key, impl, parameters),
                threadLocalMapObjectReference));

    }

    public <T> ComponentAdapter<T> addComponentAdapter(ComponentMonitor monitor,
                                    LifecycleStrategy lifecycle,
                                    Properties componentProps,
                                    final ComponentAdapter<T> adapter) {
        if (removePropertiesIfPresent(componentProps, Characteristics.NO_CACHE)) {
            return super.addComponentAdapter(monitor, lifecycle, componentProps, adapter);
        }
        removePropertiesIfPresent(componentProps, Characteristics.CACHE);

        return monitor.newBehavior(new Stored<T>(super.addComponentAdapter(monitor, lifecycle, componentProps, adapter),
                          new ThreadLocalMapObjectReference(mapThreadLocalObjectReference, adapter.getComponentKey())));
    }

    public StoreWrapper getCacheForThread() {
        StoreWrapper wrappedMap = new StoreWrapper();
        wrappedMap.wrapped = (Map)mapThreadLocalObjectReference.get();
        return wrappedMap;
    }

    public void putCacheForThread(StoreWrapper wrappedMap) {
        mapThreadLocalObjectReference.set(wrappedMap.wrapped);
    }

    public StoreWrapper resetCacheForThread() {
        Map map = new HashMap();
        mapThreadLocalObjectReference.set(map);
        StoreWrapper storeWrapper = new StoreWrapper();
        storeWrapper.wrapped = map;
        return storeWrapper;
    }

    public void invalidateCacheForThread() {
        mapThreadLocalObjectReference.set(Collections.unmodifiableMap(Collections.emptyMap()));
    }

    public int getCacheSize() {
        return ((Map)mapThreadLocalObjectReference.get()).size();
    }

    public static class StoreThreadLocal<T> extends ThreadLocal<Map<Object, T>> implements Serializable {
        protected Map<Object, T> initialValue() {
            return new HashMap<Object, T>();
        }
    }

    public static class StoreWrapper implements Serializable {
        private Map wrapped;
    }

    @SuppressWarnings("serial")
    public static class Stored<T> extends AbstractChangedBehavior<T> {

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
}