/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.containers;

import org.picocontainer.*;
import org.picocontainer.converters.ConvertsNothing;

import java.util.List;
import java.util.Collection;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
* wrap pico container to achieve immutability
 * Typically its used to mock a parent container.
 *
 * @author Konstantin Pribluda
 */
@SuppressWarnings("serial")
public final class ImmutablePicoContainer implements PicoContainer, Converting, Serializable {

    private final PicoContainer delegate;

    public ImmutablePicoContainer(PicoContainer delegate) {
        if (delegate == null) {
            throw new NullPointerException();
        }
        this.delegate = delegate;
    }

    public Object getComponent(Object keyOrType) {
        return getComponentInto(keyOrType, ComponentAdapter.NOTHING.class);
    }

    public Object getComponentInto(Object keyOrType, Type into) {
        return delegate.getComponentInto(keyOrType, into);
    }

    public <T> T getComponent(Class<T> componentType) {
        return getComponentInto(componentType, ComponentAdapter.NOTHING.class);
    }

    public <T> T getComponentInto(Class<T> componentType, Type into) {
        return delegate.getComponentInto(componentType, into);
    }

    public <T> T getComponent(Class<T> componentType, Class<? extends Annotation> binding, Type into) {
        return delegate.getComponent(componentType, binding, into);
    }

    public <T> T getComponent(Class<T> componentType, Class<? extends Annotation> binding) {
        return delegate.getComponent(componentType, binding);
    }

    public List getComponents() {
        return delegate.getComponents();
    }

    public PicoContainer getParent() {
        return delegate.getParent();
    }

    public ComponentAdapter<?> getComponentAdapter(Object key) {
        return delegate.getComponentAdapter(key);
    }

    public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, NameBinding componentNameBinding) {
        return delegate.getComponentAdapter(componentType, componentNameBinding);
    }

    public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, Class<? extends Annotation> binding) {
        return delegate.getComponentAdapter(componentType, binding);
    }

    public Collection<ComponentAdapter<?>> getComponentAdapters() {
        return delegate.getComponentAdapters();
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(Class<T> componentType) {
        return delegate.getComponentAdapters(componentType);
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(Class<T> componentType, Class<? extends Annotation> binding) {
        return delegate.getComponentAdapters(componentType, binding);
    }

    public <T> List<T> getComponents(Class<T> componentType) {
        return delegate.getComponents(componentType);
    }

    public final void accept(PicoVisitor visitor) {
        // don't visit "this" its pointless.
        delegate.accept(visitor);
    }

    public boolean equals(Object obj) {
        return obj == this
               || (obj != null && obj == delegate)
               || (obj instanceof ImmutablePicoContainer && ((ImmutablePicoContainer) obj).delegate == delegate)
            ;
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public String toString() {
        return "I<" + delegate.toString();
    }

    public Converters getConverters() {
        if (delegate instanceof Converting) {
            return ((Converting) delegate).getConverters();
        }
        return new ConvertsNothing();
    }
}
