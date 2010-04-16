/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved. *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD * style
 * license a copy of which has been included with this distribution in * the
 * LICENSE.txt file. * * Original code by *
 ******************************************************************************/
package org.picocontainer.behaviors;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;

import org.picocontainer.Behavior;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;
import org.picocontainer.Characteristics;
import org.picocontainer.InjectionType;
import org.picocontainer.injectors.AdaptingInjection;

@SuppressWarnings("serial")
public class AbstractBehavior implements ComponentFactory, Serializable, Behavior {

    private ComponentFactory delegate;


    public ComponentFactory wrap(ComponentFactory delegate) {
        this.delegate = delegate;
        return this;
    }

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor componentMonitor,
            LifecycleStrategy lifecycleStrategy, Properties componentProperties, Object componentKey,
            Class<T> componentImplementation, Parameter... parameters) throws PicoCompositionException {
        if (delegate == null) {
            delegate = new AdaptingInjection();
        }
        ComponentAdapter<T> compAdapter = delegate.createComponentAdapter(componentMonitor, lifecycleStrategy, componentProperties, componentKey,
                componentImplementation, parameters);

        boolean enableCircular = removePropertiesIfPresent(componentProperties, Characteristics.ENABLE_CIRCULAR);
        if (enableCircular && delegate instanceof InjectionType) {
            return componentMonitor.newBehavior(new HiddenImplementation(compAdapter));
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

}
