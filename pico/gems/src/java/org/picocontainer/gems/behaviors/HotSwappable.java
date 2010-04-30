/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.gems.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;

import java.lang.reflect.Type;


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
public class HotSwappable<T> extends AsmHiddenImplementation<T> {

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
