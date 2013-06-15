/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer;

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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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
    private final List<Object> containerComps = new ArrayList<Object>();
    private boolean addChildToParent;
    private LifecycleStrategy lifecycle;
    private final Stack<Object> behaviors = new Stack<Object>();
    private final List<InjectionType> injectors = new ArrayList<InjectionType>();
    private Class<? extends ComponentMonitor> monitorClass = NullComponentMonitor.class;
    private Class<? extends LifecycleStrategy> lifecycleClass = NullLifecycleStrategy.class;


    public PicoBuilder(final PicoContainer parentContainer, final InjectionType injectionType) {
        this(parentContainer);
        addInjector(injectionType);
    }

    /**
     * Constructs a PicoBuilder using the specified PicoContainer as an argument.  Note
     * that this only creates child -&gt; parent references.  You must use  parentContainer.addChildContainer()
     * to the instance built here if you require child  &lt;-&gt; parent references.
     * @param parentContainer
     */
    public PicoBuilder(final PicoContainer parentContainer) {
        if (parentContainer != null) {
            this.parentContainer = parentContainer;
        } else {
            this.parentContainer = new EmptyPicoContainer();
        }
    }

    public PicoBuilder(final InjectionType injectionType) {
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
     * @param lifecycleClass lifecycle strategy type.
     * @return <em>this</em> to allow for method chaining.
     */
    public PicoBuilder withLifecycle(final Class<? extends LifecycleStrategy> lifecycleClass) {
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

    public PicoBuilder withLifecycle(final LifecycleStrategy lifecycle) {
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
     * @param cmClass the component monitor class to use
     * @return <em>this</em> to allow for method chaining.
     */
    public PicoBuilder withMonitor(final Class<? extends ComponentMonitor> cmClass) {
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

    private void buildComponentMonitor(final DefaultPicoContainer tempContainer) {
        if (monitorClass == null) {
            tempContainer.addComponent(ComponentMonitor.class, monitor);
        } else {
            tempContainer.addComponent(ComponentMonitor.class, monitorClass);
        }
    }

    private void addChildToParent(final MutablePicoContainer newContainer) {
        if (addChildToParent) {
            if (parentContainer instanceof MutablePicoContainer) {
                ((MutablePicoContainer)parentContainer).addChildContainer(newContainer);
            } else {
                throw new PicoCompositionException("If using addChildContainer() the parent must be a MutablePicoContainer");
            }
        }
    }

    private void addContainerComponents(final DefaultPicoContainer temp) {
        for (Object containerComp : containerComps) {
            temp.addComponent(containerComp);
        }
    }

    private ComponentFactory buildComponentFactory(final DefaultPicoContainer container, final ComponentFactory lastCaf, final Stack<Object> clonedBehaviors) {
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
        addInjector(SDI());
        return this;
    }

    public PicoBuilder withAnnotatedMethodInjection(final Class<? extends Annotation> injectionAnnotation) {
        addInjector(annotatedMethodDI(injectionAnnotation));
        return this;
    }

    public PicoBuilder withAnnotatedMethodInjection() {
        addInjector(annotatedMethodDI());
        return this;
    }

    public PicoBuilder withAnnotatedFieldInjection(final Class<? extends Annotation> injectionAnnotation) {
        addInjector(annotatedFieldDI(injectionAnnotation));
        return this;
    }

    public PicoBuilder withAnnotatedFieldInjection() {
        addInjector(annotatedFieldDI());
        return this;
    }

    public PicoBuilder withTypedFieldInjection() {
        addInjector(typedFieldDI());
        return this;
    }

    public PicoBuilder withConstructorInjection() {
        addInjector(CDI());
        return this;
    }

    public PicoBuilder withNamedMethodInjection() {
        addInjector(namedMethod());
        return this;
    }

    public PicoBuilder withNamedFieldInjection() {
        addInjector(namedField());
        return this;
    }

    public PicoBuilder withCaching() {
        behaviors.push(caching());
        return this;
    }

    public PicoBuilder withComponentFactory(final ComponentFactory componentFactory) {
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

    public PicoBuilder withBehaviors(final Behavior... factories) {
        for (Behavior componentFactory : factories) {
            behaviors.push(componentFactory);
        }
        return this;
    }

    public PicoBuilder implementedBy(final Class<? extends MutablePicoContainer> containerClass) {
        mpcClass = containerClass;
        return this;
    }

    public PicoBuilder withMonitor(final ComponentMonitor monitor) {
        this.monitor = monitor;
        monitorClass = null;
        return this;
    }

    public PicoBuilder withComponentFactory(final Class<? extends ComponentFactory> componentFactoryClass) {
        behaviors.push(componentFactoryClass);
        return this;
    }

    public PicoBuilder withCustomContainerComponent(final Object containerDependency) {
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
        addInjector(new MethodInjection());
        return this;
    }

    public PicoBuilder addChildToParent() {
        addChildToParent =  true;
        return this;
    }

    protected void addInjector(final InjectionType injectionType) {
        injectors.add(injectionType);
    }
}
