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

import org.picocontainer.adapters.AbstractAdapter;
import org.picocontainer.adapters.InstanceAdapter;
import org.picocontainer.behaviors.AbstractBehaviorFactory;
import org.picocontainer.behaviors.AdaptingBehavior;
import org.picocontainer.behaviors.Cached;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.behaviors.HiddenImplementation;
import org.picocontainer.containers.AbstractDelegatingMutablePicoContainer;
import org.picocontainer.containers.AbstractDelegatingPicoContainer;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.containers.ImmutablePicoContainer;
import org.picocontainer.converters.BuiltInConverters;
import org.picocontainer.converters.ConvertsNothing;
import org.picocontainer.injectors.AbstractInjector;
import org.picocontainer.injectors.AdaptingInjection;
import org.picocontainer.injectors.FactoryInjector;
import org.picocontainer.lifecycle.DefaultLifecycleState;
import org.picocontainer.lifecycle.LifecycleState;
import org.picocontainer.lifecycle.StartableLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.parameters.DefaultConstructorParameter;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * <p/>
 * The Standard {@link PicoContainer}/{@link MutablePicoContainer} implementation.
 * Constructing a container c with a parent p container will cause c to look up components
 * in p if they cannot be found inside c itself.
 * </p>
 * <p/>
 * Using {@link Class} objects as keys to the various registerXXX() methods makes
 * a subtle semantic difference:
 * </p>
 * <p/>
 * If there are more than one registered components of the same type and one of them are
 * registered with a {@link java.lang.Class} key of the corresponding type, this addComponent
 * will take precedence over other components during type resolution.
 * </p>
 * <p/>
 * Another place where keys that are classes make a subtle difference is in
 * {@link HiddenImplementation}.
 * </p>
 * <p/>
 * This implementation of {@link MutablePicoContainer} also supports
 * {@link ComponentMonitorStrategy}.
 * </p>
 *
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Jon Tirs&eacute;n
 * @author Thomas Heller
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class DefaultPicoContainer implements MutablePicoContainer, Converting, ComponentMonitorStrategy, Serializable  {

    private String name;

	/**
	 * Component factory instance.
	 */
	protected final ComponentFactory componentFactory;
    
	/**
	 * Parent picocontainer
	 */
    private PicoContainer parent;
    
    /**
     * All picocontainer children.
     */
    private final Set<PicoContainer> children = new HashSet<PicoContainer>();

    /**
     * Current state of the container.
     */
    private LifecycleState lifecycleState = new DefaultLifecycleState();

    /**
     * Keeps track of child containers started status.
     */
    private final Set<WeakReference<PicoContainer>> childrenStarted = new HashSet<WeakReference<PicoContainer>>();

    /**
     * Lifecycle strategy instance.
     */
    protected final LifecycleStrategy lifecycleStrategy;

    /**
     * Properties set at the container level, that will affect subsequent components added.
     */
    private final Properties containerProperties = new Properties();
    
    /**
     * Component monitor instance.  Receives event callbacks.
     */
    protected ComponentMonitor componentMonitor;

    /**
     * Map used for looking up component adapters by their key.
     */
	private final Map<Object, ComponentAdapter<?>> componentKeyToAdapterCache = new HashMap<Object, ComponentAdapter<?> >();


	private final List<ComponentAdapter<?>> componentAdapters = new ArrayList<ComponentAdapter<?>>();


	protected final List<ComponentAdapter<?>> orderedComponentAdapters = new ArrayList<ComponentAdapter<?>>();


    private transient IntoThreadLocal intoThreadLocal = new IntoThreadLocal();
    private Converters converters;


    /**
     * Creates a new container with a custom ComponentFactory and a parent container.
     * <p/>
     * <em>
     * Important note about caching: If you intend the components to be cached, you should pass
     * in a factory that creates {@link Cached} instances, such as for example
     * {@link Caching}. Caching can delegate to
     * other ComponentAdapterFactories.
     * </em>
     *
     * @param componentFactory the factory to use for creation of ComponentAdapters.
     * @param parent                  the parent container (used for component dependency lookups).
     */
    public DefaultPicoContainer(final ComponentFactory componentFactory, final PicoContainer parent) {
        this(componentFactory, new StartableLifecycleStrategy(new NullComponentMonitor()), parent, new NullComponentMonitor());
    }

    /**
     * Creates a new container with a custom ComponentFactory, LifecycleStrategy for instance registration,
     * and a parent container.
     * <p/>
     * <em>
     * Important note about caching: If you intend the components to be cached, you should pass
     * in a factory that creates {@link Cached} instances, such as for example
     * {@link Caching}. Caching can delegate to
     * other ComponentAdapterFactories.
     * </em>
     *
     * @param componentFactory the factory to use for creation of ComponentAdapters.
     * @param lifecycleStrategy
     *                                the lifecycle strategy chosen for registered
     *                                instance (not implementations!)
     * @param parent                  the parent container (used for component dependency lookups).
     */
    public DefaultPicoContainer(final ComponentFactory componentFactory,
                                final LifecycleStrategy lifecycleStrategy,
                                final PicoContainer parent) {
        this(componentFactory, lifecycleStrategy, parent, new NullComponentMonitor() );
    }

    public DefaultPicoContainer(final ComponentFactory componentFactory,
                                final LifecycleStrategy lifecycleStrategy,
                                final PicoContainer parent, final ComponentMonitor componentMonitor) {
        if (componentFactory == null) {
			throw new NullPointerException("componentFactory");
		}
        if (lifecycleStrategy == null) {
			throw new NullPointerException("lifecycleStrategy");
		}
        this.componentFactory = componentFactory;
        this.lifecycleStrategy = lifecycleStrategy;
        this.parent = parent;
        if (parent != null && !(parent instanceof EmptyPicoContainer)) {
            this.parent = new ImmutablePicoContainer(parent);
        }
        this.componentMonitor = componentMonitor;
    }

    /**
     * Creates a new container with the AdaptingInjection using a
     * custom ComponentMonitor
     *
     * @param monitor the ComponentMonitor to use
     * @param parent  the parent container (used for component dependency lookups).
     */
    public DefaultPicoContainer(final ComponentMonitor monitor, final PicoContainer parent) {
        this(new AdaptingBehavior(), new StartableLifecycleStrategy(monitor), parent, monitor);
    }

    /**
     * Creates a new container with the AdaptingInjection using a
     * custom ComponentMonitor and lifecycle strategy
     *
     * @param monitor           the ComponentMonitor to use
     * @param lifecycleStrategy the lifecycle strategy to use.
     * @param parent            the parent container (used for component dependency lookups).
     */
    public DefaultPicoContainer(final ComponentMonitor monitor, final LifecycleStrategy lifecycleStrategy, final PicoContainer parent) {
        this(new AdaptingBehavior(), lifecycleStrategy, parent, monitor);
    }

    /**
     * Creates a new container with the AdaptingInjection using a
     * custom lifecycle strategy
     *
     * @param lifecycleStrategy the lifecycle strategy to use.
     * @param parent            the parent container (used for component dependency lookups).
     */
    public DefaultPicoContainer(final LifecycleStrategy lifecycleStrategy, final PicoContainer parent) {
        this(new NullComponentMonitor(), lifecycleStrategy, parent);
    }


    /**
     * Creates a new container with a custom ComponentFactory and no parent container.
     *
     * @param componentFactory the ComponentFactory to use.
     */
    public DefaultPicoContainer(final ComponentFactory componentFactory) {
        this(componentFactory, null);
    }

    /**
     * Creates a new container with the AdaptingInjection using a
     * custom ComponentMonitor
     *
     * @param monitor the ComponentMonitor to use
     */
    public DefaultPicoContainer(final ComponentMonitor monitor) {
        this(monitor, new StartableLifecycleStrategy(monitor), null);
    }

    /**
     * Creates a new container with a (caching) {@link AdaptingInjection}
     * and a parent container.
     *
     * @param parent the parent container (used for component dependency lookups).
     */
    public DefaultPicoContainer(final PicoContainer parent) {
        this(new AdaptingBehavior(), parent);
    }

    /** Creates a new container with a {@link AdaptingBehavior} and no parent container. */
    public DefaultPicoContainer() {
        this(new AdaptingBehavior(), null);
    }

    /** {@inheritDoc} **/
    public Collection<ComponentAdapter<?>> getComponentAdapters() {
        return Collections.unmodifiableList(getModifiableComponentAdapterList());
    }


    /** {@inheritDoc} **/
    public final ComponentAdapter<?> getComponentAdapter(final Object componentKey) {
        ComponentAdapter<?> adapter = getComponentKeyToAdapterCache().get(componentKey);
        if (adapter == null && parent != null) {
            adapter = getParent().getComponentAdapter(componentKey);
            if (adapter != null) {
                adapter = new KnowsContainerAdapter(adapter, getParent());
            }
        }
        if (adapter == null) {
            Object inst = componentMonitor.noComponentFound(this, componentKey);
            if (inst != null) {
                adapter = new LateInstance(componentKey, inst);
            }
        }
        return adapter;
    }

    public static class LateInstance extends AbstractAdapter {
        private final Object instance;
        private LateInstance(Object componentKey, Object instance) {
            super(componentKey, instance.getClass());
            this.instance = instance;
        }

        public Object getComponentInstance() {
            return instance;
        }

        public Object getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            return instance;
        }

        public void verify(PicoContainer container) throws PicoCompositionException {
        }

        public String getDescriptor() {
            return "LateInstance";
        }
    }

    public static class KnowsContainerAdapter<T> implements ComponentAdapter<T> {
        private final ComponentAdapter<T> ca;
        private final PicoContainer ctr;

        public KnowsContainerAdapter(ComponentAdapter<T> ca, PicoContainer ctr) {
            this.ca = ca;
            this.ctr = ctr;
        }

        public T getComponentInstance(Type into) throws PicoCompositionException {
            return getComponentInstance(ctr, into);
        }

        public Object getComponentKey() {
            return ca.getComponentKey();
        }

        public Class<? extends T> getComponentImplementation() {
            return ca.getComponentImplementation();
        }

        public T getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            return ca.getComponentInstance(container, into);
        }

        public void verify(PicoContainer container) throws PicoCompositionException {
            ca.verify(container);
        }

        public void accept(PicoVisitor visitor) {
            ca.accept(visitor);
        }

        public ComponentAdapter getDelegate() {
            return ca.getDelegate();
        }

        public <U extends ComponentAdapter> U findAdapterOfType(Class<U> adapterType) {
            return ca.findAdapterOfType(adapterType);
        }

        public String getDescriptor() {
            return null;
        }
    }

    /** {@inheritDoc} **/
    public <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final NameBinding componentNameBinding) {
        return getComponentAdapter(componentType, componentNameBinding, null);
    }

    /** {@inheritDoc} **/
    private <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final NameBinding componentNameBinding, final Class<? extends Annotation> binding) {
        // See http://jira.codehaus.org/secure/ViewIssue.jspa?key=PICO-115
        ComponentAdapter<?> adapterByKey = getComponentAdapter(componentType);
        if (adapterByKey != null) {
            return typeComponentAdapter(adapterByKey);
        }

        List<ComponentAdapter<T>> found = binding == null ? getComponentAdapters(componentType) : getComponentAdapters(componentType, binding);

        if (found.size() == 1) {
            return found.get(0);
        } else if (found.isEmpty()) {
            if (parent != null) {
                return getParent().getComponentAdapter(componentType, componentNameBinding);
            } else {
                return null;
            }
        } else {
            if (componentNameBinding != null) {
                String parameterName = componentNameBinding.getName();
                if (parameterName != null) {
                    ComponentAdapter<?> ca = getComponentAdapter(parameterName);
                    if (ca != null && componentType.isAssignableFrom(ca.getComponentImplementation())) {
                        return typeComponentAdapter(ca);
                    }
                }
            }
            Class<?>[] foundClasses = new Class[found.size()];
            for (int i = 0; i < foundClasses.length; i++) {
                foundClasses[i] = found.get(i).getComponentImplementation();
            }

            throw new AbstractInjector.AmbiguousComponentResolutionException(componentType, foundClasses);
        }
    }

    /** {@inheritDoc} **/
    public <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final Class<? extends Annotation> binding) {
        // 1
        return getComponentAdapter(componentType, null, binding);
    }

    /** {@inheritDoc} **/
    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Class<T> componentType) {
        return getComponentAdapters(componentType,  null);
    }

    /** {@inheritDoc} **/
    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Class<T> componentType, final Class<? extends Annotation> binding) {
        if (componentType == null) {
            return Collections.emptyList();
        }
        List<ComponentAdapter<T>> found = new ArrayList<ComponentAdapter<T>>();
        for (ComponentAdapter<?> componentAdapter : getComponentAdapters()) {
            Object k = componentAdapter.getComponentKey();

            if (componentType.isAssignableFrom(componentAdapter.getComponentImplementation()) &&
                (!(k instanceof BindKey) || (k instanceof BindKey && (((BindKey<?>)k).getAnnotation() == null || binding == null ||
                                                                      ((BindKey<?>)k).getAnnotation() == binding)))) {
                found.add((ComponentAdapter<T>)typeComponentAdapter(componentAdapter));
            }
        }
        return found;
    }

    protected MutablePicoContainer addAdapterInternal(ComponentAdapter<?> componentAdapter) {
        Object componentKey = componentAdapter.getComponentKey();
        if (getComponentKeyToAdapterCache().containsKey(componentKey)) {
            throw new PicoCompositionException("Duplicate Keys not allowed. Duplicate for '" + componentKey + "'");
        }
        getModifiableComponentAdapterList().add(componentAdapter);
        getComponentKeyToAdapterCache().put(componentKey, componentAdapter);
        return this;
    }

    /**
     * {@inheritDoc}
     * This method can be used to override the ComponentAdapter created by the {@link ComponentFactory}
     * passed to the constructor of this container.
     */
    public MutablePicoContainer addAdapter(final ComponentAdapter<?> componentAdapter) {
        return addAdapter(componentAdapter,  this.containerProperties);
    }

    /** {@inheritDoc} **/
    public MutablePicoContainer addAdapter(final ComponentAdapter<?> componentAdapter, final Properties properties) {
        Properties tmpProperties = (Properties)properties.clone();
        if (AbstractBehaviorFactory.removePropertiesIfPresent(tmpProperties, Characteristics.NONE) == false && componentFactory instanceof BehaviorFactory) {
            MutablePicoContainer container = addAdapterInternal(((BehaviorFactory)componentFactory).addComponentAdapter(
                componentMonitor,
                lifecycleStrategy,
                tmpProperties,
                componentAdapter));
            throwIfPropertiesLeft(tmpProperties);
            return container;
        } else {
            return addAdapterInternal(componentAdapter);
        }

    }


    /** {@inheritDoc} **/
    public <T> ComponentAdapter<T> removeComponent(final Object componentKey) {
        lifecycleState.removingComponent();

        ComponentAdapter<T> adapter = (ComponentAdapter<T>) getComponentKeyToAdapterCache().remove(componentKey);
        getModifiableComponentAdapterList().remove(adapter);
        getOrderedComponentAdapters().remove(adapter);    	
        return adapter;
    }

    /**
     * {@inheritDoc}
     * The returned ComponentAdapter will be an {@link org.picocontainer.adapters.InstanceAdapter}.
     */
    public MutablePicoContainer addComponent(final Object implOrInstance) {
        return addComponent(implOrInstance, this.containerProperties);
    }

    private MutablePicoContainer addComponent(final Object implOrInstance, final Properties props) {
        Class<?> clazz;
        if (implOrInstance instanceof String) {
            return addComponent(implOrInstance, implOrInstance);
        }
        if (implOrInstance instanceof Class) {
            clazz = (Class<?>)implOrInstance;
        } else {
            clazz = implOrInstance.getClass();
        }
        return addComponent(clazz, implOrInstance, props);
    }


    public MutablePicoContainer addConfig(final String name, final Object val) {
        return addAdapterInternal(new InstanceAdapter<Object>(name, val, lifecycleStrategy, componentMonitor));
    }


    /**
     * {@inheritDoc}
     * The returned ComponentAdapter will be instantiated by the {@link ComponentFactory}
     * passed to the container's constructor.
     */
    public MutablePicoContainer addComponent(final Object componentKey,
                                             final Object componentImplementationOrInstance,
                                             final Parameter... parameters) {
        return this.addComponent(componentKey, componentImplementationOrInstance, this.containerProperties, parameters);
    }

    private MutablePicoContainer addComponent(final Object componentKey,
                                             final Object componentImplementationOrInstance,
                                             final Properties properties,
                                             Parameter... parameters) {
        if (parameters != null && parameters.length == 0) {
            parameters = null; // backwards compatibility!  solve this better later - Paul
        }
        
        //New replacement for Parameter.ZERO.
        if (parameters != null && parameters.length == 1 && DefaultConstructorParameter.INSTANCE.equals(parameters[0])) {
        	parameters = new Parameter[0];
        }
        
        if (componentImplementationOrInstance instanceof Class) {
            Properties tmpProperties = (Properties) properties.clone();
            ComponentAdapter<?> adapter = componentFactory.createComponentAdapter(componentMonitor,
                                                                                               lifecycleStrategy,
                                                                                               tmpProperties,
                                                                                               componentKey,
                                                                                               (Class<?>)componentImplementationOrInstance,
                                                                                               parameters);
            AbstractBehaviorFactory.removePropertiesIfPresent(tmpProperties, Characteristics.USE_NAMES);
            throwIfPropertiesLeft(tmpProperties);
            if (lifecycleState.isStarted()) {
                addAdapterIfStartable(adapter);
                potentiallyStartAdapter(adapter);
            }
            return addAdapterInternal(adapter);
        } else {
            ComponentAdapter<?> adapter =
                new InstanceAdapter<Object>(componentKey, componentImplementationOrInstance, lifecycleStrategy, componentMonitor);
            if (lifecycleState.isStarted()) {
                addAdapterIfStartable(adapter);
                potentiallyStartAdapter(adapter);
            }
            return addAdapter(adapter, properties);
        }
    }

    private void throwIfPropertiesLeft(final Properties tmpProperties) {
        if(tmpProperties.size() > 0) {
            throw new PicoCompositionException("Unprocessed Characteristics:" + tmpProperties +", please refer to http://picocontainer.org/unprocessed-properties-help.html");
        }
    }

    private void addOrderedComponentAdapter(final ComponentAdapter<?> componentAdapter) {
        if (!getOrderedComponentAdapters().contains(componentAdapter)) {
            getOrderedComponentAdapters().add(componentAdapter);
        }
    }

    public List<Object> getComponents() throws PicoException {
        return getComponents(Object.class);
    }

    public <T> List<T> getComponents(final Class<T> componentType) {
        if (componentType == null) {
            return Collections.emptyList();
        }

        Map<ComponentAdapter<T>, T> adapterToInstanceMap = new HashMap<ComponentAdapter<T>, T>();
        for (ComponentAdapter<?> componentAdapter : getModifiableComponentAdapterList()) {
            if (componentType.isAssignableFrom(componentAdapter.getComponentImplementation())) {
                ComponentAdapter<T> typedComponentAdapter = typeComponentAdapter(componentAdapter);
                T componentInstance = getLocalInstance(typedComponentAdapter);

                adapterToInstanceMap.put(typedComponentAdapter, componentInstance);
            }
        }
        List<T> result = new ArrayList<T>();
        for (ComponentAdapter<?> componentAdapter : getOrderedComponentAdapters()) {
            final T componentInstance = adapterToInstanceMap.get(componentAdapter);
            if (componentInstance != null) {
                // may be null in the case of the "implicit" addAdapter
                // representing "this".
                result.add(componentInstance);
            }
        }
        return result;
    }

    private <T> T getLocalInstance(final ComponentAdapter<T> typedComponentAdapter) {
        T componentInstance = typedComponentAdapter.getComponentInstance(this, ComponentAdapter.NOTHING.class);

        // This is to ensure all are added. (Indirect dependencies will be added
        // from InstantiatingComponentAdapter).
        addOrderedComponentAdapter(typedComponentAdapter);

        return componentInstance;
    }

    @SuppressWarnings({ "unchecked" })
    private static <T> ComponentAdapter<T> typeComponentAdapter(final ComponentAdapter<?> componentAdapter) {
        return (ComponentAdapter<T>)componentAdapter;
    }


    public Object getComponent(Object componentKeyOrType) {
        return getComponent(componentKeyOrType, null, ComponentAdapter.NOTHING.class);
    }

    public Object getComponentInto(final Object componentKeyOrType, Type into) {
        return getComponent(componentKeyOrType, null, into);
    }

    public <T> T getComponent(Class<T> componentType) {
        Object o = getComponent((Object) componentType, null, ComponentAdapter.NOTHING.class);
        return componentType.cast(o);
    }

    public Object getComponent(final Object componentKeyOrType, final Class<? extends Annotation> annotation, Type into) {
        ComponentAdapter<?> componentAdapter;
        Object component;
        if (annotation != null) {
            componentAdapter = getComponentAdapter((Class<?>)componentKeyOrType, annotation);
            component = componentAdapter == null ? null : getInstance(componentAdapter, null, into);
        } else if (componentKeyOrType instanceof Class) {
            componentAdapter = getComponentAdapter((Class<?>)componentKeyOrType, (NameBinding) null);
            component = componentAdapter == null ? null : getInstance(componentAdapter, (Class<?>)componentKeyOrType, into);
        } else {
            componentAdapter = getComponentAdapter(componentKeyOrType);
            component = componentAdapter == null ? null : getInstance(componentAdapter, null, into);
        }
        return decorateComponent(component, componentAdapter);
    }

    /**
     * This is invoked when getComponent(..) is called.  It allows extendees to decorate a
     * component before it is returned to the caller.
     * @param component the component that will be returned for getComponent(..)
     * @param componentAdapter the component adapter that made that component
     * @return the component (the same as that passed in by default)
     */
    protected Object decorateComponent(Object component, ComponentAdapter<?> componentAdapter) {
        if (componentAdapter instanceof ComponentLifecycle<?>
                && lifecycleStrategy.isLazy(componentAdapter) // is Lazy
                && !((ComponentLifecycle<?>) componentAdapter).isStarted()) {
            ((ComponentLifecycle<?>)componentAdapter).start(this);
        }
        return component;
    }

    public <T> T getComponent(final Class<T> componentType, final Class<? extends Annotation> binding, Type into) {
        Object o = getComponent((Object)componentType, binding, into);
        return componentType.cast(o);
    }

    public <T> T getComponent(Class<T> componentType, Class<? extends Annotation> binding) {
        return getComponent(componentType, binding, ComponentAdapter.NOTHING.class);
    }

    public <T> T getComponentInto(final Class<T> componentType, Type into) {
        Object o = getComponent((Object)componentType, null, into);
        return componentType.cast(o);
    }

    private Object getInstance(final ComponentAdapter<?> componentAdapter, Class componentKey, Type into) {
        // check whether this is our adapter
        // we need to check this to ensure up-down dependencies cannot be followed
        final boolean isLocal = getModifiableComponentAdapterList().contains(componentAdapter);

        if (isLocal || componentAdapter instanceof LateInstance) {
            Object instance;
            try {
                if (componentAdapter instanceof FactoryInjector) {
                    instance = ((FactoryInjector) componentAdapter).getComponentInstance(this, into);
                } else {
                    instance = componentAdapter.getComponentInstance(this, into);
                }
            } catch (AbstractInjector.CyclicDependencyException e) {
                if (parent != null) {
                    instance = getParent().getComponentInto(componentAdapter.getComponentKey(), into);
                    if (instance != null) {
                        return instance;
                    }
                }
                throw e;
            }
            addOrderedComponentAdapter(componentAdapter);

            return instance;
        } else if (parent != null) {
            return getParent().getComponentInto(componentAdapter.getComponentKey(), into);
        }

        return null;
    }


    /** {@inheritDoc} **/
    public PicoContainer getParent() {
        return parent;
    }

    /** {@inheritDoc} **/
    public <T> ComponentAdapter<T> removeComponentByInstance(final T componentInstance) {
        for (ComponentAdapter<?> componentAdapter : getModifiableComponentAdapterList()) {
            if (getLocalInstance(componentAdapter).equals(componentInstance)) {
                return removeComponent(componentAdapter.getComponentKey());
            }
        }
        return null;
    }

    /**
     * Start the components of this PicoContainer and all its logical child containers.
     * The starting of the child container is only attempted if the parent
     * container start successfully.  The child container for which start is attempted
     * is tracked so that upon stop, only those need to be stopped.
     * The lifecycle operation is delegated to the component adapter,
     * if it is an instance of {@link Behavior lifecycle manager}.
     * The actual {@link LifecycleStrategy lifecycle strategy} supported
     * depends on the concrete implementation of the adapter.
     *
     * @see Behavior
     * @see LifecycleStrategy
     * @see #makeChildContainer()
     * @see #addChildContainer(PicoContainer)
     * @see #removeChildContainer(PicoContainer)
     */
    public void start() {

        lifecycleState.starting();

        startAdapters();
        childrenStarted.clear();
        for (PicoContainer child : children) {
            childrenStarted.add(new WeakReference<PicoContainer>(child));
            if (child instanceof Startable) {
                ((Startable)child).start();
            }
        }
    }

    /**
     * Stop the components of this PicoContainer and all its logical child containers.
     * The stopping of the child containers is only attempted for those that have been
     * started, possibly not successfully.
     * The lifecycle operation is delegated to the component adapter,
     * if it is an instance of {@link Behavior lifecycle manager}.
     * The actual {@link LifecycleStrategy lifecycle strategy} supported
     * depends on the concrete implementation of the adapter.
     *
     * @see Behavior
     * @see LifecycleStrategy
     * @see #makeChildContainer()
     * @see #addChildContainer(PicoContainer)
     * @see #removeChildContainer(PicoContainer)
     */
    public void stop() {

        lifecycleState.stopping();

        for (PicoContainer child : children) {
            if (childStarted(child)) {
                if (child instanceof Startable) {
                    ((Startable)child).stop();
                }
            }
        }
        stopAdapters();
        lifecycleState.stopped();
    }

    /**
     * Checks the status of the child container to see if it's been started
     * to prevent IllegalStateException upon stop
     *
     * @param child the child PicoContainer
     *
     * @return A boolean, <code>true</code> if the container is started
     */
    private boolean childStarted(final PicoContainer child) {
    	for (WeakReference<PicoContainer> eachChild : childrenStarted) {
    		PicoContainer ref = eachChild.get();
    		if (ref == null) {
    			continue;
    		}
    		
    		if (child.equals(ref)) {
    			return true;
    		}
    	}
    	return false;
    }

    /**
     * Dispose the components of this PicoContainer and all its logical child containers.
     * The lifecycle operation is delegated to the component adapter,
     * if it is an instance of {@link Behavior lifecycle manager}.
     * The actual {@link LifecycleStrategy lifecycle strategy} supported
     * depends on the concrete implementation of the adapter.
     *
     * @see Behavior
     * @see LifecycleStrategy
     * @see #makeChildContainer()
     * @see #addChildContainer(PicoContainer)
     * @see #removeChildContainer(PicoContainer)
     */
    public void dispose() {
    	if (lifecycleState.isStarted()) {
    		stop();
    	}

        lifecycleState.disposing();

        for (PicoContainer child : children) {
            if (child instanceof MutablePicoContainer) {
                ((Disposable)child).dispose();
            }
        }
        disposeAdapters();

        lifecycleState.disposed();
    }

    public void setLifecycleState(LifecycleState lifecycleState) {
        this.lifecycleState = lifecycleState;
    }

    public MutablePicoContainer makeChildContainer() {
        DefaultPicoContainer pc = new DefaultPicoContainer(componentFactory, lifecycleStrategy, this, componentMonitor);
        addChildContainer(pc);
        return pc;
    }
    
    /**
     * Checks for identical references in the child container.  It doesn't
     * traverse an entire hierarchy, namely it simply checks for child containers
     * that are equal to the current container.
     * @param child
     */
    private void checkCircularChildDependencies(PicoContainer child) {
    	final String MESSAGE = "Cannot have circular dependency between parent %s and child: %s";
    	if (child == this) {
    		throw new IllegalArgumentException(String.format(MESSAGE,this,child));
    	}
    	
    	//Todo: Circular Import Dependency on AbstractDelegatingPicoContainer
    	if (child instanceof AbstractDelegatingPicoContainer) {
    		AbstractDelegatingPicoContainer delegateChild = (AbstractDelegatingPicoContainer) child;
    		while(delegateChild != null) {
    			PicoContainer delegateInstance = delegateChild.getDelegate();
    			if (this == delegateInstance) {
					throw new IllegalArgumentException(String.format(MESSAGE,this,child));
    			}
    			if (delegateInstance instanceof AbstractDelegatingPicoContainer) {
    				delegateChild = (AbstractDelegatingPicoContainer) delegateInstance;
    			} else {
    				delegateChild = null;
    			}
    		}
    	}
    	
    }

    public MutablePicoContainer addChildContainer(final PicoContainer child) {
    	checkCircularChildDependencies(child);
    	if (children.add(child)) {
            // TODO Should only be added if child container has also be started
            if (lifecycleState.isStarted()) {
                childrenStarted.add(new WeakReference<PicoContainer>(child));
            }
        }
        return this;
    }

    public boolean removeChildContainer(final PicoContainer child) {
        final boolean result = children.remove(child);
        WeakReference<PicoContainer> foundRef = null;
        for (WeakReference<PicoContainer> eachChild : childrenStarted) {
        	PicoContainer ref = eachChild.get();
        	if (ref.equals(child)) {
        		foundRef = eachChild;
        		break;
        	}
        }
        
        if (foundRef != null) {
        	childrenStarted.remove(foundRef);
        }
        
        return result;
    }

    public MutablePicoContainer change(final Properties... properties) {
        for (Properties c : properties) {
            Enumeration<String> e = (Enumeration<String>) c.propertyNames();
            while (e.hasMoreElements()) {
                String s = e.nextElement();
                containerProperties.setProperty(s,c.getProperty(s));
            }
        }
        return this;
    }

    public MutablePicoContainer as(final Properties... properties) {
        return new AsPropertiesPicoContainer(properties);
    }

    public void accept(final PicoVisitor visitor) {
    	
    	//TODO Pico 3 : change accept signatures to allow abort at any point in the traversal.
        boolean shouldContinue = visitor.visitContainer(this);
        if (!shouldContinue) {
        	return;
        }
        
        
        componentFactory.accept(visitor); // will cascade through behaviors
        final List<ComponentAdapter<?>> componentAdapters = new ArrayList<ComponentAdapter<?>>(getComponentAdapters());
        for (ComponentAdapter<?> componentAdapter : componentAdapters) {
            componentAdapter.accept(visitor);
        }
        final List<PicoContainer> allChildren = new ArrayList<PicoContainer>(children);
        for (PicoContainer child : allChildren) {
            child.accept(visitor);
        }
    }

    /**
     * Changes monitor in the ComponentFactory, the component adapters
     * and the child containers, if these support a ComponentMonitorStrategy.
     * {@inheritDoc}
     */
    public void changeMonitor(final ComponentMonitor monitor) {
        this.componentMonitor = monitor;
        if (lifecycleStrategy instanceof ComponentMonitorStrategy) {
            ((ComponentMonitorStrategy)lifecycleStrategy).changeMonitor(monitor);
        }
        for (ComponentAdapter<?> adapter : getModifiableComponentAdapterList()) {
            if (adapter instanceof ComponentMonitorStrategy) {
                ((ComponentMonitorStrategy)adapter).changeMonitor(monitor);
            }
        }
        for (PicoContainer child : children) {
            if (child instanceof ComponentMonitorStrategy) {
                ((ComponentMonitorStrategy)child).changeMonitor(monitor);
            }
        }
    }

    /**
     * Returns the first current monitor found in the ComponentFactory, the component adapters
     * and the child containers, if these support a ComponentMonitorStrategy.
     * {@inheritDoc}
     *
     * @throws PicoCompositionException if no component monitor is found in container or its children
     */
    public ComponentMonitor currentMonitor() {
        return componentMonitor;
    }

    /**
     * {@inheritDoc}
     * Loops over all component adapters and invokes
     * start(PicoContainer) method on the ones which are LifecycleManagers
     */
    private void startAdapters() {
        Collection<ComponentAdapter<?>> adapters = getComponentAdapters();
        for (ComponentAdapter<?> adapter : adapters) {
            addAdapterIfStartable(adapter);
        }
        adapters = getOrderedComponentAdapters();
        // clone the adapters
        List<ComponentAdapter<?>> adaptersClone = new ArrayList<ComponentAdapter<?>>(adapters);
        for (final ComponentAdapter<?> adapter : adaptersClone) {
            potentiallyStartAdapter(adapter);
        }
    }

    protected void potentiallyStartAdapter(ComponentAdapter<?> adapter) {
        if (adapter instanceof ComponentLifecycle) {
            if (!lifecycleStrategy.isLazy(adapter)) {
                ((ComponentLifecycle<?>)adapter).start(this);
            }
        }
    }

    private void addAdapterIfStartable(ComponentAdapter<?> adapter) {
        if (adapter instanceof ComponentLifecycle) {
            ComponentLifecycle<?> componentLifecycle = (ComponentLifecycle<?>)adapter;
            if (componentLifecycle.componentHasLifecycle()) {
                // create an instance, it will be added to the ordered CA list
                instantiateComponentAsIsStartable(adapter);
                addOrderedComponentAdapter(adapter);
            }
        }
    }

    protected void instantiateComponentAsIsStartable(ComponentAdapter<?> adapter) {
        if (!lifecycleStrategy.isLazy(adapter)) {
            adapter.getComponentInstance(DefaultPicoContainer.this, ComponentAdapter.NOTHING.class);
        }
    }

    /**
     * {@inheritDoc}
     * Loops over started component adapters (in inverse order) and invokes
     * stop(PicoContainer) method on the ones which are LifecycleManagers
     */
    private void stopAdapters() {
        for (int i = getOrderedComponentAdapters().size() - 1; 0 <= i; i--) {
            ComponentAdapter<?> adapter = getOrderedComponentAdapters().get(i);
            if (adapter instanceof ComponentLifecycle) {
                ComponentLifecycle<?> componentLifecycle = (ComponentLifecycle<?>)adapter;
                if (componentLifecycle.componentHasLifecycle() && componentLifecycle.isStarted()) {
                    componentLifecycle.stop(DefaultPicoContainer.this);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * Loops over all component adapters (in inverse order) and invokes
     * dispose(PicoContainer) method on the ones which are LifecycleManagers
     */
    private void disposeAdapters() {
        for (int i = getOrderedComponentAdapters().size() - 1; 0 <= i; i--) {
            ComponentAdapter<?> adapter = getOrderedComponentAdapters().get(i);
            if (adapter instanceof ComponentLifecycle) {
                ComponentLifecycle<?> componentLifecycle = (ComponentLifecycle<?>)adapter;
                componentLifecycle.dispose(DefaultPicoContainer.this);
            }
        }
    }



	/**
	 * @return the orderedComponentAdapters
	 */
	protected List<ComponentAdapter<?>> getOrderedComponentAdapters() {
		return orderedComponentAdapters;
	}

	/**
	 * @return the componentKeyToAdapterCache
	 */
	protected Map<Object, ComponentAdapter<?>> getComponentKeyToAdapterCache() {
		return componentKeyToAdapterCache;
	}

	/**
	 * @return the componentAdapters
	 */
	protected List<ComponentAdapter<?>> getModifiableComponentAdapterList() {
		return componentAdapters;
	}

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("%s:%d<%s", (name != null ? name : super.toString()), this.componentAdapters.size(), (parent != null ? parent.toString() : "|"));
    }

    /**
     * If this container has a set of converters, then return it.
     * If it does not, and the parent (or their parent ..) does, use that
     * If they do not, return a NullObject implementation (ConversNothing)
     * @return the converters
     */    
    public synchronized Converters getConverters() {
        if (converters == null) {
            if (parent == null || (parent instanceof Converting && ((Converting) parent).getConverters() instanceof ConvertsNothing)) {
                converters = new BuiltInConverters();
            } else {
                return ((Converting) parent).getConverters();
            }
        }
        return converters;
    }

    private class AsPropertiesPicoContainer extends AbstractDelegatingMutablePicoContainer {

		private final Properties properties;

        public AsPropertiesPicoContainer(final Properties... props) {
            super(DefaultPicoContainer.this);
            properties = (Properties) containerProperties.clone();
            for (Properties c : props) {
                Enumeration<?> e = c.propertyNames();
                while (e.hasMoreElements()) {
                    String s = (String)e.nextElement();
                    properties.setProperty(s,c.getProperty(s));
                }
            }
        }

        @Override
        @SuppressWarnings("unused")
        public MutablePicoContainer as( Properties... props) {
            throw new PicoCompositionException("Syntax 'as(FOO).as(BAR)' not allowed, do 'as(FOO, BAR)' instead");
        }

        @Override
		public MutablePicoContainer makeChildContainer() {
            return getDelegate().makeChildContainer();
        }

        @Override
		public MutablePicoContainer addComponent(final Object componentKey,
                                                 final Object componentImplementationOrInstance,
                                                 final Parameter... parameters) throws PicoCompositionException {
            return DefaultPicoContainer.this.addComponent(componentKey,
                                      componentImplementationOrInstance,
                                      properties,
                                      parameters);
        }

        @Override
		public MutablePicoContainer addComponent(final Object implOrInstance) throws PicoCompositionException {
            return DefaultPicoContainer.this.addComponent(implOrInstance, properties);
        }

        @Override
		public MutablePicoContainer addAdapter(final ComponentAdapter<?> componentAdapter) throws PicoCompositionException {
            return DefaultPicoContainer.this.addAdapter(componentAdapter, properties);
        }
    }

    private static class IntoThreadLocal extends ThreadLocal<Type> implements Serializable {
        protected Type initialValue() {
            return ComponentAdapter.NOTHING.class;
        }
    }
}
