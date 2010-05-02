/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved. *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD * style
 * license a copy of which has been included with this distribution in * the
 * LICENSE.txt file. * * Original code by *
 ******************************************************************************/
package org.picocontainer.behaviors;

import org.picocontainer.Behavior;
import org.picocontainer.ChangedBehavior;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.ComponentMonitorStrategy;
import org.picocontainer.InjectionType;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;
import org.picocontainer.injectors.AdaptingInjection;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Enumeration;
import java.util.Properties;

@SuppressWarnings("serial")
public class AbstractBehavior implements ComponentFactory, Serializable, Behavior {

    private ComponentFactory delegate;

    public ComponentFactory wrap(ComponentFactory delegate) {
        this.delegate = delegate;
        return this;
    }

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor componentMonitor,
            LifecycleStrategy lifecycleStrategy, Properties componentProperties, Object key,
            Class<T> componentImplementation, Parameter... parameters) throws PicoCompositionException {
        if (delegate == null) {
            delegate = new AdaptingInjection();
        }
        ComponentAdapter<T> compAdapter = delegate.createComponentAdapter(componentMonitor, lifecycleStrategy, componentProperties, key,
                componentImplementation, parameters);

        boolean enableCircular = removePropertiesIfPresent(componentProperties, Characteristics.ENABLE_CIRCULAR);
        if (enableCircular && delegate instanceof InjectionType) {
            return componentMonitor.newBehavior(new ImplementationHiding.HiddenImplementation(compAdapter));
        } else {
            return compAdapter;
        }
    }

    public void verify(PicoContainer container) {
        delegate.verify(container);
    }

    public void accept(PicoVisitor visitor) {
        visitor.visitComponentFactory(this);
        if (delegate != null) {
            delegate.accept(visitor);
        }
    }


    public <T> ComponentAdapter<T> addComponentAdapter(ComponentMonitor componentMonitor,
            LifecycleStrategy lifecycleStrategy, Properties componentProperties, ComponentAdapter<T> adapter) {
        if (delegate != null && delegate instanceof Behavior) {
            return ((Behavior) delegate).addComponentAdapter(componentMonitor, lifecycleStrategy,
                    componentProperties, adapter);
        }
        return adapter;
    }

    public static boolean arePropertiesPresent(Properties current, Properties present, boolean compareValueToo) {
        Enumeration<?> keys = present.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String presentValue = present.getProperty(key);
            String currentValue = current.getProperty(key);
            if (currentValue == null) {
                return false;
            }
            if (!presentValue.equals(currentValue) && compareValueToo) {
                return false;
            }
        }
        return true;
    }

    public static boolean removePropertiesIfPresent(Properties current, Properties present) {
        if (!arePropertiesPresent(current, present, true)) {
            return false;
        }
        Enumeration<?> keys = present.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            current.remove(key);
        }
        return true;
    }

    public static String getAndRemovePropertiesIfPresentByKey(Properties current, Properties present) {
        if (!arePropertiesPresent(current, present, false)) {
            return null;
        }
        Enumeration<?> keys = present.keys();
        String value = null;
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            value = (String) current.remove(key);
        }
        return value;
    }

    protected void mergeProperties(Properties into, Properties from) {
        Enumeration<?> e = from.propertyNames();
        while (e.hasMoreElements()) {
            String s = (String) e.nextElement();
            into.setProperty(s, from.getProperty(s));
        }

    }

    /**
     * <p>
     * Component adapter which decorates another adapter.
     * </p>
     * <p>
     * This adapter supports a {@link org.picocontainer.ComponentMonitorStrategy component monitor strategy}
     * and will propagate change of monitor to the delegate if the delegate itself
     * support the monitor strategy.
     * </p>
     * <p>
     * This adapter also supports a {@link Behavior lifecycle manager} and a
     * {@link org.picocontainer.LifecycleStrategy lifecycle strategy} if the delegate does.
     * </p>
     *
     * @author Jon Tirsen
     * @author Aslak Hellesoy
     * @author Mauro Talevi
     */
    public abstract static class AbstractChangedBehavior<T> implements ChangedBehavior<T>, ComponentMonitorStrategy,
                                                      LifecycleStrategy, Serializable {

        protected final ComponentAdapter<T> delegate;

        public AbstractChangedBehavior(ComponentAdapter<T> delegate) {
            this.delegate = delegate;
        }

        public Object getComponentKey() {
            return delegate.getComponentKey();
        }

        public Class<? extends T> getComponentImplementation() {
            return delegate.getComponentImplementation();
        }

        public T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            return (T) delegate.getComponentInstance(container, into);
        }

        public void verify(PicoContainer container) throws PicoCompositionException {
            delegate.verify(container);
        }

        public final ComponentAdapter<T> getDelegate() {
            return delegate;
        }

        @SuppressWarnings("unchecked")
        public final <U extends ComponentAdapter> U findAdapterOfType(Class<U> adapterType) {
            if (adapterType.isAssignableFrom(this.getClass())) {
                return (U) this;
            } else {
                return delegate.findAdapterOfType(adapterType);
            }
        }

        public void accept(PicoVisitor visitor) {
            visitor.visitComponentAdapter(this);
            delegate.accept(visitor);
        }

        /**
         * Delegates change of monitor if the delegate supports
         * a component monitor strategy.
         * {@inheritDoc}
         */
        public void changeMonitor(ComponentMonitor monitor) {
            if (delegate instanceof ComponentMonitorStrategy ) {
                ((ComponentMonitorStrategy)delegate).changeMonitor(monitor);
            }
        }

        /**
         * Returns delegate's current monitor if the delegate supports
         * a component monitor strategy.
         * {@inheritDoc}
         * @throws org.picocontainer.PicoCompositionException if no component monitor is found in delegate
         */
        public ComponentMonitor currentMonitor() {
            if (delegate instanceof ComponentMonitorStrategy ) {
                return ((ComponentMonitorStrategy)delegate).currentMonitor();
            }
            throw new PicoCompositionException("No component monitor found in delegate");
        }

        /**
         * Invokes delegate start method if the delegate is a Behavior
         * {@inheritDoc}
         */
        public void start(PicoContainer container) {
            if (delegate instanceof ChangedBehavior) {
                ((ChangedBehavior<?>)delegate).start(container);
            }
        }

        /**
         * Invokes delegate stop method if the delegate is a Behavior
         * {@inheritDoc}
         */
        public void stop(PicoContainer container) {
            if (delegate instanceof ChangedBehavior) {
                ((ChangedBehavior<?>)delegate).stop(container);
            }
        }

        /**
         * Invokes delegate dispose method if the delegate is a Behavior
         * {@inheritDoc}
         */
        public void dispose(PicoContainer container) {
            if (delegate instanceof ChangedBehavior) {
                ((ChangedBehavior<?>)delegate).dispose(container);
            }
        }

        /**
         * Invokes delegate hasLifecycle method if the delegate is a Behavior
         * {@inheritDoc}
         */
        public boolean componentHasLifecycle() {
            if (delegate instanceof ChangedBehavior) {
                return ((ChangedBehavior<?>)delegate).componentHasLifecycle();
            }
            return false;
        }

        public boolean isStarted() {
            if (delegate instanceof ChangedBehavior) {
                return ((ChangedBehavior<?>)delegate).isStarted();
            }
            return false;
        }

    // ~~~~~~~~ LifecycleStrategy ~~~~~~~~

        /**
         * Invokes delegate start method if the delegate is a LifecycleStrategy
         * {@inheritDoc}
         */
        public void start(Object component) {
            if (delegate instanceof LifecycleStrategy ) {
                ((LifecycleStrategy)delegate).start(component);
            }
        }

        /**
         * Invokes delegate stop method if the delegate is a LifecycleStrategy
         * {@inheritDoc}
         */
        public void stop(Object component) {
            if (delegate instanceof LifecycleStrategy ) {
                ((LifecycleStrategy)delegate).stop(component);
            }
        }

        /**
         * Invokes delegate dispose method if the delegate is a LifecycleStrategy
         * {@inheritDoc}
         */
        public void dispose(Object component) {
            if (delegate instanceof LifecycleStrategy ) {
                ((LifecycleStrategy)delegate).dispose(component);
            }
        }

        /**
         * Invokes delegate hasLifecycle(Class) method if the delegate is a LifecycleStrategy
         * {@inheritDoc}
         */
        public boolean hasLifecycle(Class<?> type) {
            return delegate instanceof LifecycleStrategy && ((LifecycleStrategy) delegate).hasLifecycle(type);
        }

        public boolean isLazy(ComponentAdapter<?> adapter) {
            return delegate instanceof LifecycleStrategy && ((LifecycleStrategy) delegate).isLazy(adapter);
        }

        public String toString() {
            return getDescriptor() + ":" + delegate.toString();
        }
    }
}
