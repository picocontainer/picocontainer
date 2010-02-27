/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.gems.containers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Converters;
import org.picocontainer.Converting;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;
import org.picocontainer.converters.ConvertsNothing;
import org.picocontainer.lifecycle.LifecycleState;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/** @author Michael Rimov 
 * @deprecated As of PicoContainer 2.3  ComponentMonitor now can do all jobs of tracing container.
 */
@Deprecated
@SuppressWarnings("serial")
public class CommonsLoggingTracingContainerDecorator implements MutablePicoContainer, Converting, Serializable {


	/** Wrapped container. */
    private final MutablePicoContainer delegate;

    /** Logger instance used for writing events. */
    private transient Log log;

    /** Serialized log category. */
    private final String logCategory;

    /**
     * Default typical wrapper that wraps another MutablePicoContainer.
     *
     * @param delegate Container to be decorated.
     *
     * @throws NullPointerException if delegate is null.
     */
    public CommonsLoggingTracingContainerDecorator(final MutablePicoContainer delegate) {
        this(delegate, PicoContainer.class.getName());
    }


    /**
     * Alternate constructor that allows specification of the Logger to
     * use.
     *
     * @param delegate        Container to be decorated.
     * @param loggingCategory specific Log4j Logger to use.
     *
     * @throws NullPointerException if delegate or log is null.
     */
    public CommonsLoggingTracingContainerDecorator(final MutablePicoContainer delegate, final String loggingCategory) {
        if (delegate == null) {
            throw new NullPointerException("delegate");
        }

        if (loggingCategory == null) {
            throw new NullPointerException("loggingCategory");
        }

        log = LogFactory.getLog(loggingCategory);

        this.delegate = delegate;
        logCategory = loggingCategory;
    }


    /**
     * Standard message handling for cases when a null object is returned
     * for a given key.
     *
     * @param componentKey Component key that does not exist
     * @param target Logger to log into
     */
    protected void onKeyOrTypeDoesNotExistInContainer(final Object componentKey, final Log target) {
        log.info("Could not find component "
                 + (componentKey instanceof Class ? ((Class)componentKey).getName() : componentKey)
                 + " in container or parent container.");
    }

    /**
     * {@inheritDoc}
     *
     * @param visitor
     *
     * @see org.picocontainer.PicoContainer#accept(org.picocontainer.PicoVisitor)
     */
    public void accept(final PicoVisitor visitor) {
        if (log.isDebugEnabled()) {
            log.debug("Visiting Container " + delegate
                      + " with visitor " + visitor);
        }
        delegate.accept(visitor);
    }

