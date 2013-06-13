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

import com.googlecode.jtype.Generic;
import org.picocontainer.adapters.AbstractAdapter;
import org.picocontainer.adapters.InstanceAdapter;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.behaviors.AdaptingBehavior;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.containers.AbstractDelegatingMutablePicoContainer;
import org.picocontainer.containers.AbstractDelegatingPicoContainer;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.containers.ImmutablePicoContainer;
import org.picocontainer.converters.BuiltInConverters;
import org.picocontainer.converters.ConvertsNothing;
import org.picocontainer.injectors.AbstractInjector;
import org.picocontainer.injectors.AdaptingInjection;
import org.picocontainer.injectors.FactoryInjector;
import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.lifecycle.DefaultLifecycleState;
import org.picocontainer.lifecycle.LifecycleState;
import org.picocontainer.lifecycle.StartableLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.DefaultConstructorParameter;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

import javax.inject.Provider;
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
 * {@link org.picocontainer.behaviors.ImplementationHiding.HiddenImplementation}.
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
public class DefaultPicoContainer implements MutablePicoContainer, Converting, ComponentMonitorStrategy, Serializable {

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
    protected final LifecycleStrategy lifecycle;

    /**
     * Properties set at the container level, that will affect subsequent components added.
     */
    private final Properties containerProperties = new Properties();

    /**
     * Component monitor instance.  Receives event callbacks.
     */
    protected ComponentMonitor monitor;

    /**
     * Map used for looking up component adapters by their key.
     */
    private final Map<Object, ComponentAdapter<?>> keyToAdapterCache = new HashMap<Object, ComponentAdapter<?>>();


    private final List<ComponentAdapter<?>> componentAdapters = new ArrayList<ComponentAdapter<?>>();

    protected final List<ComponentAdapter<?>> orderedComponentAdapters = new ArrayList<ComponentAdapter<?>>();

    private Converters converters;

    /**
     * Creates a new container with a custom ComponentFactory and no parent container.
     *
     * @param componentFactory the ComponentFactory to use.
     */
    public DefaultPicoContainer(final ComponentFactory componentFactory) {
        this((PicoContainer) null, componentFactory);
    }

    /**
     * Creates a new container with the AdaptingInjection using a
     * custom ComponentMonitor
     *
     * @param monitor the ComponentMonitor to use
     */
    public DefaultPicoContainer(final ComponentMonitor monitor) {
        this((PicoContainer) null, new StartableLifecycleStrategy(monitor), monitor);
    }

    /**
     * Creates a new container with a (caching) {@link AdaptingInjection}
     * and a parent container.
     *
     * @param parent the parent container (used for component dependency lookups).
     */
    public DefaultPicoContainer(final PicoContainer parent) {
        this(parent, new AdaptingBehavior());
    }

    /**
     * Creates a new container with a {@link AdaptingBehavior} and no parent container.
     */
    public DefaultPicoContainer() {
        this((PicoContainer) null, new AdaptingBehavior());
    }

    /**
     * Creates a new container with a custom ComponentFactory and a parent container.
     * <p/>
     * <em>
     * Important note about caching: If you intend the components to be cached, you should pass
     * in a factory that creates {@link org.picocontainer.behaviors.Caching.Cached} instances, such as for example
     * {@link Caching}. Caching can delegate to
     * other ComponentAdapterFactories.
     * </em>
     *
     * @param parent           the parent container (used for component dependency lookups).
     * @param componentFactories the factory to use for creation of ComponentAdapters.
     */
    public DefaultPicoContainer(final PicoContainer parent, final ComponentFactory... componentFactories) {
        this(parent, new StartableLifecycleStrategy(new NullComponentMonitor()), new NullComponentMonitor(), componentFactories);
    }

    /**
     * Creates a new container with a custom ComponentFactory, LifecycleStrategy for instance registration,
     * and a parent container.
     * <p/>
     * <em>
     * Important note about caching: If you intend the components to be cached, you should pass
     * in a factory that creates {@link org.picocontainer.behaviors.Caching.Cached} instances, such as for example
     * {@link Caching}. Caching can delegate to
     * other ComponentAdapterFactories.
     * </em>
     *
     * @param parent            the parent container (used for component dependency lookups).
     * @param lifecycle the lifecycle strategy chosen for registered
     *                          instance (not implementations!)
     * @param componentFactories  the factory to use for creation of ComponentAdapters.
     */
    public DefaultPicoContainer(final PicoContainer parent, final LifecycleStrategy lifecycle, final ComponentFactory... componentFactories) {
        this(parent, lifecycle, new NullComponentMonitor(), componentFactories);
    }

