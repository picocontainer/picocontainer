/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by various                           *
 *****************************************************************************/
package org.picocontainer.containers;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Converters;
import org.picocontainer.Converting;
import org.picocontainer.NameBinding;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;

import com.googlecode.jtype.Generic;

/**
 * CompositePicoContainer takes a var-args list of containers and will query them
 * in turn for getComponent(*) and getComponentAdapter(*) requests.  Methods returning
 * lists and getParent/accept will not function.
 */
public class CompositePicoContainer implements PicoContainer, Converting, Serializable {

    private final PicoContainer[] containers;
    private final Converters compositeConverter = new CompositeConverters();

    public class CompositeConverters implements Converters {
        public boolean canConvert(final Type type) {
            for (PicoContainer container : containers) {
                if (container instanceof Converting && ((Converting) container).getConverters().canConvert(type)) {
                    return true;
                }
            }
            return false;
        }

        public Object convert(final String paramValue, final Type type) {
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

    public CompositePicoContainer(final PicoContainer... containers) {
        this.containers = containers;
    }

    public <T> T getComponentInto(final Class<T> componentType, final Type into) {
        for (PicoContainer container : containers) {
            T inst = container.getComponentInto(componentType, into);
            if (inst != null) {
                return inst;
            }
        }
        return null;
    }

    public <T> T getComponentInto(final Generic<T> componentType, final Type into) {
        for (PicoContainer container : containers) {
            T inst = container.getComponentInto(componentType, into);
            if (inst != null) {
                return inst;
            }
        }
        return null;
    }

    public Object getComponent(final Object keyOrType) {
        return getComponentInto(keyOrType, ComponentAdapter.NOTHING.class);
    }

    public Object getComponentInto(final Object keyOrType, final Type into) {
        for (PicoContainer container : containers) {
            Object inst = container.getComponentInto(keyOrType, into);
            if (inst != null) {
                return inst;
            }
        }
        return null;
    }

    public <T> T getComponent(final Class<T> componentType) {
        return getComponent(Generic.get(componentType));
    }


    public <T> T getComponent(final Generic<T> componentType) {
        for (PicoContainer container : containers) {
            Object inst = container.getComponent(componentType);
            if (inst != null) {
                return (T) inst;
            }
        }
        return null;
    }

    public ComponentAdapter getComponentAdapter(final Object key) {
        for (PicoContainer container : containers) {
            ComponentAdapter inst = container.getComponentAdapter(key);
            if (inst != null) {
                return inst;
            }
        }
        return null;
    }

    public <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final NameBinding nameBinding) {
        for (PicoContainer container : containers) {
            ComponentAdapter<T> inst = container.getComponentAdapter(Generic.get(componentType), nameBinding);
            if (inst != null) {
                return inst;
            }
        }
        return null;
    }

    public <T> ComponentAdapter<T> getComponentAdapter(final Generic<T> componentType, final NameBinding nameBinding) {
        for (PicoContainer container : containers) {
            ComponentAdapter<T> inst = container.getComponentAdapter(componentType, nameBinding);
            if (inst != null) {
                return inst;
            }
        }
        return null;
    }

    public <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final Class<? extends Annotation> binding) {
        return getComponentAdapter(Generic.get(componentType), binding);
    }

    public <T> ComponentAdapter<T> getComponentAdapter(final Generic<T> componentType, final Class<? extends Annotation> binding) {
        for (PicoContainer container : containers) {
            ComponentAdapter<T> inst = container.getComponentAdapter(componentType, binding);
            if (inst != null) {
                return inst;
            }
        }
        return null;
    }

    public <T> T getComponent(final Class<T> componentType, final Class<? extends Annotation> binding, final Type into) {
        return null;
    }

    public <T> T getComponent(final Class<T> componentType, final Class<? extends Annotation> binding) {
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

    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Class<T> componentType) {
        return Collections.emptyList();
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Generic<T> componentType) {
        return Collections.emptyList();
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Class<T> componentType, final Class<? extends Annotation> binding) {
        return Collections.emptyList();
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Generic<T> componentType, final Class<? extends Annotation> binding) {
        return Collections.emptyList();
    }

    public <T> List<T> getComponents(final Class<T> componentType) {
        return Collections.emptyList();
    }

    public void accept(final PicoVisitor visitor) {
    }

    public Converters getConverters() {
        return compositeConverter;
    }
}
