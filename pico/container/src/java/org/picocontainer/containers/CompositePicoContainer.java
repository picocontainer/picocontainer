/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by various                           *
 *****************************************************************************/
package org.picocontainer.containers;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Converters;
import org.picocontainer.Converting;
import org.picocontainer.NameBinding;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * CompositePicoContainer takes a var-args list of containers and will query them
 * in turn for getComponent(*) and getComponentAdapter(*) requests.  Methods returning
 * lists and getParent/accept will not function.
 */
public class CompositePicoContainer implements PicoContainer, Converting, Serializable {

    private final PicoContainer[] containers;
    private Converters compositeConverter = new CompositeConverters();

    public class CompositeConverters implements Converters {
        public boolean canConvert(Type type) {
            for (PicoContainer container : containers) {
                if (container instanceof Converting && ((Converting) container).getConverters().canConvert(type)) {
                    return true;
                }
            }
            return false;
        }

        public Object convert(String paramValue, Type type) {
            for (PicoContainer container : containers) {
                if (container instanceof Converting) {
                    Converters converter = ((Converting) container).getConverters();
                    if (converter.canConvert(type)) {
                        return converter.convert(paramValue, type);
                    }
                }
            }
            return null;
        }
    }

    public CompositePicoContainer(PicoContainer... containers) {
        this.containers = containers;
    }

    public <T> T getComponentInto(Class<T> componentType, Type into) {
        for (PicoContainer container : containers) {
            T inst = container.getComponentInto(componentType, into);
            if (inst != null) {
                return inst;
            }
        }
        return null;
    }

    public Object getComponent(Object keyOrType) {
        return getComponentInto(keyOrType, ComponentAdapter.NOTHING.class);
    }

    public Object getComponentInto(Object keyOrType, Type into) {
        for (PicoContainer container : containers) {
            Object inst = container.getComponentInto(keyOrType, into);
            if (inst != null) {
                return inst;
            }
        }
        return null;
    }

    public <T> T getComponent(Class<T> componentType) {
        return getComponentInto(componentType, ComponentAdapter.NOTHING.class);
    }

    public ComponentAdapter getComponentAdapter(Object key) {
        for (PicoContainer container : containers) {
            ComponentAdapter inst = container.getComponentAdapter(key);
            if (inst != null) {
                return inst;
            }
        }
        return null;
    }

    public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, NameBinding nameBinding) {
        for (PicoContainer container : containers) {
            ComponentAdapter<T> inst = container.getComponentAdapter(componentType, nameBinding);
            if (inst != null) {
                return inst;
            }
        }
        return null;
    }

    public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, Class<? extends Annotation> binding) {
        for (PicoContainer container : containers) {
            ComponentAdapter<T> inst = container.getComponentAdapter(componentType, binding);
            if (inst != null) {
                return inst;
            }
        }
        return null;
    }

    public <T> T getComponent(Class<T> componentType, Class<? extends Annotation> binding, Type into) {
        return null;
    }

    public <T> T getComponent(Class<T> componentType, Class<? extends Annotation> binding) {
        return null;
    }

    public List<Object> getComponents() {
        return Collections.emptyList();
    }

    public PicoContainer getParent() {
        return null;
    }

    public Collection<ComponentAdapter<?>> getComponentAdapters() {
        return Collections.emptyList();
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(Class<T> componentType) {
        return Collections.emptyList();
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(Class<T> componentType, Class<? extends Annotation> binding) {
        return Collections.emptyList();
    }

    public <T> List<T> getComponents(Class<T> componentType) {
        return Collections.emptyList();
    }

    public void accept(PicoVisitor visitor) {
    }

    public Converters getConverters() {
        return compositeConverter;
    }
}
