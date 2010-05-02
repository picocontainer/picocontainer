/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.behaviors;

import org.picocontainer.Behavior;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;
import org.picocontainer.annotations.Cache;
import org.picocontainer.injectors.AdaptingInjection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("serial")
public class AdaptingBehavior implements Behavior, Serializable {


    public ComponentAdapter createComponentAdapter(ComponentMonitor monitor,
                                                   LifecycleStrategy lifecycle,
                                                   Properties componentProps,
                                                   Object key,
                                                   Class impl,
                                                   Parameter... parameters) throws PicoCompositionException {
        List<Behavior> list = new ArrayList<Behavior>();
        ComponentFactory lastFactory = makeInjectionFactory();
        processSynchronizing(componentProps, list);
        processLocking(componentProps, list);
        processPropertyApplying(componentProps, list);
        processAutomatic(componentProps, list);
        processImplementationHiding(componentProps, list);
        processCaching(componentProps, impl, list);
        processGuarding(componentProps, impl, list);

        //Instantiate Chain of ComponentFactories
        for (ComponentFactory componentFactory : list) {
            if (lastFactory != null && componentFactory instanceof Behavior) {
                ((Behavior)componentFactory).wrap(lastFactory);
            }
            lastFactory = componentFactory;
        }

        return lastFactory.createComponentAdapter(monitor,
                                                  lifecycle,
                                                  componentProps,
                                                  key,
                                                  impl,
                                                  parameters);
    }


    public ComponentAdapter addComponentAdapter(ComponentMonitor monitor,
                                                LifecycleStrategy lifecycle,
                                                Properties componentProps,
                                                ComponentAdapter adapter) {
        List<Behavior> list = new ArrayList<Behavior>();
        processSynchronizing(componentProps, list);
        processImplementationHiding(componentProps, list);
        processCaching(componentProps, adapter.getComponentImplementation(), list);
        processGuarding(componentProps, adapter.getComponentImplementation(), list);

        //Instantiate Chain of ComponentFactories
        Behavior lastFactory = null;
        for (Behavior componentFactory : list) {
            if (lastFactory != null) {
                componentFactory.wrap(lastFactory);
            }
            lastFactory = componentFactory;
        }

        if (lastFactory == null) {
            return adapter;
        }


        return lastFactory.addComponentAdapter(monitor, lifecycle, componentProps, adapter);
    }

    public void verify(PicoContainer container) {
    }

    public void accept(PicoVisitor visitor) {
        visitor.visitComponentFactory(this);
        
    }

    protected AdaptingInjection makeInjectionFactory() {
        return new AdaptingInjection();
    }

    protected void processSynchronizing(Properties componentProps, List<Behavior> list) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProps, Characteristics.SYNCHRONIZE)) {
            list.add(new Synchronizing());
        }
    }

    protected void processLocking(Properties componentProps, List<Behavior> list) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProps, Characteristics.LOCK)) {
            list.add(new Locking());
        }
    }

    protected void processCaching(Properties componentProps,
                                       Class impl,
                                       List<Behavior> list) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProps, Characteristics.CACHE) ||
            impl.getAnnotation(Cache.class) != null) {
            list.add(new Caching());
        }
        AbstractBehavior.removePropertiesIfPresent(componentProps, Characteristics.NO_CACHE);
    }

    protected void processGuarding(Properties componentProps, Class impl, List<Behavior> list) {
        if (AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.GUARD, false)) {
            list.add(new Guarding());
        }
    }

    protected void processImplementationHiding(Properties componentProps, List<Behavior> list) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProps, Characteristics.HIDE_IMPL)) {
            list.add(new ImplementationHiding());
        }
        AbstractBehavior.removePropertiesIfPresent(componentProps, Characteristics.NO_HIDE_IMPL);
    }

    protected void processPropertyApplying(Properties componentProps, List<Behavior> list) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProps, Characteristics.PROPERTY_APPLYING)) {
            list.add(new PropertyApplying());
        }
    }

    protected void processAutomatic(Properties componentProps, List<Behavior> list) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProps, Characteristics.AUTOMATIC)) {
            list.add(new Automating());
        }
    }

    public ComponentFactory wrap(ComponentFactory delegate) {
        throw new UnsupportedOperationException();
    }
}
