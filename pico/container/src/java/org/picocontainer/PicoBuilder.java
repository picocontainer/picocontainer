/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer;

import org.picocontainer.behaviors.Automating;
import org.picocontainer.behaviors.Locking;
import org.picocontainer.behaviors.PropertyApplying;
import org.picocontainer.behaviors.Synchronizing;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.containers.TransientPicoContainer;
import org.picocontainer.injectors.CompositeInjection;
import org.picocontainer.injectors.MethodInjection;
import org.picocontainer.lifecycle.JavaEE5LifecycleStrategy;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.lifecycle.ReflectionLifecycleStrategy;
import org.picocontainer.lifecycle.StartableLifecycleStrategy;
import org.picocontainer.monitors.ConsoleComponentMonitor;
import org.picocontainer.monitors.NullComponentMonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.picocontainer.behaviors.Behaviors.caching;
import static org.picocontainer.behaviors.Behaviors.implementationHiding;
import static org.picocontainer.injectors.Injectors.CDI;
import static org.picocontainer.injectors.Injectors.SDI;
import static org.picocontainer.injectors.Injectors.adaptiveDI;
import static org.picocontainer.injectors.Injectors.annotatedFieldDI;
import static org.picocontainer.injectors.Injectors.annotatedMethodDI;
import static org.picocontainer.injectors.Injectors.namedField;
import static org.picocontainer.injectors.Injectors.namedMethod;
import static org.picocontainer.injectors.Injectors.typedFieldDI;

/**
 * Helps assembles the myriad items available to a picocontainer.
 * <p>Simple Example:</p>
 * <pre>
 * MutablePicoContainer mpc = new PicoBuilder()
 * &nbsp;&nbsp;.withCaching()
 * &nbsp;&nbsp;.withLifecycle()
 * &nbsp;&nbsp;.build();
 * </pre>
 * @author Paul Hammant
 */
public class PicoBuilder {

    private PicoContainer parentContainer;
    private Class<? extends MutablePicoContainer> mpcClass = DefaultPicoContainer.class;
    private ComponentMonitor monitor;
    private List<Object> containerComps = new ArrayList<Object>();
    private boolean addChildToParent;
    private LifecycleStrategy lifecycle;
    private final Stack<Object> behaviors = new Stack<Object>();
    private final List<InjectionType> injectors = new ArrayList<InjectionType>();
    private Class<? extends ComponentMonitor> monitorClass = NullComponentMonitor.class;
    private Class<? extends LifecycleStrategy> lifecycleClass = NullLifecycleStrategy.class;


    public PicoBuilder(PicoContainer parentContainer, InjectionType injectionType) {
        this(parentContainer);
        injectors.add(injectionType);
    }

    /**
     * Constructs a PicoBuilder using the specified PicoContainer as an argument.  Note
     * that this only creates child -&gt; parent references.  You must use  parentContainer.addChildContainer()
     * to the instance built here if you require child  &lt;-&gt; parent references. 
     * @param parentContainer
     */
    public PicoBuilder(PicoContainer parentContainer) {
        if (parentContainer != null) {
            this.parentContainer = parentContainer;
        } else {
            this.parentContainer = new EmptyPicoContainer();
        }
    }

    public PicoBuilder(InjectionType injectionType) {
        this(new EmptyPicoContainer(), injectionType);
    }

    /**
     * Will be used to build a PicoContainer not bound to any parent container.
     */
    public PicoBuilder() {
        this(new EmptyPicoContainer());
    }

    public PicoBuilder withLifecycle() {
        lifecycleClass = StartableLifecycleStrategy.class;
        lifecycle = null;
        return this;
    }

    /**
     * Constructed PicoContainer will use {@linkplain org.picocontainer.lifecycle.ReflectionLifecycleStrategy ReflectionLifecycle}.
     * @return <em>this</em> to allow for method chaining.
     */
    public PicoBuilder withReflectionLifecycle() {
        lifecycleClass = ReflectionLifecycleStrategy.class;
        lifecycle = null;
        return this;
    }

    /**
     * Allows you to specify your own lifecycle strategy class.
     * @param specifiedLifecycleStrategyType lifecycle strategy type.
     * @return <em>this</em> to allow for method chaining.
     */
    public PicoBuilder withLifecycle(Class<? extends LifecycleStrategy> lifecycleClass) {
        this.lifecycleClass = lifecycleClass;
        lifecycle = null;
        return this;
    }

    /**
     * Constructed PicoContainer will use {@linkplain org.picocontainer.lifecycle.JavaEE5LifecycleStrategy JavaEE5LifecycleStrategy}.
     * @return <em>this</em> to allow for method chaining.
     */
    public PicoBuilder withJavaEE5Lifecycle() {
        this.lifecycleClass = JavaEE5LifecycleStrategy.class;
        lifecycle = null;
        return this;
    }

    public PicoBuilder withLifecycle(LifecycleStrategy lifecycle) {
        this.lifecycle = lifecycle;
        lifecycleClass = null;
        return this;
    }


    public PicoBuilder withConsoleMonitor() {
        monitorClass =  ConsoleComponentMonitor.class;
        return this;
    }

    /**
     * Allows you to specify your very own component monitor to be used by the created
     * picocontainer
     * @param specifiedComponentMonitor
     * @return <em>this</em> to allow for method chaining.
     */
    public PicoBuilder withMonitor(Class<? extends ComponentMonitor> cmClass) {
        if (cmClass == null) {
            throw new NullPointerException("monitor class cannot be null");
        }
        if (!ComponentMonitor.class.isAssignableFrom(cmClass)) {
            throw new ClassCastException(cmClass.getName() + " is not a " + ComponentMonitor.class.getName());

        }
        monitorClass = cmClass;
        monitor = null;
        return this;
    }

