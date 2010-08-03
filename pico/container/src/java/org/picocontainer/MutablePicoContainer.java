/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by various                           *
 *****************************************************************************/
package org.picocontainer;

import org.picocontainer.injectors.Provider;
import org.picocontainer.lifecycle.LifecycleState;

import java.lang.annotation.Annotation;
import java.util.Properties;

/**
 * This is the core interface used for registration of components with a container. It is possible to register
 * implementations and instances here
 *
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Jon Tirs&eacute;n
 * @see <a href="package-summary.html#package_description">See package description for basic overview how to use PicoContainer.</a>
 */
public interface MutablePicoContainer extends PicoContainer, Startable, Disposable {

    public static interface BindTo<T> {
        MutablePicoContainer to(Class<? extends T> impl);
        MutablePicoContainer to(T instance);
        MutablePicoContainer toProvider(javax.inject.Provider<? extends T> provider);
        MutablePicoContainer toProvider(Provider provider);
    }

    public static interface BindWithOrTo<T> extends BindTo<T> {
        <T> BindTo<T> withAnnotation(Class<? extends Annotation> annotation);
        <T> BindTo<T> named(String name);
    }



    <T> BindWithOrTo<T> bind(Class<T> type);

    /**
     * Register a component and creates specific instructions on which constructor to use, along with
     * which components and/or constants to provide as constructor arguments.  These &quot;directives&quot; are
     * provided through an array of <tt>Parameter</tt> objects.  Parameter[0] correspondes to the first constructor
     * argument, Parameter[N] corresponds to the  N+1th constructor argument.
     * <h4>Tips for Parameter usage</h4>
     * <ul>
     * <li><strong>Partial Autowiring: </strong>If you have two constructor args to match and you only wish to specify one of the constructors and
     * let PicoContainer wire the other one, you can use as parameters:
     * <code><strong>new ComponentParameter()</strong>, new ComponentParameter("someService")</code>
     * The default constructor for the component parameter indicates auto-wiring should take place for
     * that parameter.
     * </li>
     * <li><strong>Force No-Arg constructor usage:</strong> If you wish to force a component to be constructed with
     * the no-arg constructor, use a zero length Parameter array.  Ex:  <code>new Parameter[0]</code>
     * <ul>
     *
     * @param key a key that identifies the component. Must be unique within the container. The type
     *                     of the key object has no semantic significance unless explicitly specified in the
     *                     documentation of the implementing container.
     * @param implOrInstance
     *                     the component's implementation class. This must be a concrete class (ie, a
     *                     class that can be instantiated). Or an intance of the compoent.
     * @param parameters   the parameters that gives the container hints about what arguments to pass
     *                     to the constructor when it is instantiated. Container implementations may ignore
     *                     one or more of these hints.
     *
     * @return the same instance of MutablePicoContainer
     *
     * @throws PicoCompositionException if registration of the component fails.
     * @see org.picocontainer.Parameter
     * @see org.picocontainer.parameters.ConstantParameter
     * @see org.picocontainer.parameters.ComponentParameter
     */
    MutablePicoContainer addComponent(Object key,
                                      Object implOrInstance,
                                      Parameter... parameters);

    /**
     * Register an arbitrary object. The class of the object will be used as a key. Calling this method is equivalent to
     * calling  <code>addComponent(impl, impl)</code>.
     *
     * @param implOrInstance Component implementation or instance
     *
     * @return the same instance of MutablePicoContainer
     *
     * @throws PicoCompositionException if registration fails.
     */
    MutablePicoContainer addComponent(Object implOrInstance);

    /**
     * Register a config item.
     *
     * @param name the name of the config item
     * @param val the value of the config item
     *
     * @return the same instance of MutablePicoContainer
     *
     * @throws PicoCompositionException if registration fails.
     */
    MutablePicoContainer addConfig(String name, Object val);

    /**
     * Register a component via a ComponentAdapter. Use this if you need fine grained control over what
     * ComponentAdapter to use for a specific component.  The adapter will be wrapped in whatever behaviors that the 
     * the container has been set up with.  If you want to bypass that behavior for the adapter you are adding, 
     * you should use Characteristics.NONE like so pico.as(Characteristics.NONE).addAdapter(...)
     *
     * @param componentAdapter the adapter
     *
     * @return the same instance of MutablePicoContainer
     *
     * @throws PicoCompositionException if registration fails.
     */
    MutablePicoContainer addAdapter(ComponentAdapter<?> componentAdapter);