    public DefaultPicoContainer(final PicoContainer parent, final LifecycleStrategy lifecycle, final ComponentMonitor monitor, final ComponentFactory... componentFactories) {
        if (componentFactories.length == 0) {
            throw new NullPointerException("at least one componentFactory");
        }
        int i = componentFactories.length -1;
        ComponentFactory componentFactory = componentFactories[i];
        while (i > 0) {
            try {
                componentFactory = ((Behavior) componentFactories[i-1]).wrap(componentFactory);
            } catch (ClassCastException e) {
                throw new PicoCompositionException("Check the order of the BehaviorFactories " +
                        "in the varargs list of ComponentFactories. Index " + (i-1)
                        + " ("+componentFactories[i-1].getClass().getName()+") should be a BehaviorFactory but is not.");
            }
            i--;
        }
        if (componentFactory == null) {
            throw new NullPointerException("one of the componentFactories");
        }
        if (lifecycle == null) {
            throw new NullPointerException("lifecycle");
        }
        this.componentFactory = componentFactory;
        this.lifecycle = lifecycle;
        this.parent = parent;
        if (parent != null && !(parent instanceof EmptyPicoContainer)) {
            this.parent = new ImmutablePicoContainer(parent);
        }
        this.monitor = monitor;
    }

    /**
     * Creates a new container with the AdaptingInjection using a
     * custom ComponentMonitor
     *
     * @param parent  the parent container (used for component dependency lookups).
     * @param monitor the ComponentMonitor to use
     */
    public DefaultPicoContainer(final PicoContainer parent, final ComponentMonitor monitor) {
        this(parent, new StartableLifecycleStrategy(monitor), monitor, new AdaptingBehavior());
    }

    /**
     * Creates a new container with the AdaptingInjection using a
     * custom ComponentMonitor and lifecycle strategy
     *
     * @param parent            the parent container (used for component dependency lookups).
     * @param lifecycle the lifecycle strategy to use.
     * @param monitor           the ComponentMonitor to use
     */
    public DefaultPicoContainer(final PicoContainer parent, final LifecycleStrategy lifecycle, final ComponentMonitor monitor) {
        this(parent, lifecycle, monitor, new AdaptingBehavior());
    }

    /**
     * Creates a new container with the AdaptingInjection using a
     * custom lifecycle strategy
     *
     * @param parent            the parent container (used for component dependency lookups).
     * @param lifecycle the lifecycle strategy to use.
     */
    public DefaultPicoContainer(final PicoContainer parent, final LifecycleStrategy lifecycle) {
        this(parent, lifecycle, new NullComponentMonitor());
    }

    /**
     * Creates a new container with a custom ComponentFactory and no parent container.
     *
     * @param componentFactories the ComponentFactory to use.
     */
    public DefaultPicoContainer(final ComponentFactory... componentFactories) {
        this(null, componentFactories);
    }

    /**
     * {@inheritDoc} *
     */
    public Collection<ComponentAdapter<?>> getComponentAdapters() {
        return Collections.unmodifiableList(getModifiableComponentAdapterList());
    }

    /**
     * {@inheritDoc}
     */
    public final ComponentAdapter<?> getComponentAdapter (Object key) {
        if (key instanceof Generic) {
            key = ((Generic) key).getType();
        }
        ComponentAdapter<?> adapter = getComponentKeyToAdapterCache().get(key);
        if (adapter == null && parent != null) {
            adapter = getParent().getComponentAdapter(key);
            if (adapter != null) {
                adapter = new KnowsContainerAdapter(adapter, getParent());
            }
        }
        if (adapter == null) {
            Object inst = monitor.noComponentFound(this, key);
            if (inst != null) {
                adapter = new LateInstance(key, inst);
            }
        }
        return adapter;
    }

    public static class LateInstance extends AbstractAdapter {
        private final Object instance;

        private LateInstance(Object key, Object instance) {
            super(key, instance.getClass());
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

    public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, NameBinding nameBinding) {
        return getComponentAdapter(Generic.get(componentType), nameBinding, null);
    }

    /**
     * {@inheritDoc} *
     */
    public <T> ComponentAdapter<T> getComponentAdapter(final Generic<T> componentType, final NameBinding nameBinding) {
        return getComponentAdapter(componentType, nameBinding, null);
    }