    public MutablePicoContainer build() {

        DefaultPicoContainer tempContainer = new TransientPicoContainer();
        tempContainer.addComponent(PicoContainer.class, parentContainer);

        addContainerComponents(tempContainer);

        ComponentFactory componentFactory;
        if (injectors.size() == 1) {
            componentFactory = injectors.get(0);
        } else if (injectors.size() == 0) {
            componentFactory = adaptiveDI();
        } else {
            componentFactory = new CompositeInjection(injectors.toArray(new InjectionType[injectors.size()]));
        }
        
        Stack<Object> clonedBehaviors = (Stack< Object >) behaviors.clone();
        while (!clonedBehaviors.empty()) {  
        	componentFactory = buildComponentFactory(tempContainer, componentFactory, clonedBehaviors);
        }

        tempContainer.addComponent(ComponentFactory.class, componentFactory);

        buildComponentMonitor(tempContainer);

        if (lifecycle == null) {
            tempContainer.addComponent(LifecycleStrategy.class, lifecycleClass);
        } else {
            tempContainer.addComponent(LifecycleStrategy.class, lifecycle);

        }
        tempContainer.addComponent("mpc", mpcClass);

        MutablePicoContainer newContainer = (MutablePicoContainer) tempContainer.getComponent("mpc");

        addChildToParent(newContainer);
        return newContainer;
    }

    private void buildComponentMonitor(DefaultPicoContainer tempContainer) {
        if (monitorClass == null) {
            tempContainer.addComponent(ComponentMonitor.class, monitor);
        } else {
            tempContainer.addComponent(ComponentMonitor.class, monitorClass);
        }
    }

    private void addChildToParent(MutablePicoContainer newContainer) {
        if (addChildToParent) {
            if (parentContainer instanceof MutablePicoContainer) {
                ((MutablePicoContainer)parentContainer).addChildContainer(newContainer);
            } else {
                throw new PicoCompositionException("If using addChildContainer() the parent must be a MutablePicoContainer");
            }
        }
    }

    private void addContainerComponents(DefaultPicoContainer temp) {
        for (Object containerComp : containerComps) {
            temp.addComponent(containerComp);
        }
    }

    private ComponentFactory buildComponentFactory(DefaultPicoContainer container, final ComponentFactory lastCaf, final Stack<Object> clonedBehaviors) {
        Object componentFactory = clonedBehaviors.pop();
        DefaultPicoContainer tmpContainer = new TransientPicoContainer(container);
        tmpContainer.addComponent("componentFactory", componentFactory);
        if (lastCaf != null) {
            tmpContainer.addComponent(ComponentFactory.class, lastCaf);
        }
        ComponentFactory newlastCaf = (ComponentFactory) tmpContainer.getComponent("componentFactory");
        if (newlastCaf instanceof Behavior) {
            ((Behavior) newlastCaf).wrap(lastCaf);
        }
        return newlastCaf;
    }

    public PicoBuilder withHiddenImplementations() {
        behaviors.push(implementationHiding());
        return this;
    }

    public PicoBuilder withSetterInjection() {
        injectors.add(SDI());
        return this;
    }

    public PicoBuilder withAnnotatedMethodInjection() {
        injectors.add(annotatedMethodDI());
        return this;
    }


    public PicoBuilder withAnnotatedFieldInjection() {
        injectors.add(annotatedFieldDI());
        return this;
    }

    public PicoBuilder withTypedFieldInjection() {
        injectors.add(typedFieldDI());
        return this;
    }


    public PicoBuilder withConstructorInjection() {
        injectors.add(CDI());
        return this;
    }

    public PicoBuilder withNamedMethodInjection() {
        injectors.add(namedMethod());
        return this;
    }

    public PicoBuilder withNamedFieldInjection() {
        injectors.add(namedField());
        return this;
    }

    public PicoBuilder withCaching() {
        behaviors.push(caching());
        return this;
    }

    public PicoBuilder withComponentFactory(ComponentFactory componentFactory) {
        if (componentFactory == null) {
            throw new NullPointerException("CAF cannot be null");
        }
        behaviors.push(componentFactory);
        return this;
    }

    public PicoBuilder withSynchronizing() {
        behaviors.push(new Synchronizing());
        return this;
    }

    public PicoBuilder withLocking() {
        behaviors.push(new Locking());
        return this;
    }

    public PicoBuilder withBehaviors(Behavior... factories) {
        for (Behavior componentFactory : factories) {
            behaviors.push(componentFactory);
        }
        return this;
    }

    public PicoBuilder implementedBy(Class<? extends MutablePicoContainer> containerClass) {
        mpcClass = containerClass;
        return this;
    }

    public PicoBuilder withMonitor(ComponentMonitor monitor) {
        this.monitor = monitor;
        monitorClass = null;
        return this;
    }

    public PicoBuilder withComponentFactory(Class<? extends ComponentFactory> componentFactoryClass) {
        behaviors.push(componentFactoryClass);
        return this;
    }

    public PicoBuilder withCustomContainerComponent(Object containerDependency) {
        containerComps.add(containerDependency);
        return this;
    }

    public PicoBuilder withPropertyApplier() {
        behaviors.push(new PropertyApplying());
        return this;
    }

    public PicoBuilder withAutomatic() {
        behaviors.push(new Automating());
        return this;
    }

    public PicoBuilder withMethodInjection() {
        injectors.add(new MethodInjection());
        return this;
    }

    public PicoBuilder addChildToParent() {
        addChildToParent =  true;
        return this;
    }
}