    MutablePicoContainer addProvider(javax.inject.Provider<?> provider);

    /**
     * Unregister a component by key.
     *
     * @param key key of the component to unregister.
     *
     * @return the ComponentAdapter that was associated with this component.
     */
    <T> ComponentAdapter<T> removeComponent(Object key);

    /**
     * Unregister a component by instance.
     *
     * @param componentInstance the component instance to unregister.
     *
     * @return the same instance of MutablePicoContainer
     */
    <T> ComponentAdapter<T> removeComponentByInstance(T componentInstance);

    /**
     * Make a child container, using both the same implementation of MutablePicoContainer as the parent
     * and identical behaviors as well.
     * It will have a reference to this as parent.  This will list the resulting MPC as a child.
     * Lifecycle events will be cascaded from parent to child
     * as a consequence of this.  
     * <p>Note that for long-lived parent containers, you need to unregister child containers
     * made with this call before disposing or you will leak memory.  <em>(Experience
     * speaking here! )</em></p>
     * <p>Incorrect Example:</p>
     * <pre>
     *   MutablePicoContainer parent = new PicoBuilder().withCaching().withLifecycle().build();
     *   MutablePicoContainer child = parent.makeChildContainer();
     *   child = null; //Child still retains in memory because parent still holds reference.
     * </pre>
     * <p>Correct Example:</p>
     * <pre>
     *   MutablePicoContainer parent = new PicoBuilder().withCaching().withLifecycle().build();
     *   MutablePicoContainer child = parent.makeChildContainer();
     *   parent.removeChildContainer(child); //Remove the bi-directional references.
     *   child = null; 
     * </pre>
     * @return the new child container.
     */
    MutablePicoContainer makeChildContainer();

    /**
     * Add a child container. This action will list the the 'child' as exactly that in the parents scope.
     * It will not change the child's view of a parent.  That is determined by the constructor arguments of the child
     * itself. Lifecycle events will be cascaded from parent to child
     * as a consequence of calling this method.
     *
     * @param child the child container
     *
     * @return the same instance of MutablePicoContainer
     *
     */
    MutablePicoContainer addChildContainer(PicoContainer child);

    /**
     * Remove a child container from this container. It will not change the child's view of a parent.
     * Lifecycle event will no longer be cascaded from the parent to the child.
     *
     * @param child the child container
     *
     * @return <code>true</code> if the child container has been removed.
     *
     */
    boolean removeChildContainer(PicoContainer child);


    /**
     * You can change the characteristic of registration of all subsequent components in this container.
     *
     * @param properties
     * @return the same Pico instance with changed properties
     */
    MutablePicoContainer change(Properties... properties);

    /**
     * You can set for the following operation only the characteristic of registration of a component on the fly.
     *
     * @param properties
     * @return the same Pico instance with temporary properties
     */
    MutablePicoContainer as(Properties... properties);

    /**
     * Name the container instance, to assit debugging.
     *
     * @param name the name to call it.
     * @since 2.8
     */
    void setName(String name);

    /**
     * To assist ThreadLocal usage, LifecycleState can be set.  No need to use this for normal usages.
     * @param lifecycleState the lifecyle state to use.
     * @since 2.8
     */
    void setLifecycleState(LifecycleState lifecycleState);
    
    /**
     * Retrieve the name set (if any).
     * @return Retrieve the arbitrary name of the container set by calling {@link #setName(String) setName}.
     * @since 2.10.2
     */
    String getName();
    
    
    /**
     * Allow querying of the current lifecycle state of a MutablePicoContainer.
     * @return the current Lifecycle State.
     * @since 2.10.2
     */
    LifecycleState getLifecycleState();

    /**
     * Changes monitor in the ComponentFactory, the component adapters
     * and the child containers, if these support a ComponentMonitorStrategy.
     * {@inheritDoc}
     * @since 3.0
     */
    void changeMonitor(final ComponentMonitor monitor);

    
}