    /**
     * {@inheritDoc} *
     */
    private <T> ComponentAdapter<T> getComponentAdapter(final Generic<T> componentType, final NameBinding componentNameBinding, final Class<? extends Annotation> binding) {
        // See http://jira.codehaus.org/secure/ViewIssue.jspa?key=PICO-115
        ComponentAdapter<T> adapterByKey = (ComponentAdapter<T>) getComponentAdapter(componentType);
        if (adapterByKey != null) {
            return adapterByKey;
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
                    if (ca != null && JTypeHelper.isAssignableFrom(componentType, ca.getComponentImplementation())) {
                        return (ComponentAdapter<T>) ca;
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

    /**
     * {@inheritDoc} *
     */
    public <T> ComponentAdapter<T> getComponentAdapter(final Class<T> componentType, final Class<? extends Annotation> binding) {
        return getComponentAdapter(Generic.get(componentType), null, binding);
    }

    /**
     * {@inheritDoc} *
     */
    public <T> ComponentAdapter<T> getComponentAdapter(final Generic<T> componentType, final Class<? extends Annotation> binding) {
        return getComponentAdapter(componentType, null, binding);
    }

    /**
     * {@inheritDoc} *
     */
    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Class<T> componentType) {
        return getComponentAdapters(Generic.get(componentType), null);
    }

    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Generic<T> componentType) {
        return getComponentAdapters(componentType, null);
    }

    /**
     * {@inheritDoc} *
     */
    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Class<T> componentType, final Class<? extends Annotation> binding) {
        return getComponentAdapters(Generic.get(componentType), binding);
    }

    /**
     * {@inheritDoc} *
     */
    public <T> List<ComponentAdapter<T>> getComponentAdapters(final Generic<T> componentType, final Class<? extends Annotation> binding) {
        if (componentType == null) {
            return Collections.emptyList();
        }
        List<ComponentAdapter<T>> found = new ArrayList<ComponentAdapter<T>>();
        for (ComponentAdapter<?> componentAdapter : getComponentAdapters()) {
            Object key = componentAdapter.getComponentKey();
            
            //JSR 330 Provider compatibility... we have to be able to return both the providers that provide
            //the type as well as the actual types themselves.
            Class<?> implementation = componentAdapter.getComponentImplementation();
            
            
            boolean compatible = JTypeHelper.isAssignableFrom(componentType, implementation);
            if (!compatible && componentAdapter.findAdapterOfType(ProviderAdapter.class) != null) {
            	//If provider
            	//Todo: Direct access of provider adapter... work around.
            	ProviderAdapter adapter = (ProviderAdapter)componentAdapter.findAdapterOfType(ProviderAdapter.class);
            	compatible = JTypeHelper.isAssignableFrom(componentType, adapter.getProviderReturnType());
            }            
            
            if (compatible &&
                    (!(key instanceof Key) || ((((Key<?>) key).getAnnotation() == null || binding == null ||
                            ((Key<?>) key).getAnnotation() == binding)))) {
                found.add((ComponentAdapter<T>) componentAdapter);
            }
        }
        return found;
    }

    protected MutablePicoContainer addAdapterInternal(ComponentAdapter<?> componentAdapter) {
        Object key = componentAdapter.getComponentKey();
        if (getComponentKeyToAdapterCache().containsKey(key)) {
            throw new PicoCompositionException("Duplicate Keys not allowed. Duplicate for '" + key + "'");
        }
        getModifiableComponentAdapterList().add(componentAdapter);
        getComponentKeyToAdapterCache().put(key, componentAdapter);
        return this;
    }

    /**
     * {@inheritDoc}
     * This method can be used to override the ComponentAdapter created by the {@link ComponentFactory}
     * passed to the constructor of this container.
     */
    public MutablePicoContainer addAdapter(final ComponentAdapter<?> componentAdapter) {
        return addAdapter(componentAdapter, this.containerProperties);
    }

    /**
     * {@inheritDoc}
     */
    public MutablePicoContainer addProvider(Provider<?> provider) {
        return addAdapter(new ProviderAdapter(provider), this.containerProperties);
    }
    
    /**
     * {@inheritDoc}
     */
    public MutablePicoContainer addProvider(Object key, Provider<?> provider) {
        return addAdapter(new ProviderAdapter(key, provider), this.containerProperties);
    }
    

