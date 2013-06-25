/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.gems.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.adapters.InstanceAdapter;


public class PicoMap implements Map {

    private final MutablePicoContainer mutablePicoContainer;

    public PicoMap(final MutablePicoContainer mutablePicoContainer) {
        this.mutablePicoContainer = mutablePicoContainer;
    }

    public PicoMap() {
        mutablePicoContainer = new DefaultPicoContainer();
    }

    public int size() {
        return mutablePicoContainer.getComponentAdapters().size();
    }

    public boolean isEmpty() {
        return mutablePicoContainer.getComponentAdapters().size() == 0;
    }

    public boolean containsKey(final Object o) {
        if (o instanceof Class) {
            return mutablePicoContainer.getComponent((Class<?>)o) != null;
        } else {
            return mutablePicoContainer.getComponent(o) != null;
        }
    }

    public boolean containsValue(final Object o) {
        return false;
    }

    public Object get(final Object o) {
        if (o instanceof Class) {
            return mutablePicoContainer.getComponent((Class<?>)o);
        } else {
            return mutablePicoContainer.getComponent(o);
        }
    }

    public Object put(final Object o, final Object o1) {
        Object object = remove(o);
        mutablePicoContainer.addComponent(o, o1);
        return object;
    }

    public Object remove(final Object o) {
        ComponentAdapter adapter = mutablePicoContainer.removeComponent(o);
        if (adapter != null) {
            // if previously an instance was registered, return it, otherwise return the type
            return adapter instanceof InstanceAdapter ? adapter
                    .getComponentInstance(mutablePicoContainer, ComponentAdapter.NOTHING.class) : adapter
                    .getComponentImplementation();
        } else {
            return null;
        }
    }

    public void putAll(final Map map) {
        for (Object o : map.entrySet()) {
            final Entry entry = (Entry) o;
            put(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        Set adapters = keySet();
        for (Object adapter : adapters) {
            mutablePicoContainer.removeComponent(adapter);
        }
    }

    public Set keySet() {
        Set<Object> set = new HashSet<Object>();
        Collection<ComponentAdapter<?>> adapters = mutablePicoContainer.getComponentAdapters();
        for (final ComponentAdapter<?> adapter : adapters) {
            set.add(adapter.getComponentKey());
        }
        return Collections.unmodifiableSet(set);
    }

    @SuppressWarnings({ "unchecked" })
    public Collection values() {
        return Collections.unmodifiableCollection(mutablePicoContainer.getComponents());
    }

    public Set entrySet() {
        Set<Entry> set = new HashSet<Entry>();
        Collection<ComponentAdapter<?>> adapters = mutablePicoContainer.getComponentAdapters();
        for (ComponentAdapter<?> adapter : adapters) {
            final Object key = adapter.getComponentKey();
            final Object component = mutablePicoContainer.getComponent(key);
            set.add(new Entry() {
                public Object getKey() {
                    return key;
                }

                public Object getValue() {
                    return component;
                }

                public Object setValue(final Object value) {
                    throw new UnsupportedOperationException("Cannot set addComponent");
                }
            });
        }
        return Collections.unmodifiableSet(set);
    }
}
