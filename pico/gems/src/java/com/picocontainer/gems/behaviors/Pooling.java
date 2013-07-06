/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.gems.behaviors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.picocontainer.gems.GemsCharacteristics;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.behaviors.AbstractBehavior;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;
import com.thoughtworks.proxy.ProxyFactory;
import com.thoughtworks.proxy.factory.StandardProxyFactory;
import com.thoughtworks.proxy.kit.NoOperationResetter;
import com.thoughtworks.proxy.kit.Resetter;
import com.thoughtworks.proxy.toys.nullobject.Null;
import com.thoughtworks.proxy.toys.pool.Pool;
import com.thoughtworks.proxy.toys.pool.SerializationMode;

@SuppressWarnings("serial")
public class Pooling extends AbstractBehavior {

 	private final Pooled.Context<?> poolContext;

    public Pooling(final Pooled.Context<?> poolContext) {
        this.poolContext = poolContext;
    }

    public Pooling() {
        poolContext = new Pooled.DefaultContext<Object>();
    }

    @Override
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final Properties componentProps, final Object key, final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams)
            throws PicoCompositionException {
        ComponentAdapter<T> delegate = super.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams);

        if (AbstractBehavior.removePropertiesIfPresent(componentProps, GemsCharacteristics.NO_POOL)) {
        	return delegate;
		}

        AbstractBehavior.removePropertiesIfPresent(componentProps, GemsCharacteristics.POOL);
        Pooled<T> behavior = new Pooled<T>(delegate, poolContext);
        //TODO
        //Characteristics.HIDE.setProcessedIn(componentCharacteristics);
        return monitor.changedBehavior(behavior);
    }

    @Override
	public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
                                                final Properties componentProps, final ComponentAdapter<T> adapter) {

        if (AbstractBehavior.removePropertiesIfPresent(componentProps, GemsCharacteristics.NO_POOL)) {
        	return super.addComponentAdapter(monitor, lifecycle, componentProps, adapter);
		}

        AbstractBehavior.removePropertiesIfPresent(componentProps, GemsCharacteristics.POOL);
    	return monitor.changedBehavior(new Pooled<T>(super.addComponentAdapter(monitor, lifecycle, componentProps, adapter), poolContext));
    }

    /**
     * {@link com.picocontainer.ComponentAdapter} implementation that pools components.
     * <p>
     * The implementation utilizes a delegated ComponentAdapter to create the instances of the pool. The
     * pool can be configured to grow unlimited or to a maximum size. If a component is requested from
     * this adapter, the implementation returns an available instance from the pool or will create a
     * new one, if the maximum pool size is not reached yet. If none is available, the implementation
     * can wait a defined time for a returned object before it throws a {@link com.picocontainer.gems.behaviors.Pooling.Pooled.PoolException}.
     * </p>
     * <p>
     * This implementation uses the {@link com.thoughtworks.proxy.toys.pool.Pool} toy from the <a
     * href="http://proxytoys.codehaus.org">ProxyToys</a> project. This ensures, that any component,
     * that is out of scope will be automatically returned to the pool by the garbage collector.
     * Additionally will every component instance also implement
     * {@link com.thoughtworks.proxy.toys.pool.Poolable}, that can be used to return the instance
     * manually. After returning an instance it should not be used in client code anymore.
     * </p>
     * <p>
     * Before a returning object is added to the available instances of the pool again, it should be
     * reinitialized to a normalized state. By providing a proper Resetter implementation this can be
     * done automatically. If the object cannot be reused anymore it can also be dropped and the pool
     * may request a new instance.
     * </p>
     * <p>
     * The pool supports components with a lifecycle. If the delegated {@link com.picocontainer.ComponentAdapter}
     * implements a {@link com.picocontainer.LifecycleStrategy}, any component retrieved form the pool will be started
     * before and stopped again, when it returns back into the pool. Also if a component cannot be
     * resetted it will automatically be disposed. If the container of the pool is disposed, that any
     * returning object is also disposed and will not return to the pool anymore. Note, that current
     * implementation cannot dispose pooled objects.
     * </p>
     *
     * @author J&ouml;rg Schaible
     * @author Aslak Helles&oslash;y
     */
    public static final class Pooled<T> extends AbstractChangedBehavior<T> {

        /**
         * Context of the Pooled used to initialize it.
         *
         * @author J&ouml;rg Schaible
         */
        public static interface Context<T> {
            /**
             * Retrieve the maximum size of the pool. An implementation may return the maximum value or
             * {@link com.picocontainer.gems.behaviors.Pooling.Pooled#UNLIMITED_SIZE} for <em>unlimited</em> growth.
             *
             * @return the maximum pool size
             */
            int getMaxSize();

            /**
             * Retrieve the maximum number of milliseconds to wait for a returned element. An
             * implementation may return alternatively {@link com.picocontainer.gems.behaviors.Pooling.Pooled#BLOCK_ON_WAIT} or
             * {@link com.picocontainer.gems.behaviors.Pooling.Pooled#FAIL_ON_WAIT}.
             *
             * @return the maximum number of milliseconds to wait
             */
            int getMaxWaitInMilliseconds();

            /**
             * Allow the implementation to invoke the garbace collector manually if the pool is
             * exhausted.
             *
             * @return <code>true</code> for an internal call to {@link System#gc()}
             */
            boolean autostartGC();

            /**
             * Retrieve the ProxyFactory to use to create the pooling proxies.
             *
             * @return the {@link com.thoughtworks.proxy.ProxyFactory}
             */
            ProxyFactory getProxyFactory();

            /**
             * Retrieve the {@link com.thoughtworks.proxy.kit.Resetter} of the objects returning to the pool.
             *
             * @return the Resetter instance
             */
            Resetter<T> getResetter();

            /**
             * Retrieve the serialization mode of the pool. Following values are possible:
             * <ul>
             * <li>{@link com.thoughtworks.proxy.toys.pool.SerializationMode#STANDARD}</li>
             * <li>{@link com.thoughtworks.proxy.toys.pool.SerializationMode#NONE}</li>
             * <li>{@link com.thoughtworks.proxy.toys.pool.SerializationMode#FORCE}</li>
             * </ul>
             *
             * @return the serialization mode
             */
            SerializationMode getSerializationMode();
        }

        /**
         * The default context for a Pooled.
         *
         * @author J&ouml;rg Schaible
         */
        public static class DefaultContext<T> implements Context<T> {

            /**
             * {@inheritDoc} Returns {@link com.picocontainer.gems.behaviors.Pooling.Pooled#DEFAULT_MAX_SIZE}.
             */
            public int getMaxSize() {
                return DEFAULT_MAX_SIZE;
            }

            /**
             * {@inheritDoc} Returns {@link com.picocontainer.gems.behaviors.Pooling.Pooled#FAIL_ON_WAIT}.
             */
            public int getMaxWaitInMilliseconds() {
                return FAIL_ON_WAIT;
            }

            /**
             * {@inheritDoc} Returns <code>false</code>.
             */
            public boolean autostartGC() {
                return false;
            }

            /**
             * {@inheritDoc} Returns a {@link com.thoughtworks.proxy.factory.StandardProxyFactory}.
             */
            public ProxyFactory getProxyFactory() {
                return new StandardProxyFactory();
            }

            /**
             * {@inheritDoc} Returns the {@link com.picocontainer.gems.behaviors.Pooling.Pooled#DEFAULT_RESETTER}.
             */
            public Resetter<T> getResetter() {
                return DEFAULT_RESETTER;
            }

            /**
             * {@inheritDoc} Returns {@link com.thoughtworks.proxy.toys.pool.SerializationMode#STANDARD}.
             */
            public SerializationMode getSerializationMode() {
                return SerializationMode.STANDARD;
            }

        }

        /**
         * <code>UNLIMITED_SIZE</code> is the value to set the maximum size of the pool to unlimited ({@link Integer#MAX_VALUE}
         * in fact).
         */
        public static final int UNLIMITED_SIZE = Integer.MAX_VALUE;
        /**
         * <code>DEFAULT_MAX_SIZE</code> is the default size of the pool.
         */
        public static final int DEFAULT_MAX_SIZE = 8;
        /**
         * <code>BLOCK_ON_WAIT</code> forces the pool to wait until an object of the pool is returning
         * in case none is immediately available.
         */
        public static final int BLOCK_ON_WAIT = 0;
        /**
         * <code>FAIL_ON_WAIT</code> forces the pool to fail none is immediately available.
         */
        public static final int FAIL_ON_WAIT = -1;
        /**
         * <code>DEFAULT_RESETTER</code> is a {@link com.thoughtworks.proxy.kit.NoOperationResetter} that is used by default.
         */
        public static final Resetter DEFAULT_RESETTER = new NoOperationResetter();

        private int maxPoolSize;
        private int waitMilliSeconds;
        private Pool pool;
        private SerializationMode serializationMode;
        private boolean autostartGC;
        private boolean started;
        private boolean disposed;
        private boolean delegateHasLifecylce;
        private transient List<Object> components;

        /**
         * Construct a Pooled. Remember, that the implementation will request new
         * components from the delegate as long as no component instance is available in the pool and
         * the maximum pool size is not reached. Therefore the delegate may not return the same
         * component instance twice. Ensure, that the used {@link com.picocontainer.ComponentAdapter} does not cache.
         *
         * @param delegate the delegated ComponentAdapter
         * @param context the {@link com.picocontainer.gems.behaviors.Pooling.Pooled.Context} of the pool
         * @throws IllegalArgumentException if the maximum pool size or the serialization mode is
         *             invalid
         */
        public Pooled(final ComponentAdapter<T> delegate, final Context context) {
            super(delegate);
            this.maxPoolSize = context.getMaxSize();
            this.waitMilliSeconds = context.getMaxWaitInMilliseconds();
            this.autostartGC = context.autostartGC();
            this.serializationMode = context.getSerializationMode();
            if (maxPoolSize <= 0) {
                throw new IllegalArgumentException("Invalid maximum pool size");
            }
            started = false;
            disposed = false;
            delegateHasLifecylce = delegate instanceof LifecycleStrategy
                    && ((LifecycleStrategy)delegate)
                            .hasLifecycle(delegate.getComponentImplementation());
            components = new ArrayList<Object>();

            final Class type = delegate.getComponentKey() instanceof Class ? (Class)delegate
                    .getComponentKey() : delegate.getComponentImplementation();
            this.pool = Pool.create(type).resettedBy(makeResetted(context)).mode(serializationMode).build(context.getProxyFactory());

        }

        private Resetter<T> makeResetted(final Context context) {
            final Resetter<T> resetter = context.getResetter();
            return delegateHasLifecylce ? new LifecycleResetter<T>(this, resetter) : resetter;
        }

        /**
         * Construct an empty ComponentAdapter, used for serialization with reflection only.
         *
         */
        protected Pooled() {
            // TODO super class should support standard ctor
            super(Null.proxy(ComponentAdapter.class).build(new StandardProxyFactory()));
        }

        /**
         * {@inheritDoc}
         * <p>
         * As long as the maximum size of the pool is not reached and the pool is exhausted, the
         * implementation will request its delegate for a new instance, that will be managed by the
         * pool. Only if the maximum size of the pool is reached, the implementation may wait (depends
         * on the initializing {@link com.picocontainer.gems.behaviors.Pooling.Pooled.Context}) for a returning object.
         * </p>
         *
         * @throws com.picocontainer.gems.behaviors.Pooling.Pooled.PoolException if the pool is exhausted or waiting for a returning object timed out or
         *             was interrupted
         */
        @Override
        public T getComponentInstance(final PicoContainer container, final Type into) {
            if (delegateHasLifecylce) {
                if (disposed) {
					throw new IllegalStateException("Already disposed");
				}
            }
            T componentInstance;
            long now = System.currentTimeMillis();
            boolean gc = autostartGC;
            while (true) {
                synchronized (pool) {
                    componentInstance = (T) pool.get();
                    if (componentInstance != null) {
                        break;
                    }
                    if (maxPoolSize > pool.size()) {
                        final Object component = super.getComponentInstance(container, into);
                        if (delegateHasLifecylce) {
                            components.add(component);
                            if (started) {
                                start(component);
                            }
                        }
                        pool.add(component);
                    } else if (!gc) {
                        long after = System.currentTimeMillis();
                        if (waitMilliSeconds < 0) {
                            throw new PoolException("Pool exhausted");
                        }
                        if (waitMilliSeconds > 0 && after - now > waitMilliSeconds) {
                            throw new PoolException("Time out wating for returning object into pool");
                        }
                        try {
                            pool.wait(waitMilliSeconds); // Note, the pool notifies after an object
                                                            // was returned
                        } catch (InterruptedException e) {
                            // give the client code of the current thread a chance to abort also
                            Thread.currentThread().interrupt();
                            throw new PoolException(
                                    "Interrupted waiting for returning object into the pool", e);
                        }
                    } else {
                        System.gc();
                        gc = false;
                    }
                }
            }
            return componentInstance;
        }

        public String getDescriptor() {
            return "Pooled";
        }

        /**
         * Retrieve the current size of the pool. The returned value reflects the number of all managed
         * components.
         *
         * @return the number of components.
         */
        public int size() {
            return pool.size();
        }

        static final class LifecycleResetter<T> implements Resetter<T>, Serializable {
            private final Resetter<T> delegate;
            private final Pooled adapter;

            LifecycleResetter(final Pooled adapter, final Resetter<T> delegate) {
                this.adapter = adapter;
                this.delegate = delegate;
            }

            public boolean reset(final T object) {
                final boolean result = delegate.reset(object);
                if (!result || adapter.disposed) {
                    if (adapter.started) {
                        adapter.stop(object);
                    }
                    adapter.components.remove(object);
                    if (!adapter.disposed) {
                        adapter.dispose(object);
                    }
                }
                return result && !adapter.disposed;
            }

        }

        /**
         * Start of the container ensures that at least one pooled component has been started. Applies
         * only if the delegated {@link com.picocontainer.ComponentAdapter} supports a lifecylce by implementing
         * {@link com.picocontainer.LifecycleStrategy}.
         *
         * @throws IllegalStateException if pool was already disposed
         */
        @Override
        public void start(final PicoContainer container) {
            if (delegateHasLifecylce) {
                if (started) {
					throw new IllegalStateException("Already started");
				}
                if (disposed) {
					throw new IllegalStateException("Already disposed");
				}
                for (Object component : components) {
                    start(component);
                }
                started = true;
                if (pool.size() == 0) {
                    getComponentInstance(container, NOTHING.class);
                }
            }
        }

        /**
         * Stop of the container has no effect for the pool. Applies only if the delegated
         * {@link com.picocontainer.ComponentAdapter} supports a lifecylce by implementing {@link com.picocontainer.LifecycleStrategy}.
         *
         * @throws IllegalStateException if pool was already disposed
         */
        @Override
        public void stop(final PicoContainer container) {
            if (delegateHasLifecylce) {
                if (!started) {
					throw new IllegalStateException("Not started yet");
				}
                if (disposed) {
					throw new IllegalStateException("Already disposed");
				}
                for (Object component : components) {
                    stop(component);
                }
                started = false;
            }
        }

        /**
         * Dispose of the container will dispose all returning objects. They will not be added to the
         * pool anymore. Applies only if the delegated {@link com.picocontainer.ComponentAdapter} supports a lifecylce by
         * implementing {@link com.picocontainer.LifecycleStrategy}.
         *
         * @throws IllegalStateException if pool was already disposed
         */
        @Override
        public void dispose(final PicoContainer container) {
            if (delegateHasLifecylce) {
                if (started) {
					throw new IllegalStateException("Not stopped yet");
				}
                if (disposed) {
					throw new IllegalStateException("Already disposed");
				}
                disposed = true;
                for (Object component : components) {
                    dispose(component);
                }
                // TODO: Release pooled components and clear collection
            }
        }

        private synchronized void writeObject(final ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            SerializationMode mode = serializationMode;
            if (mode == SerializationMode.FORCE && components.size() > 0) {
                try {
                    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    final ObjectOutputStream testStream = new ObjectOutputStream(buffer);
                    testStream.writeObject(components); // force NotSerializableException
                    testStream.close();
                } catch (final NotSerializableException e) {
                    mode = SerializationMode.NONE;
                }
            }
            if (mode == SerializationMode.STANDARD) {
                out.writeObject(components);
            } else {
                out.writeObject(new ArrayList());
            }
        }

        private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            components = (List<Object>) in.readObject();
        }

        /**
         * Exception thrown from the Pooled. Only thrown if the interaction with the internal pool fails.
         *
         * @author J&ouml;rg Schaible
         */
        public static class PoolException extends PicoCompositionException {


            /**
             * Construct a PoolException with an explaining message and a originalting cause.
             *
             * @param message the explaining message
             * @param cause the originating cause
             */
            public PoolException(final String message, final Throwable cause) {
                super(message, cause);
            }

            /**
             * Construct a PoolException with an explaining message.
             *
             * @param message the explaining message
             */
            public PoolException(final String message) {
                super(message);
            }

        }

    }
}