    /**
     * {@inheritDoc} 
     */
    public MutablePicoContainer addAdapter(final ComponentAdapter<?> componentAdapter, final Properties properties) {
        Properties tmpProperties = (Properties) properties.clone();
        removeGenericPropertiesThatWeDontCareAbout(tmpProperties);
        
        
        if (AbstractBehavior.removePropertiesIfPresent(tmpProperties, Characteristics.NONE) == false && componentFactory instanceof Behavior) {
            MutablePicoContainer container = addAdapterInternal(((Behavior) componentFactory).addComponentAdapter(
                    monitor,
                    lifecycle,
                    tmpProperties,
                    componentAdapter));
            throwIfPropertiesLeft(tmpProperties);
            return container;
        } else {
            return addAdapterInternal(componentAdapter);
        }

    }


    /**
     * {@inheritDoc} *
     */
    public <T> ComponentAdapter<T> removeComponent(final Object key) {
        lifecycleState.removingComponent();

        ComponentAdapter<T> adapter = (ComponentAdapter<T>) getComponentKeyToAdapterCache().remove(key);
        getModifiableComponentAdapterList().remove(adapter);
        getOrderedComponentAdapters().remove(adapter);
        return adapter;
    }


    public <T> BindWithOrTo<T> bind(Class<T> type) {
        return new DpcBindWithOrTo<T>(DefaultPicoContainer.this, type);
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
            clazz = (Class<?>) implOrInstance;
        } else {
            clazz = implOrInstance.getClass();
        }
        return addComponent(clazz, implOrInstance, props, null, null, null);
    }


    public MutablePicoContainer addConfig(final String name, final Object val) {
        return addAdapterInternal(new InstanceAdapter<Object>(name, val, lifecycle, monitor));
    }

    /**
     * {@inheritDoc}
     * The returned ComponentAdapter will be instantiated by the {@link ComponentFactory}
     * passed to the container's constructor.
     */
    public MutablePicoContainer addComponent(final Object key,
                                             final Object implOrInstance,
                                             final Parameter... constructorParameters) {
    	
    	
        return this.addComponent(key, implOrInstance, this.containerProperties, 
        			new ConstructorParameters(constructorParameters), null, null);
    }
    
    public MutablePicoContainer addComponent(final Object key, 
    		final Object implOrInstance, 
    		final ConstructorParameters constructorParams, 
    		final FieldParameters[] fieldParameters, 
    		final MethodParameters[] methodParams) {
    	return this.addComponent(key, implOrInstance, containerProperties, constructorParams, fieldParameters, methodParams);
    }

    private MutablePicoContainer addComponent(Object key,
                                              final Object implOrInstance,
                                              final Properties properties,
                                              ConstructorParameters constructorParameters, 
                                              FieldParameters[] fieldParameters, 
                                              MethodParameters[] methodParameters) {
    	
    	Parameter[] tweakedParameters = (constructorParameters != null) ? constructorParameters.getParams() : null;
    	
        if (key instanceof Generic) {
            key = Generic.get(((Generic) key).getType());
        }
        if (tweakedParameters != null && tweakedParameters.length == 0) {
            tweakedParameters = null; // backwards compatibility!  solve this better later - Paul
        }

        //New replacement for Parameter.ZERO.
        if (tweakedParameters != null && tweakedParameters.length == 1 && DefaultConstructorParameter.INSTANCE.equals(tweakedParameters[0])) {
            tweakedParameters = new Parameter[0];
        }
        
        

        if (implOrInstance instanceof Class) {
            Properties tmpProperties = (Properties) properties.clone();
            ComponentAdapter<?> adapter = componentFactory.createComponentAdapter(monitor,
                    lifecycle,
                    tmpProperties,
                    key,
                    (Class<?>) implOrInstance,
                    new ConstructorParameters(tweakedParameters), fieldParameters, methodParameters);
            removeGenericPropertiesThatWeDontCareAbout(tmpProperties);
            throwIfPropertiesLeft(tmpProperties);
            if (lifecycleState.isStarted()) {
                addAdapterIfStartable(adapter);
                potentiallyStartAdapter(adapter);
            }
            return addAdapterInternal(adapter);
        } else {
            ComponentAdapter<?> adapter =
                    new InstanceAdapter<Object>(key, implOrInstance, lifecycle, monitor);
            if (lifecycleState.isStarted()) {
                addAdapterIfStartable(adapter);
                potentiallyStartAdapter(adapter);
            }
            return addAdapter(adapter, properties);
        }
    }

