/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaibe                                            *
 *****************************************************************************/

package org.picocontainer.gems.behaviors;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Properties;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

import com.thoughtworks.proxy.ProxyFactory;
import com.thoughtworks.proxy.factory.StandardProxyFactory;
import com.thoughtworks.proxy.toys.delegate.Delegating;


/**
 * Factory for the Assimilated. This factory will create {@link Assimilated} instances for all
 * {@link ComponentAdapter} instances created by the delegate. This will assimilate every component for a specific type.
 * (TODO Since assimilating is taking types that are not the result type, does this mean that we cannot use generics)
 *
 * @author J&ouml;rg Schaible
 * for this type?  I've been unable to actually get it working.
 */
@SuppressWarnings("serial")
public class Assimilating extends AbstractBehavior {

	private final ProxyFactory proxyFactory;
    private final Class assimilationType;

    /**
     * Construct an Assimilating. The instance will use the {@link StandardProxyFactory} using the JDK
     * implementation.
     *
     * @param type The assimilated type.
     */
    public Assimilating(final Class type) {
        this(type, new StandardProxyFactory());
    }

    /**
     * Construct an Assimilating using a special {@link ProxyFactory}.
     *
     * @param type The assimilated type.
     * @param proxyFactory The proxy factory to use.
     */
    public Assimilating(final Class type, final ProxyFactory proxyFactory) {
        this.assimilationType = type;
        this.proxyFactory = proxyFactory;
    }

    /**
     * Create a {@link Assimilated}. This adapter will wrap the returned {@link ComponentAdapter} of the
     * deleated {@link ComponentFactory}.
     *
     * @see ComponentFactory#createComponentAdapter(ComponentMonitor,LifecycleStrategy,Properties,Object,Class,ConstructorParameters, FieldParameters[], MethodParameters[])
     */
	@Override
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final Properties componentProps,
                                       final Object key, final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
        ComponentAdapter<T> delegate1 = super.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams);
        return monitor.changedBehavior(new Assimilated<T>(assimilationType, delegate1, proxyFactory));
    }

    @Override
	public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
                                                final Properties componentProps, final ComponentAdapter<T> adapter) {
        ComponentAdapter<T> delegate1 = super.addComponentAdapter(monitor, lifecycle, componentProps, adapter);
        return monitor.changedBehavior(new Assimilated<T>(assimilationType, delegate1));
    }

    /**
     * ComponentAdapter that assimilates a component for a specific type.
     * <p>
     * Allows the instance of another {@link org.picocontainer.ComponentAdapter} to be converted into interface <code>type</code>, that the
     * instance is not assignable from. In other words the instance of the delegated adapter does NOT necessarily implement the
     * <code>type</code> interface.
     * </p>
     * <p>
     * For Example:
     * </p>
     * <code><pre>
     * public interface Foo {
     *     int size();
     * }
     *
     * public class Bar {
     *     public int size() {
     *         return 1;
     *     }
     * }
     *
     * new Assimilated(Foo.class, new InstanceAdapter(new Bar()));
     * </pre></code>
     * <p>
     * Notice how Bar does not implement the interface Foo. But Bar does have an identical <code>size()</code> method.
     * </p>
     * @author J&ouml;rg Schaible
     * @author Michael Ward
     */
    @SuppressWarnings("serial")
    public static final class Assimilated<T> extends AbstractChangedBehavior<T> {

        private final Class<T> type;
        private final ProxyFactory proxyFactory;
        private final boolean isCompatible;

        /**
         * Construct an Assimilated. The <code>type</code> may not implement the type of the component instance.
         * If the component instance <b>does</b> implement the interface, no proxy is used though.
         *
         * @param type The class type used as key.
         * @param delegate The delegated {@link org.picocontainer.ComponentAdapter}.
         * @param proxyFactory The {@link com.thoughtworks.proxy.ProxyFactory} to use.
         * @throws org.picocontainer.PicoCompositionException Thrown if the <code>type</code> is not compatible and cannot be proxied.
         */
        @SuppressWarnings("unchecked")
        public Assimilated(final Class<T> type, final ComponentAdapter<T> delegate, final ProxyFactory proxyFactory)
                throws PicoCompositionException {
            super(delegate);
            this.type = type;
            this.proxyFactory = proxyFactory;
            final Class<? extends T> delegationType = delegate.getComponentImplementation();
            this.isCompatible = type.isAssignableFrom(delegationType);
            if (!isCompatible) {
                if (!proxyFactory.canProxy(type)) {
                    throw new PicoCompositionException("Cannot create proxy for type " + type.getName());
                }
                final Method[] methods = type.getMethods();
                for (final Method method : methods) {
                    try {
                        delegationType.getMethod(method.getName(), method.getParameterTypes());
                    } catch (final NoSuchMethodException e) {
                        throw new PicoCompositionException("Cannot create proxy for type "
                                                             + type.getName()
                                                             + ", because of incompatible method "
                                                             + method.toString());
                    }
                }
            }
        }

        /**
         * Construct an Assimilated. The <code>type</code> may not implement the type of the component instance.
         * The implementation will use JDK {@link java.lang.reflect.Proxy} instances. If the component instant <b>does </b>
         * implement the interface, no proxy is used anyway.
         *
         * @param type The class type used as key.
         * @param delegate The delegated {@link org.picocontainer.ComponentAdapter}.
         *
         */
        @SuppressWarnings("unchecked")
        public Assimilated(final Class<T> type, final ComponentAdapter<T> delegate) {
            this(type, delegate, new StandardProxyFactory());
        }

        /**
         * Create and return a component instance. If the component instance and the type to assimilate is not compatible, a proxy
         * for the instance is generated, that implements the assimilated type.
         *
         * @see org.picocontainer.behaviors.AbstractBehavior.AbstractChangedBehavior#getComponentInstance(org.picocontainer.PicoContainer, Type into)
         */
        @Override
        public T getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException  {
            return isCompatible ? super.getComponentInstance(container, into)
                    : Delegating.proxy(type).with(super.getComponentInstance(container, into)).build(proxyFactory);
        }

        public String getDescriptor() {
            return "Assimilated";
        }

        /**
         * Return the type of the component. If the component type is not compatible with the type to assimilate, the assimilated
         * type is returned.
         *
         * @see org.picocontainer.behaviors.AbstractBehavior.AbstractChangedBehavior#getComponentImplementation()
         */
        @Override
        public Class<? extends T> getComponentImplementation() {
            return isCompatible ? super.getComponentImplementation() : type;
        }

        /**
         * Return the key of the component. If the key of the delegated component is a type, that is not compatible with the type to
         * assimilate, then the assimilated type replaces the original type.
         *
         * @see org.picocontainer.behaviors.AbstractBehavior.AbstractChangedBehavior#getComponentKey()
         */
        @Override
        public Object getComponentKey() {
            final Object key = super.getComponentKey();
            if (key instanceof Class && (!isCompatible || !type.isAssignableFrom((Class)key))) {
                return type;
            }
            return key;
        }

    }
}