    /**
     * {@inheritDoc}
     *
     * @param child
     *
     * @return
     *
     * @see org.picocontainer.MutablePicoContainer#addChildContainer(org.picocontainer.PicoContainer)
     */
    public MutablePicoContainer addChildContainer(final PicoContainer child) {
        if (log.isDebugEnabled()) {
            log.debug("Adding child container: " + child + " to container " + delegate);
        }
        return delegate.addChildContainer(child);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.picocontainer.Disposable#dispose()
     */
    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("Disposing container " + delegate);
        }
        delegate.dispose();
    }

    /**
     * {@inheritDoc}
     *
     * @param componentKey
     *
     * @return
     *
     * @see org.picocontainer.PicoContainer#getComponentAdapter(java.lang.Object)
     */
    public ComponentAdapter<?> getComponentAdapter(final Object componentKey) {
        if (log.isDebugEnabled()) {
            log.debug("Locating component adapter with key " + componentKey);
        }

        ComponentAdapter adapter = delegate.getComponentAdapter(componentKey);
        if (adapter == null) {
            onKeyOrTypeDoesNotExistInContainer(componentKey, log);
        }
        return adapter;
    }

    /**
     * {@inheritDoc}
     *
     * @param componentType
     * @return ComponentAdapter or null.
     * @see org.picocontainer.PicoContainer#getComponentAdapter(java.lang.Class, NameBinding)
     */

    public <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final NameBinding componentNameBinding) {
        if (log.isDebugEnabled()) {
            log.debug("Locating component adapter with type " + componentType);
        }

        ComponentAdapter<T> ca = delegate.getComponentAdapter(componentType, componentNameBinding);

        if (ca == null) {
            onKeyOrTypeDoesNotExistInContainer(ca, log);
        }
        return ca;
    }

    /**
     * {@inheritDoc}
     *
     * @return Collection or null.
     *
     * @see org.picocontainer.PicoContainer#getComponentAdapters()
     */
    public Collection<ComponentAdapter<?>> getComponentAdapters() {
        if (log.isDebugEnabled()) {
            log.debug("Grabbing all component adapters for container: " + delegate);
        }
        return delegate.getComponentAdapters();
    }

    /**
     * {@inheritDoc}
     *
     * @param componentType
     *
     * @return List of ComponentAdapters
     *
     * @see org.picocontainer.PicoContainer#getComponentAdapters(java.lang.Class)
     */
    public <T>List<ComponentAdapter<T>> getComponentAdapters(final Class<T> componentType) {
        if (log.isDebugEnabled()) {
            log.debug("Grabbing all component adapters for container: "
                      + delegate + " of type: " + componentType.getName());
        }
        return delegate.getComponentAdapters(componentType);
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Class<T> componentType, final Class<? extends Annotation> binding) {
        if (log.isDebugEnabled()) {
            log.debug("Grabbing all component adapters for container: "
                      + delegate + " of type: " + componentType.getName() + ", binding:" + binding.getName());
        }
        return delegate.getComponentAdapters(componentType, binding);
    }

    public <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final Class<? extends Annotation> binding) {
        if (log.isDebugEnabled()) {
            log.debug("Grabbing component adapter for container: "
                      + delegate + " of type: " + componentType.getName() + ", binding:" + binding.getName());
        }
        return delegate.getComponentAdapter(componentType, binding);
    }

    /**
     * {@inheritDoc}
     *
     * @param componentKeyOrType
     *
     * @return
     *
     * @see org.picocontainer.PicoContainer#getComponent(java.lang.Object)
     */
    public Object getComponent(final Object componentKeyOrType) {

        if (log.isDebugEnabled()) {
            log.debug("Attempting to load component instance with "
                      + (componentKeyOrType instanceof Class ? "type" : "key")
                      + ": " + componentKeyOrType + " for container " + delegate);

        }

        Object result = delegate.getComponent(componentKeyOrType);
        if (result == null) {
            onKeyOrTypeDoesNotExistInContainer(componentKeyOrType, log);
        }

        return result;
    }

    public Object getComponent(final Object componentKeyOrType, final Type into) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to load component instance with "
                      + (componentKeyOrType instanceof Class ? "type" : "key")
                      + ": " + componentKeyOrType + " for container " + delegate);

        }

        Object result = delegate.getComponent(componentKeyOrType, into);
        if (result == null) {
            onKeyOrTypeDoesNotExistInContainer(componentKeyOrType, log);
        }

        return result;
    }

    public <T> T getComponent(final Class<T> componentType) {
        return componentType.cast(getComponent((Object)componentType));
    }

    public <T> T getComponent(final Class<T> componentType, final Class<? extends Annotation> binding) {
        if (log.isDebugEnabled()) {
            log.debug("Grabbing component for container: "
                      + delegate + " of type: " + componentType.getName() + ", binding:" + binding.getName());
        }
        return delegate.getComponent(componentType, binding);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     *
     * @see org.picocontainer.PicoContainer#getComponents()
     */
    public List getComponents() {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving all component instances for container "
                      + delegate);
        }
        return delegate.getComponents();
    }

    /**
     * {@inheritDoc}
     *
     * @param componentType
     *
     * @return
     *
     * @see org.picocontainer.PicoContainer#getComponents(java.lang.Class)
     */
    public <T> List<T> getComponents(final Class<T> componentType) {
        if (log.isDebugEnabled()) {
            log.debug("Loading all component instances of type " + componentType
                      + " for container " + delegate);
        }
        List<T> result = delegate.getComponents(componentType);
        if (result == null || result.isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info("Could not find any components  "
                         + " in container or parent container.");
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     *
     * @see org.picocontainer.PicoContainer#getParent()
     */
    public PicoContainer getParent() {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving the parent for container " + delegate);
        }

        return delegate.getParent();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     *
     * @see org.picocontainer.MutablePicoContainer#makeChildContainer()
     */
    public MutablePicoContainer makeChildContainer() {
        if (log.isDebugEnabled()) {
            log.debug("Making child container for container " + delegate);
        }

        //Wrap the new delegate
        return new Log4jTracingContainerDecorator(delegate.makeChildContainer());
    }

    /**
     * {@inheritDoc}
     *
     * @param componentAdapter
     *
     * @return
     *
     * @see org.picocontainer.MutablePicoContainer#addAdapter(org.picocontainer.ComponentAdapter)
     */
    public MutablePicoContainer addAdapter(final ComponentAdapter componentAdapter) {
        if (log.isDebugEnabled()) {
            log.debug("Registering component adapter " + componentAdapter);
        }

        return delegate.addAdapter(componentAdapter);
    }

    /**
     * {@inheritDoc}
     *
     * @param componentKey
     * @param componentImplementationOrInstance
     *
     * @param parameters
     *
     * @return
     */
    public MutablePicoContainer addComponent(final Object componentKey, final Object componentImplementationOrInstance,
                                             final Parameter... parameters)
    {
        if (log.isDebugEnabled()) {
            log.debug("Registering component "
                      + (componentImplementationOrInstance instanceof Class ? "implementation" : "instance")
                      + " with key "
                      + componentKey
                      + " and implementation "
                      + (componentImplementationOrInstance instanceof Class
                         ? ((Class)componentImplementationOrInstance).getCanonicalName()
                         : componentImplementationOrInstance.getClass())
                      + " using parameters "
                      + parameters);
        }

        return delegate.addComponent(componentKey, componentImplementationOrInstance, parameters);
    }

    /**
     * {@inheritDoc}
     *
     * @param implOrInstance
     *
     * @return
     *
     * @see org.picocontainer.MutablePicoContainer#addComponent(java.lang.Object)
     */
    public MutablePicoContainer addComponent(final Object implOrInstance) {
        if (log.isDebugEnabled()) {
            log.debug("Registering component impl or instance "
                      + implOrInstance + "(class: "
                      + ((implOrInstance != null) ? implOrInstance.getClass().getName() : " null "));
        }

        return delegate.addComponent(implOrInstance);
    }

    public MutablePicoContainer addConfig(final String name, final Object val) {
        if (log.isDebugEnabled()) {
            log.debug("Registering config: " + name);
        }

        return delegate.addConfig(name, val);
    }

    /**
     * {@inheritDoc}
     *
     * @param child
     *
     * @return
     *
     * @see org.picocontainer.MutablePicoContainer#removeChildContainer(org.picocontainer.PicoContainer)
     */
    public boolean removeChildContainer(final PicoContainer child) {
        if (log.isDebugEnabled()) {
            log.debug("Removing child container: " + child
                      + " from parent: " + delegate);
        }
        return delegate.removeChildContainer(child);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.picocontainer.Startable#start()
     */
    public void start() {
        if (log.isInfoEnabled()) {
            log.info("Starting Container " + delegate);
        }

        delegate.start();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.picocontainer.Startable#stop()
     */
    public void stop() {
        if (log.isInfoEnabled()) {
            log.info("Stopping Container " + delegate);
        }
        delegate.stop();
    }

    /**
     * {@inheritDoc}
     *
     * @param componentKey
     *
     * @return
     *
     * @see org.picocontainer.MutablePicoContainer#removeComponent(java.lang.Object)
     */
    public ComponentAdapter removeComponent(final Object componentKey) {
        if (log.isDebugEnabled()) {
            log.debug("Unregistering component " + componentKey + " from container " + delegate);
        }

        return delegate.removeComponent(componentKey);
    }

    /**
     * {@inheritDoc}
     *
     * @param componentInstance
     *
     * @return
     *
     * @see org.picocontainer.MutablePicoContainer#removeComponentByInstance(java.lang.Object)
     */
    public ComponentAdapter removeComponentByInstance(final Object componentInstance) {
        if (log.isDebugEnabled()) {
            log.debug("Unregistering component by instance (" + componentInstance + ") from container " + delegate);
        }

        return delegate.removeComponentByInstance(componentInstance);
    }


    /**
     * Retrieves the log instance used by this decorator.
     *
     * @return Logger instance.
     */
    public Log getLoggerUsed() {
        return this.log;
    }

    private void readObject(final java.io.ObjectInputStream s)
        throws java.io.IOException, java.lang.ClassNotFoundException {
	        s.defaultReadObject();                                    
	        log = LogFactory.getLog(this.logCategory);
	}

    public MutablePicoContainer change(final Properties... properties) {
        return delegate.change(properties);
    }

    public MutablePicoContainer as(final Properties... properties) {
        return delegate.as(properties);
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public void setLifecycleState(LifecycleState lifecycleState) {
        delegate.setLifecycleState(lifecycleState);
    }

    /**
     * {@inheritDoc} 
     */
    public Converters getConverters() {
        if (delegate instanceof Converting) {
            return ((Converting) delegate).getConverters();
        }
        return new ConvertsNothing();
    }
}