	private void removeGenericPropertiesThatWeDontCareAbout(Properties tmpProperties) {
		AbstractBehavior.removePropertiesIfPresent(tmpProperties, Characteristics.USE_NAMES);
        AbstractBehavior.removePropertiesIfPresent(tmpProperties, Characteristics.STATIC_INJECTION);
	}
    
    

    public static class DpcBindWithOrTo<T> extends DpcBindTo<T> implements BindWithOrTo<T> {

        public DpcBindWithOrTo(MutablePicoContainer mutablePicoContainer, Class<T> type) {
            super(mutablePicoContainer, type);
        }

        public <T> BindTo<T> withAnnotation(Class<? extends Annotation> annotation) {
            return new DpcBindTo<T>(mutablePicoContainer, (Class<T>) type).withAnno(annotation);
        }

        public <T> BindTo<T> named(String name) {
            return new DpcBindTo<T>(mutablePicoContainer, (Class<T>) type).named(name);
        }
    }

    public static class DpcBindTo<T> implements BindTo<T> {
        final MutablePicoContainer mutablePicoContainer;
        final Class<T> type;
        private Class<? extends Annotation> annotation;
        private String name;

        private DpcBindTo(MutablePicoContainer mutablePicoContainer, Class<T> type) {
            this.mutablePicoContainer = mutablePicoContainer;
            this.type = type;
        }

        public MutablePicoContainer to(Class<? extends T> impl) {
            return mutablePicoContainer.addComponent(type, impl);
        }

        public MutablePicoContainer to(T instance) {
            return mutablePicoContainer.addComponent(type, instance);
        }

        public MutablePicoContainer toProvider(javax.inject.Provider<? extends T> provider) {
            return mutablePicoContainer.addAdapter(new ProviderAdapter(provider));
        }

        public MutablePicoContainer toProvider(org.picocontainer.injectors.Provider provider) {
            return mutablePicoContainer.addAdapter(new ProviderAdapter(provider));
        }

        private BindTo<T> withAnno(Class<? extends Annotation> annotation) {
            this.annotation = annotation;
            return this;
        }

        private BindTo<T> named(String name) {
            this.name = name;
            return this;
        }
    }

    private void throwIfPropertiesLeft(final Properties tmpProperties) {
        if (tmpProperties.size() > 0) {
            throw new PicoCompositionException("Unprocessed Characteristics:" + tmpProperties + ", please refer to http://picocontainer.org/unprocessed-properties-help.html");
        }
    }

