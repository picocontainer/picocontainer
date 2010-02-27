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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.picocontainer.BehaviorFactory;
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

@SuppressWarnings("serial")
public class AdaptingBehavior implements BehaviorFactory, Serializable {


    public ComponentAdapter createComponentAdapter(ComponentMonitor componentMonitor,
                                                   LifecycleStrategy lifecycleStrategy,
                                                   Properties componentProperties,
                                                   Object componentKey,
                                                   Class componentImplementation,
                                                   Parameter... parameters) throws PicoCompositionException {
        List<BehaviorFactory> list = new ArrayList<BehaviorFactory>();
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
            if (lastFactory != null && componentFactory instanceof BehaviorFactory) {
                ((BehaviorFactory)componentFactory).wrap(lastFactory);
            }
            lastFactory = componentFactory;
        }

        return lastFactory.createComponentAdapter(componentMonitor,
                                                  lifecycleStrategy,
                                                  componentProperties,
                                                  componentKey,
                                                  componentImplementation,
                                                  parameters);
    }


    public ComponentAdapter addComponentAdapter(ComponentMonitor componentMonitor,
                                                LifecycleStrategy lifecycleStrategy,
                                                Properties componentProperties,
                                                ComponentAdapter adapter) {
        List<BehaviorFactory> list = new ArrayList<BehaviorFactory>();
        processSynchronizing(componentProperties, list);
        processImplementationHiding(componentProperties, list);
        processCaching(componentProperties, adapter.getComponentImplementation(), list);
        processGuarding(componentProperties, adapter.getComponentImplementation(), list);

        //Instantiate Chain of ComponentFactories
        BehaviorFactory lastFactory = null;
        for (BehaviorFactory componentFactory : list) {
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

    protected void processSynchronizing(Properties componentProperties, List<BehaviorFactory> list) {
        if (AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, Characteristics.SYNCHRONIZE)) {
            list.add(new Synchronizing());
        }
    }

    protected void processLocking(Properties componentProperties, List<BehaviorFactory> list) {
        if (AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, Characteristics.LOCK)) {
            list.add(new Locking());
        }
    }

    protected void processCaching(Properties componentProperties,
                                       Class componentImplementation,
                                       List<BehaviorFactory> list) {
        if (AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, Characteristics.CACHE) ||
            componentImplementation.getAnnotation(Cache.class) != null) {
            list.add(new Caching());
        }
        AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, Characteristics.NO_CACHE);
    }

    protected void processGuarding(Properties componentProperties, Class componentImplementation, List<BehaviorFactory> list) {
        if (AbstractBehaviorFactory.arePropertiesPresent(componentProperties, Characteristics.GUARD, false)) {
            list.add(new Guarding());
        }
    }

    protected void processImplementationHiding(Properties componentProperties,
                                             List<BehaviorFactory> list) {
        if (AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, Characteristics.HIDE_IMPL)) {
            list.add(new ImplementationHiding());
        }
        AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, Characteristics.NO_HIDE_IMPL);
    }

    protected void processPropertyApplying(Properties componentProperties,
                                             List<BehaviorFactory> list) {
        if (AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, Characteristics.PROPERTY_APPLYING)) {
            list.add(new PropertyApplying());
        }
    }

    protected void processAutomatic(Properties componentProperties,
                                             List<BehaviorFactory> list) {
        if (AbstractBehaviorFactory.removePropertiesIfPresent(componentProperties, Characteristics.AUTOMATIC)) {
            list.add(new Automating());
        }
    }


    public ComponentFactory wrap(ComponentFactory delegate) {
        throw new UnsupportedOperationException();
    }
}
