/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package org.picocontainer.gems.behaviors;

import java.lang.reflect.Type;
import java.util.Properties;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.gems.GemsCharacteristics;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;


/**
 * Hides implementation.
 *
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @see HotSwappable
 */
@SuppressWarnings("serial")
public class HotSwapping extends AbstractBehavior {

	@Override
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final Properties componentProps,
                                 final Object key, final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
        ComponentAdapter<T> delegateAdapter = super.createComponentAdapter(monitor, lifecycle,
                componentProps, key, impl, constructorParams, fieldParams, methodParams);

        if (AbstractBehavior.removePropertiesIfPresent(componentProps, GemsCharacteristics.NO_HOT_SWAP)) {
        	return delegateAdapter;
		}

		AbstractBehavior.removePropertiesIfPresent(componentProps, GemsCharacteristics.HOT_SWAP);
        return monitor.changedBehavior(new HotSwappable<T>(delegateAdapter));
    }

    @Override
	public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
                                                final Properties componentProps, final ComponentAdapter<T> adapter) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProps, GemsCharacteristics.NO_HOT_SWAP)) {
        	return super.addComponentAdapter(monitor,
                    lifecycle,
                    componentProps,
                    adapter);
		}


		AbstractBehavior.removePropertiesIfPresent(componentProps, GemsCharacteristics.HOT_SWAP);
    	return monitor.changedBehavior(new HotSwappable<T>(super.addComponentAdapter(monitor,
                                                                 lifecycle,
                                                                 componentProps,
                                                                 adapter)));
    }

    /**
     * This component adapter makes it possible to hide the implementation of a real subject (behind a proxy). If the key of the
     * component is of type {@link Class} and that class represents an interface, the proxy will only implement the interface
     * represented by that Class. Otherwise (if the key is something else), the proxy will implement all the interfaces of the
     * underlying subject. In any case, the proxy will also implement {@link com.thoughtworks.proxy.toys.hotswap.Swappable}, making
     * it possible to swap out the underlying subject at runtime. <p/> <em>
     * Note that this class doesn't cache instances. If you want caching,
     * use a {@link org.picocontainer.behaviors.Caching.Cached} around this one.
     * </em>
     *
     * @author Paul Hammant
     */
    @SuppressWarnings("serial")
    public static class HotSwappable<T> extends AsmImplementationHiding.AsmHiddenImplementation<T> {

        private final Swappable swappable = new Swappable();

        private T instance;

        public HotSwappable(final ComponentAdapter<T> delegate) {
            super(delegate);
        }

        @Override
        protected Swappable getSwappable() {
            return swappable;
        }

        @SuppressWarnings("unchecked")
        public T swapRealInstance(final T instance) {
            return (T) swappable.swap(instance);
        }

        @SuppressWarnings("unchecked")
        public T getRealInstance() {
            return (T) swappable.getInstance();
        }


        @Override
        public T getComponentInstance(final PicoContainer container, final Type into) {
            synchronized (swappable) {
                if (instance == null) {
                    instance = super.getComponentInstance(container, into);
                }
            }
            return instance;
        }

        @Override
        public String getDescriptor() {
            return "HotSwappable";
        }

        public static class Swappable {

            private transient Object delegate;

            public Object getInstance() {
                return delegate;
            }

            public Object swap(final Object delegate) {
                Object old = this.delegate;
                this.delegate = delegate;
                return old;
            }

        }

    }
}