    private synchronized void addOrderedComponentAdapter(final ComponentAdapter<?> componentAdapter) {
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
        List<T> result = new ArrayList<T>();
        synchronized (this) {
            for (ComponentAdapter<?> componentAdapter : getModifiableComponentAdapterList()) {
                if (componentType.isAssignableFrom(componentAdapter.getComponentImplementation())) {
                    ComponentAdapter<T> typedComponentAdapter = (ComponentAdapter<T>) componentAdapter;
                    T componentInstance = getLocalInstance(typedComponentAdapter);
                    adapterToInstanceMap.put(typedComponentAdapter, componentInstance);
                }
            }
            for (ComponentAdapter<?> componentAdapter : getOrderedComponentAdapters()) {
                final T componentInstance = adapterToInstanceMap.get(componentAdapter);
                if (componentInstance != null) {
                    // may be null in the case of the "implicit" addAdapter
                    // representing "this".
                    result.add(componentInstance);
                }
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

    public Object getComponent(Object keyOrType) {
        return getComponent(keyOrType, null, ComponentAdapter.NOTHING.class);
    }

    public Object getComponentInto(final Object keyOrType, Type into) {
        return getComponent(keyOrType, null, into);
    }

    public <T> T getComponent(Class<T> componentType) {
        return getComponent(Generic.get(componentType));
    }

    public <T> T getComponent(Generic<T> componentType) {
        Object o = getComponent((Object) componentType, null, ComponentAdapter.NOTHING.class);
        return (T) o;
    }

    public Object getComponent(final Object keyOrType, final Class<? extends Annotation> annotation, Type into) {
        ComponentAdapter<?> componentAdapter;
        Object component;
        if (annotation != null) {
            componentAdapter = getComponentAdapter((Generic<?>) keyOrType, annotation);
            component = componentAdapter == null ? null : getInstance(componentAdapter, null, into);
        } else if (keyOrType instanceof Generic && ((Generic) keyOrType).getType() instanceof Class) {
            componentAdapter = getComponentAdapter((Generic<?>) keyOrType, (NameBinding) null);
            component = componentAdapter == null ? null : getInstance(componentAdapter, (Generic<?>) keyOrType, into);
        } else {
            componentAdapter = getComponentAdapter(keyOrType);
            component = componentAdapter == null ? null : getInstance(componentAdapter, null, into);
        }
        return decorateComponent(component, componentAdapter);
    }

    /**
     * This is invoked when getComponent(..) is called.  It allows extendees to decorate a
     * component before it is returned to the caller.
     *
     * @param component        the component that will be returned for getComponent(..)
     * @param componentAdapter the component adapter that made that component
     * @return the component (the same as that passed in by default)
     */
    protected Object decorateComponent(Object component, ComponentAdapter<?> componentAdapter) {
        if (componentAdapter instanceof ComponentLifecycle<?>
                && lifecycle.isLazy(componentAdapter) // is Lazy
                && !((ComponentLifecycle<?>) componentAdapter).isStarted()) {
            ((ComponentLifecycle<?>) componentAdapter).start(this);
        }
        return component;
    }

    public <T> T getComponent(final Class<T> componentType, final Class<? extends Annotation> binding, Type into) {
        Object o = getComponent((Object) Generic.get(componentType), binding, into);
        return componentType.cast(o);
    }

    public <T> T getComponent(Class<T> componentType, Class<? extends Annotation> binding) {
        return getComponent(componentType, binding, ComponentAdapter.NOTHING.class);
    }

    public <T> T getComponentInto(final Class<T> componentType, Type into) {
        Object o = getComponent((Object) componentType, null, into);
        return componentType.cast(o);
    }

    public <T> T getComponentInto(Generic<T> componentType, Type into) {
        Object o = getComponent((Object) componentType, null, into);
        return (T) o;
    }

    private Object getInstance(final ComponentAdapter<?> componentAdapter, Generic<?> key, Type into) {
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
        	Object componentKey = key;
        	if (componentKey == null) {
        		componentKey = componentAdapter.getComponentKey();
        	}
            return getParent().getComponentInto(componentKey, into);
        }

        return null;
    }


    /**
     * {@inheritDoc} *
     */
    public PicoContainer getParent() {
        return parent;
    }

    /**
     * {@inheritDoc} *
     */
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
     * if it is an instance of {@link ChangedBehavior lifecycle manager}.
     * The actual {@link LifecycleStrategy lifecycle strategy} supported
     * depends on the concrete implementation of the adapter.
     *
     * @see ChangedBehavior
     * @see LifecycleStrategy
     * @see #makeChildContainer()
     * @see #addChildContainer(PicoContainer)
     * @see #removeChildContainer(PicoContainer)
     */
    public synchronized void start() {

        lifecycleState.starting(getName());

        startAdapters();
        childrenStarted.clear();
        for (PicoContainer child : children) {
            childrenStarted.add(new WeakReference<PicoContainer>(child));
            if (child instanceof Startable) {
                ((Startable) child).start();
            }
        }
    }

    /**
     * Stop the components of this PicoContainer and all its logical child containers.
     * The stopping of the child containers is only attempted for those that have been
     * started, possibly not successfully.
     * The lifecycle operation is delegated to the component adapter,
     * if it is an instance of {@link ChangedBehavior lifecycle manager}.
     * The actual {@link LifecycleStrategy lifecycle strategy} supported
     * depends on the concrete implementation of the adapter.
     *
     * @see ChangedBehavior
     * @see LifecycleStrategy
     * @see #makeChildContainer()
     * @see #addChildContainer(PicoContainer)
     * @see #removeChildContainer(PicoContainer)
     */
    public synchronized void stop() {

        lifecycleState.stopping(getName());

        for (PicoContainer child : children) {
            if (childStarted(child)) {
                if (child instanceof Startable) {
                    ((Startable) child).stop();
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
     * if it is an instance of {@link ChangedBehavior lifecycle manager}.
     * The actual {@link LifecycleStrategy lifecycle strategy} supported
     * depends on the concrete implementation of the adapter.
     *
     * @see ChangedBehavior
     * @see LifecycleStrategy
     * @see #makeChildContainer()
     * @see #addChildContainer(PicoContainer)
     * @see #removeChildContainer(PicoContainer)
     */
    public synchronized void dispose() {
        if (lifecycleState.isStarted()) {
            stop();
        }

        lifecycleState.disposing(getName());

        for (PicoContainer child : children) {
            if (child instanceof MutablePicoContainer) {
                ((Disposable) child).dispose();
            }
        }
        disposeAdapters();

        lifecycleState.disposed();
    }

    /** {@inheritDoc} **/
    public synchronized void setLifecycleState(LifecycleState lifecycleState) {
        this.lifecycleState = lifecycleState;
    }
    
    /** {@inheritDoc} **/
    public synchronized LifecycleState getLifecycleState() {
    	return lifecycleState;
    }

    public MutablePicoContainer makeChildContainer() {
        DefaultPicoContainer pc = new DefaultPicoContainer(this, lifecycle, monitor, componentFactory);
        addChildContainer(pc);
        return pc;
    }

    /**
     * Checks for identical references in the child container.  It doesn't
     * traverse an entire hierarchy, namely it simply checks for child containers
     * that are equal to the current container.
     *
     * @param child
     */
    private void checkCircularChildDependencies(PicoContainer child) {
        final String MESSAGE = "Cannot have circular dependency between parent %s and child: %s";
        if (child == this) {
            throw new IllegalArgumentException(String.format(MESSAGE, this, child));
        }

        //Todo: Circular Import Dependency on AbstractDelegatingPicoContainer
        if (child instanceof AbstractDelegatingPicoContainer) {
            AbstractDelegatingPicoContainer delegateChild = (AbstractDelegatingPicoContainer) child;
            while (delegateChild != null) {
                PicoContainer delegateInstance = delegateChild.getDelegate();
                if (this == delegateInstance) {
                    throw new IllegalArgumentException(String.format(MESSAGE, this, child));
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
                containerProperties.setProperty(s, c.getProperty(s));
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
        this.monitor = monitor;
        if (lifecycle instanceof ComponentMonitorStrategy) {
            ((ComponentMonitorStrategy) lifecycle).changeMonitor(monitor);
        }
        for (ComponentAdapter<?> adapter : getModifiableComponentAdapterList()) {
            if (adapter instanceof ComponentMonitorStrategy) {
                ((ComponentMonitorStrategy) adapter).changeMonitor(monitor);
            }
        }
        for (PicoContainer child : children) {
            if (child instanceof ComponentMonitorStrategy) {
                ((ComponentMonitorStrategy) child).changeMonitor(monitor);
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
        return monitor;
    }

    /**
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
            if (!lifecycle.isLazy(adapter)) {
                ((ComponentLifecycle<?>) adapter).start(this);
            }
        }
    }

    private void addAdapterIfStartable(ComponentAdapter<?> adapter) {
        if (adapter instanceof ComponentLifecycle) {
            ComponentLifecycle<?> componentLifecycle = (ComponentLifecycle<?>) adapter;
            if (componentLifecycle.componentHasLifecycle()) {
                // create an instance, it will be added to the ordered CA list
                instantiateComponentAsIsStartable(adapter);
                addOrderedComponentAdapter(adapter);
            }
        }
    }

    protected void instantiateComponentAsIsStartable(ComponentAdapter<?> adapter) {
        if (!lifecycle.isLazy(adapter)) {
            adapter.getComponentInstance(DefaultPicoContainer.this, ComponentAdapter.NOTHING.class);
        }
    }

    /**
     * Loops over started component adapters (in inverse order) and invokes
     * stop(PicoContainer) method on the ones which are LifecycleManagers
     */
    private void stopAdapters() {
        for (int i = getOrderedComponentAdapters().size() - 1; 0 <= i; i--) {
            ComponentAdapter<?> adapter = getOrderedComponentAdapters().get(i);
            if (adapter instanceof ComponentLifecycle) {
                ComponentLifecycle<?> componentLifecycle = (ComponentLifecycle<?>) adapter;
                if (componentLifecycle.componentHasLifecycle() && componentLifecycle.isStarted()) {
                    componentLifecycle.stop(DefaultPicoContainer.this);
                }
            }
        }
    }

    /**
     * Loops over all component adapters (in inverse order) and invokes
     * dispose(PicoContainer) method on the ones which are LifecycleManagers
     */
    private void disposeAdapters() {
        for (int i = getOrderedComponentAdapters().size() - 1; 0 <= i; i--) {
            ComponentAdapter<?> adapter = getOrderedComponentAdapters().get(i);
            if (adapter instanceof ComponentLifecycle) {
                ComponentLifecycle<?> componentLifecycle = (ComponentLifecycle<?>) adapter;
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
     * @return the keyToAdapterCache
     */
    protected Map<Object, ComponentAdapter<?>> getComponentKeyToAdapterCache() {
        return keyToAdapterCache;
    }

    /**
     * @return the componentAdapters
     */
    protected List<ComponentAdapter<?>> getModifiableComponentAdapterList() {
        return componentAdapters;
    }
    
    /** {@inheritDoc} **/
    public synchronized void setName(String name) {
        this.name = name;
    }
    
    /** {@inheritDoc} **/
    public synchronized String getName() {
    	return name;
    }

    @Override
    public String toString() {
    	return String.format("%s:%d<%s", (name != null ? name : super.toString()), this.componentAdapters.size(), 
    				(parent != null && !(parent instanceof EmptyPicoContainer)? parent.toString() : "|"));    
    }

    /**
     * If this container has a set of converters, then return it.
     * If it does not, and the parent (or their parent ..) does, use that
     * If they do not, return a NullObject implementation (ConversNothing)
     *
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

    @SuppressWarnings("synthetic-access")
    private class AsPropertiesPicoContainer extends AbstractDelegatingMutablePicoContainer {

        private final Properties properties;
        
        
        public AsPropertiesPicoContainer(final Properties... props) {
            super(DefaultPicoContainer.this);
            properties = (Properties) containerProperties.clone();
            for (Properties c : props) {
                Enumeration<?> e = c.propertyNames();
                while (e.hasMoreElements()) {
                    String s = (String) e.nextElement();
                    properties.setProperty(s, c.getProperty(s));
                }
            }
        }

        @Override
        public MutablePicoContainer as(Properties... props) {
            throw new PicoCompositionException("Syntax 'as(FOO).as(BAR)' not allowed, do 'as(FOO, BAR)' instead");
        }

        @Override
        public MutablePicoContainer makeChildContainer() {
            return getDelegate().makeChildContainer();
        }

        @Override
        public <T> BindWithOrTo<T> bind(Class<T> type) {
            return new DpcBindWithOrTo<T>(AsPropertiesPicoContainer.this, type);
        }

        @Override
        public MutablePicoContainer addComponent(final Object key,
                                                 final Object implOrInstance,
                                                 final Parameter... parameters) throws PicoCompositionException {
            return DefaultPicoContainer.this.addComponent(key,
                    implOrInstance,
                    properties,
                    new ConstructorParameters(parameters), null, null);
        }

        @Override
        public MutablePicoContainer addComponent(final Object key,
                                                 final Object implOrInstance,
                                                 final ConstructorParameters constructorParams, 
                                                 final FieldParameters[] fieldParams, 
                                                 final MethodParameters[] methodParams) throws PicoCompositionException {
            return DefaultPicoContainer.this.addComponent(key,
                    implOrInstance,
                    properties,
                    constructorParams, fieldParams, methodParams);
        }        
        
        @Override
        public MutablePicoContainer addComponent(final Object implOrInstance) throws PicoCompositionException {
            return DefaultPicoContainer.this.addComponent(implOrInstance, properties);
        }

        @Override
        public MutablePicoContainer addAdapter(final ComponentAdapter<?> componentAdapter) throws PicoCompositionException {
            return DefaultPicoContainer.this.addAdapter(componentAdapter, properties);
        }

        @Override
        public MutablePicoContainer addProvider(Provider<?> provider) {
            return DefaultPicoContainer.this.addAdapter(new ProviderAdapter(provider), properties);
        }

        /**
         * {@inheritDoc}
         * @see org.picocontainer.MutablePicoContainer#getLifecycleState()
         */
        @Override
        public LifecycleState getLifecycleState() {
            return DefaultPicoContainer.this.getLifecycleState();
        }

        /**
         * {@inheritDoc}
         * @see org.picocontainer.MutablePicoContainer#getName()
         */
        @Override
        public String getName() {
            return DefaultPicoContainer.this.getName();
        }

        public void changeMonitor(ComponentMonitor monitor) {
            DefaultPicoContainer.this.changeMonitor(monitor);
        }
    }

}
