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


    public ComponentAdapter createComponentAdapter(ComponentMonitor componentMonitor,
                                                   LifecycleStrategy lifecycleStrategy,
                                                   Properties componentProperties,
                                                   Object key,
                                                   Class componentImplementation,
                                                   Parameter... parameters) throws PicoCompositionException {
        List<Behavior> list = new ArrayList<Behavior>();
        ComponentFactory lastFactory = makeInjectionFactory();
        processSynchronizing(componentProperties, list);
        processLocking(componentProperties, list);
        processPropertyApplying(componentProperties, list);
        processAutomatic(componentProperties, list);
        processImplementationHiding(componentProperties, list);
        processCaching(componentProperties, componentImplementation, list);
        processGuarding(componentProperties, componentImplementation, list);

        //Instantiate Chain of ComponentFactories
        for (ComponentFactory componentFactory : list) {
            if (lastFactory != null && componentFactory instanceof Behavior) {
                ((Behavior)componentFactory).wrap(lastFactory);
            }
            lastFactory = componentFactory;
        }

        return lastFactory.createComponentAdapter(componentMonitor,
                                                  lifecycleStrategy,
                                                  componentProperties,
                                                  key,
                                                  componentImplementation,
                                                  parameters);
    }


    public ComponentAdapter addComponentAdapter(ComponentMonitor componentMonitor,
                                                LifecycleStrategy lifecycleStrategy,
                                                Properties componentProperties,
                                                ComponentAdapter adapter) {
        List<Behavior> list = new ArrayList<Behavior>();
        processSynchronizing(componentProperties, list);
        processImplementationHiding(componentProperties, list);
        processCaching(componentProperties, adapter.getComponentImplementation(), list);
        processGuarding(componentProperties, adapter.getComponentImplementation(), list);

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


        return lastFactory.addComponentAdapter(componentMonitor, lifecycleStrategy, componentProperties, adapter);
    }

    public void verify(PicoContainer container) {
    }

    public void accept(PicoVisitor visitor) {
        visitor.visitComponentFactory(this);
        
    }

    protected AdaptingInjection makeInjectionFactory() {
        return new AdaptingInjection();
    }

    protected void processSynchronizing(Properties componentProperties, List<Behavior> list) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProperties, Characteristics.SYNCHRONIZE)) {
            list.add(new Synchronizing());
        }
    }

    protected void processLocking(Properties componentProperties, List<Behavior> list) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProperties, Characteristics.LOCK)) {
            list.add(new Locking());
        }
    }

    protected void processCaching(Properties componentProperties,
                                       Class componentImplementation,
                                       List<Behavior> list) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProperties, Characteristics.CACHE) ||
            componentImplementation.getAnnotation(Cache.class) != null) {
            list.add(new Caching());
        }
        AbstractBehavior.removePropertiesIfPresent(componentProperties, Characteristics.NO_CACHE);
    }

    protected void processGuarding(Properties componentProperties, Class componentImplementation, List<Behavior> list) {
        if (AbstractBehavior.arePropertiesPresent(componentProperties, Characteristics.GUARD, false)) {
            list.add(new Guarding());
        }
    }

    protected void processImplementationHiding(Properties componentProperties, List<Behavior> list) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProperties, Characteristics.HIDE_IMPL)) {
            list.add(new ImplementationHiding());
        }
        AbstractBehavior.removePropertiesIfPresent(componentProperties, Characteristics.NO_HIDE_IMPL);
    }

    protected void processPropertyApplying(Properties componentProperties, List<Behavior> list) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProperties, Characteristics.PROPERTY_APPLYING)) {
            list.add(new PropertyApplying());
        }
    }

    protected void processAutomatic(Properties componentProperties, List<Behavior> list) {
        if (AbstractBehavior.removePropertiesIfPresent(componentProperties, Characteristics.AUTOMATIC)) {
            list.add(new Automating());
        }
    }

    public ComponentFactory wrap(ComponentFactory delegate) {
        throw new UnsupportedOperationException();
    }
}
