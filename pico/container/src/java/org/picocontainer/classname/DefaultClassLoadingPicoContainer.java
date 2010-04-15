/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file. 
 ******************************************************************************/
package org.picocontainer.classname;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.ComponentMonitorStrategy;
import org.picocontainer.Converters;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.PicoClassNotFoundException;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.containers.AbstractDelegatingMutablePicoContainer;
import org.picocontainer.lifecycle.LifecycleState;
import org.picocontainer.security.CustomPermissionsURLClassLoader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.security.AccessController;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Default implementation of ClassLoadingPicoContainer.
 *
 * @author Paul Hammant
 * @author Mauro Talevi
 * @author Michael Rimov
 */
@SuppressWarnings("serial")
public class DefaultClassLoadingPicoContainer extends AbstractDelegatingMutablePicoContainer implements
        ClassLoadingPicoContainer, ComponentMonitorStrategy {

    /**
     * Converting Map to allow for primitives to be boxed to Object types.
     */
    private static final transient Map<String, String> primitiveNameToBoxedName = new HashMap<String, String>();

    static {
        primitiveNameToBoxedName.put("int", Integer.class.getName());
        primitiveNameToBoxedName.put("byte", Byte.class.getName());
        primitiveNameToBoxedName.put("short", Short.class.getName());
        primitiveNameToBoxedName.put("long", Long.class.getName());
        primitiveNameToBoxedName.put("float", Float.class.getName());
        primitiveNameToBoxedName.put("double", Double.class.getName());
        primitiveNameToBoxedName.put("boolean", Boolean.class.getName());
    }

    private final transient List<ClassPathElement> classPathElements = new ArrayList<ClassPathElement>();
    private final transient ClassLoader parentClassLoader;

    private transient ClassLoader componentClassLoader;
    private transient boolean componentClassLoaderLocked;

    protected final Map<String, PicoContainer> namedChildContainers = new HashMap<String, PicoContainer>();

    public DefaultClassLoadingPicoContainer(ClassLoader classLoader, ComponentFactory componentFactory, PicoContainer parent) {
        super(new DefaultPicoContainer(componentFactory, parent));
        parentClassLoader = classLoader;
    }

    public DefaultClassLoadingPicoContainer(ClassLoader classLoader, MutablePicoContainer delegate) {
        super(delegate);
        parentClassLoader = classLoader;

    }

    public DefaultClassLoadingPicoContainer(ClassLoader classLoader, PicoContainer parent, ComponentMonitor componentMonitor) {
        super(new DefaultPicoContainer(new Caching(), parent));
        parentClassLoader = classLoader;
        ((ComponentMonitorStrategy) getDelegate()).changeMonitor(componentMonitor);
    }

    public DefaultClassLoadingPicoContainer(ComponentFactory componentFactory) {
        super(new DefaultPicoContainer(componentFactory, null));
        parentClassLoader = DefaultClassLoadingPicoContainer.class.getClassLoader();
    }

    
    public DefaultClassLoadingPicoContainer(PicoContainer parent) {
        super(new DefaultPicoContainer(parent));
        parentClassLoader = DefaultClassLoadingPicoContainer.class.getClassLoader();
    }

    public DefaultClassLoadingPicoContainer(MutablePicoContainer delegate) {
        super(delegate);
        parentClassLoader = DefaultClassLoadingPicoContainer.class.getClassLoader();
    }

    public DefaultClassLoadingPicoContainer(ClassLoader classLoader) {
        super(new DefaultPicoContainer());
        parentClassLoader = classLoader;
    }

    public DefaultClassLoadingPicoContainer() {
        super(new DefaultPicoContainer());
        parentClassLoader = DefaultClassLoadingPicoContainer.class.getClassLoader();
    }

    public DefaultClassLoadingPicoContainer(ComponentFactory componentFactory, LifecycleStrategy lifecycleStrategy,
            PicoContainer parent, ClassLoader cl, ComponentMonitor componentMonitor) {

        super(new DefaultPicoContainer(componentFactory, lifecycleStrategy, parent, componentMonitor));
        parentClassLoader = (cl != null) ? cl : DefaultClassLoadingPicoContainer.class.getClassLoader();
    }

    protected DefaultClassLoadingPicoContainer createChildContainer() {
        MutablePicoContainer child = getDelegate().makeChildContainer();
        DefaultClassLoadingPicoContainer container = new DefaultClassLoadingPicoContainer(getComponentClassLoader(), child);
        container.changeMonitor(currentMonitor());
        return container;
    }

    /**
     * Propagates the monitor change down the delegate chain if a delegate that implements ComponentMonitorStrategy
     * exists.  Because of the ComponentMonitorStrategy API, not all delegates can have their API changed.  If
     * a delegate implementing ComponentMonitorStrategy cannot be found, an exception is thrown.
     * @throws IllegalStateException if no delegate can be found that implements ComponentMonitorStrategy.
     * @param monitor the monitor to swap.
     */
    public void changeMonitor(ComponentMonitor monitor) {
    	
    	MutablePicoContainer picoDelegate = getDelegate();
    	while (picoDelegate != null) {
    		if (picoDelegate instanceof ComponentMonitorStrategy) {
    			((ComponentMonitorStrategy)picoDelegate).changeMonitor(monitor);
    			return;
    		}
    		
    		if (picoDelegate instanceof AbstractDelegatingMutablePicoContainer) {
    			picoDelegate = ((AbstractDelegatingMutablePicoContainer)picoDelegate).getDelegate();
    		} else {
    			break;
    		}
    	}
    	
    	throw new IllegalStateException("Could not find delegate picocontainer that implemented ComponentMonitorStrategy");
    	
    	
    }

    public ComponentMonitor currentMonitor() {
    	MutablePicoContainer picoDelegate = getDelegate();
    	while (picoDelegate != null) {
    		if (picoDelegate instanceof ComponentMonitorStrategy) {
    			return ((ComponentMonitorStrategy)picoDelegate).currentMonitor();
    		}
    		
    		if (picoDelegate instanceof AbstractDelegatingMutablePicoContainer) {
    			picoDelegate = ((AbstractDelegatingMutablePicoContainer)picoDelegate).getDelegate();
    		} else {
    			break;
    		}
    	}
    	
    	throw new IllegalStateException("Could not find delegate picocontainer that implemented ComponentMonitorStrategy");
    }

    @Override
    public final Object getComponent(Object componentKeyOrType) {
        return getComponentInto(componentKeyOrType, ComponentAdapter.NOTHING.class);
    }

    public final Object getComponentInto(Object componentKeyOrType, Type into) {

        if (componentKeyOrType instanceof ClassName) {
            componentKeyOrType = loadClass(componentKeyOrType.toString());
        }

        Object instance = getDelegate().getComponentInto(componentKeyOrType, into);

        if (instance != null) {
            return instance;
        }

        ComponentAdapter<?> componentAdapter = null;
        if (componentKeyOrType.toString().startsWith("*")) {
            String candidateClassName = componentKeyOrType.toString().substring(1);
            Collection<ComponentAdapter<?>> cas = getComponentAdapters();
            for (ComponentAdapter<?> ca : cas) {
                Object key = ca.getComponentKey();
                if (key instanceof Class && candidateClassName.equals(((Class<?>) key).getName())) {
                    componentAdapter = ca;
                    break;
                }
            }
        }
        if (componentAdapter != null) {
            return componentAdapter.getComponentInstance(this, into);
        } else {
            return getComponentInstanceFromChildren(componentKeyOrType, into);
        }
    }

    private Object getComponentInstanceFromChildren(Object componentKey, Type into) {
        String componentKeyPath = componentKey.toString();
        int ix = componentKeyPath.indexOf('/');
        if (ix != -1) {
            String firstElement = componentKeyPath.substring(0, ix);
            String remainder = componentKeyPath.substring(ix + 1, componentKeyPath.length());
            Object o = getNamedContainers().get(firstElement);
            if (o != null) {
                MutablePicoContainer child = (MutablePicoContainer) o;
                return child.getComponentInto(remainder, into);
            }
        }
        return null;
    }

    public final MutablePicoContainer makeChildContainer() {
        return makeChildContainer("containers" + namedChildContainers.size());
    }

    /**
     * Makes a child container with the same basic characteristics of
     * <tt>this</tt> object (ComponentFactory, PicoContainer type, Behavior,
     * etc)
     *
     * @param name the name of the child container
     * @return The child MutablePicoContainer
     */
    public ClassLoadingPicoContainer makeChildContainer(String name) {
        DefaultClassLoadingPicoContainer child = createChildContainer();
        MutablePicoContainer parentDelegate = getDelegate();
        parentDelegate.removeChildContainer(child.getDelegate());
        parentDelegate.addChildContainer(child);
        namedChildContainers.put(name, child);
        return child;
    }

    public boolean removeChildContainer(PicoContainer child) {
        boolean result = getDelegate().removeChildContainer(child);
        Iterator<Map.Entry<String, PicoContainer>> children = namedChildContainers.entrySet().iterator();
        while (children.hasNext()) {
            Map.Entry<String, PicoContainer> e = children.next();
            PicoContainer pc = e.getValue();
            if (pc == child) {
                children.remove();
            }
        }
        return result;
    }

    protected final Map<String, PicoContainer> getNamedContainers() {
        return namedChildContainers;
    }

    public ClassPathElement addClassLoaderURL(URL url) {
        if (componentClassLoaderLocked) {
            throw new IllegalStateException("ClassLoader URLs cannot be added once this instance is locked");
        }

        ClassPathElement classPathElement = new ClassPathElement(url);
        classPathElements.add(classPathElement);
        return classPathElement;
    }

    public MutablePicoContainer addComponent(Object implOrInstance) {
        if (implOrInstance instanceof ClassName) {
            super.addComponent(loadClass(implOrInstance.toString()));
        } else {
            super.addComponent(implOrInstance);
        }
        return this;
    }

    public MutablePicoContainer addComponent(Object key, Object componentImplementationOrInstance,
            Parameter... parameters) {
        super.addComponent(classNameToClassIfApplicable(key),
                classNameToClassIfApplicable(componentImplementationOrInstance), parameters);
        return this;
    }

    private Object classNameToClassIfApplicable(Object key) {
        if (key instanceof ClassName) {
            key = loadClass(key.toString());
        }
        return key;
    }

    public MutablePicoContainer addAdapter(ComponentAdapter<?> componentAdapter) throws PicoCompositionException {
        super.addAdapter(componentAdapter);
        return this;
    }

    public ClassLoader getComponentClassLoader() {
        if (componentClassLoader == null) {
            componentClassLoaderLocked = true;
            componentClassLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    return new CustomPermissionsURLClassLoader(getURLs(classPathElements), makePermissions(),
                            parentClassLoader);
                }
            });
        }
        return componentClassLoader;
    }

    public MutablePicoContainer addChildContainer(PicoContainer child) {
        getDelegate().addChildContainer(child);
        namedChildContainers.put("containers" + namedChildContainers.size(), child);
        return this;
    }

    public ClassLoadingPicoContainer addChildContainer(String name, PicoContainer child) {

        super.addChildContainer(child);

        namedChildContainers.put(name, child);
        return this;
    }

    private Class<?> loadClass(final String className) {
        ClassLoader classLoader = getComponentClassLoader();
        // this is deliberately not a doPrivileged operation.
        String cn = getClassName(className);
        try {
            return classLoader.loadClass(cn);
        } catch (ClassNotFoundException e) {
            throw new PicoClassNotFoundException(cn, e);
        }
    }

    private Map<URL, Permissions> makePermissions() {
        Map<URL, Permissions> permissionsMap = new HashMap<URL, Permissions>();
        for (ClassPathElement cpe : classPathElements) {
            Permissions permissionCollection = cpe.getPermissionCollection();
            permissionsMap.put(cpe.getUrl(), permissionCollection);
        }
        return permissionsMap;
    }

    private URL[] getURLs(List<ClassPathElement> classPathElemelements) {
        final URL[] urls = new URL[classPathElemelements.size()];
        for (int i = 0; i < urls.length; i++) {
            urls[i] = (classPathElemelements.get(i)).getUrl();
        }
        return urls;
    }

    private static String getClassName(String primitiveOrClass) {
        String fromMap = primitiveNameToBoxedName.get(primitiveOrClass);
        return fromMap != null ? fromMap : primitiveOrClass;
    }

    public ComponentAdapter<?> getComponentAdapter(Object componentKey) {
        Object componentKey2 = componentKey;
        if (componentKey instanceof ClassName) {
            componentKey2 = loadClass(componentKey.toString());
        }
        return super.getComponentAdapter(componentKey2);
    }

    public MutablePicoContainer change(Properties... properties) {
        super.change(properties);
        return this;
    }

    public MutablePicoContainer as(Properties... properties) {
        return new AsPropertiesPicoContainer(properties);
    }

    private class AsPropertiesPicoContainer implements ClassLoadingPicoContainer {
        private MutablePicoContainer delegate;

        public AsPropertiesPicoContainer(Properties... props) {
            delegate = DefaultClassLoadingPicoContainer.this.getDelegate().as(props);
        }

        public ClassPathElement addClassLoaderURL(URL url) {
            return DefaultClassLoadingPicoContainer.this.addClassLoaderURL(url);
        }

        public ClassLoader getComponentClassLoader() {
            return DefaultClassLoadingPicoContainer.this.getComponentClassLoader();
        }

        public ClassLoadingPicoContainer makeChildContainer(String name) {
            return DefaultClassLoadingPicoContainer.this.makeChildContainer(name);
        }

        public ClassLoadingPicoContainer addChildContainer(String name, PicoContainer child) {
            return (ClassLoadingPicoContainer) DefaultClassLoadingPicoContainer.this.addChildContainer(child);
        }

        public MutablePicoContainer addComponent(Object componentKey, Object componentImplementationOrInstance,
                Parameter... parameters) {
            delegate.addComponent(classNameToClassIfApplicable(componentKey),
                    classNameToClassIfApplicable(componentImplementationOrInstance), parameters);
            return DefaultClassLoadingPicoContainer.this;
        }

        public MutablePicoContainer addComponent(Object implOrInstance) {
            delegate.addComponent(classNameToClassIfApplicable(implOrInstance));
            return DefaultClassLoadingPicoContainer.this;
        }

        public MutablePicoContainer addConfig(String name, Object val) {
            delegate.addConfig(name, val);
            return DefaultClassLoadingPicoContainer.this;
        }

        public MutablePicoContainer addAdapter(ComponentAdapter<?> componentAdapter) {
            delegate.addAdapter(componentAdapter);
            return DefaultClassLoadingPicoContainer.this;
        }

        public ComponentAdapter removeComponent(Object componentKey) {
            return delegate.removeComponent(componentKey);
        }

        public ComponentAdapter removeComponentByInstance(Object componentInstance) {
            return delegate.removeComponentByInstance(componentInstance);
        }

        public MutablePicoContainer makeChildContainer() {
            return DefaultClassLoadingPicoContainer.this.makeChildContainer();
        }

        public MutablePicoContainer addChildContainer(PicoContainer child) {
            return DefaultClassLoadingPicoContainer.this.addChildContainer(child);
        }

        public boolean removeChildContainer(PicoContainer child) {
            return DefaultClassLoadingPicoContainer.this.removeChildContainer(child);
        }

        public MutablePicoContainer change(Properties... properties) {
            return DefaultClassLoadingPicoContainer.this.change(properties);
        }

        public MutablePicoContainer as(Properties... properties) {
            return new AsPropertiesPicoContainer(properties);
        }

        public Object getComponent(Object componentKeyOrType) {
            return getComponentInto(componentKeyOrType, ComponentAdapter.NOTHING.class);
        }

        public Object getComponentInto(Object componentKeyOrType, Type into) {
            return DefaultClassLoadingPicoContainer.this.getComponentInto(componentKeyOrType, into);
        }

        public <T> T getComponent(Class<T> componentType) {
            return getComponentInto(componentType, ComponentAdapter.NOTHING.class);
        }

        public <T> T getComponentInto(Class<T> componentType, Type into) {
            return DefaultClassLoadingPicoContainer.this.getComponentInto(componentType, into);
        }

        public <T> T getComponent(Class<T> componentType, Class<? extends Annotation> binding, Type into) {
            return DefaultClassLoadingPicoContainer.this.getComponent(componentType, binding, into);
        }

        public <T> T getComponent(Class<T> componentType, Class<? extends Annotation> binding) {
            return DefaultClassLoadingPicoContainer.this.getComponent(componentType, binding);
        }

        public List<Object> getComponents() {
            return DefaultClassLoadingPicoContainer.this.getComponents();
        }

        public PicoContainer getParent() {
            return DefaultClassLoadingPicoContainer.this.getParent();
        }

        public ComponentAdapter<?> getComponentAdapter(Object componentKey) {
            return DefaultClassLoadingPicoContainer.this.getComponentAdapter(componentKey);
        }

        public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, NameBinding componentNameBinding) {
            return DefaultClassLoadingPicoContainer.this.getComponentAdapter(componentType, componentNameBinding);
        }

        public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, Class<? extends Annotation> binding) {
            return DefaultClassLoadingPicoContainer.this.getComponentAdapter(componentType, binding);
        }

        public Collection<ComponentAdapter<?>> getComponentAdapters() {
            return DefaultClassLoadingPicoContainer.this.getComponentAdapters();
        }

        public <T> List<ComponentAdapter<T>> getComponentAdapters(Class<T> componentType) {
            return DefaultClassLoadingPicoContainer.this.getComponentAdapters(componentType);
        }

        public <T> List<ComponentAdapter<T>> getComponentAdapters(Class<T> componentType,
                Class<? extends Annotation> binding) {
            return DefaultClassLoadingPicoContainer.this.getComponentAdapters(componentType, binding);
        }

        public <T> List<T> getComponents(Class<T> componentType) {
            return DefaultClassLoadingPicoContainer.this.getComponents(componentType);
        }

        public void accept(PicoVisitor visitor) {
            DefaultClassLoadingPicoContainer.this.accept(visitor);
        }

        public void start() {
            //This implementation does nothing on lifecycle triggers.          
        }

        public void stop() {
            //This implementation does nothing on lifecycle triggers.          
        }

        public void dispose() {
            //This implementation does nothing on lifecycle triggers.          
        }

        public void setName(String name) {
            DefaultClassLoadingPicoContainer.this.setName(name);
        }

        public void setLifecycleState(LifecycleState lifecycleState) {
            DefaultClassLoadingPicoContainer.this.setLifecycleState(lifecycleState);
        }

        public Converters getConverter() {
            return DefaultClassLoadingPicoContainer.this.getConverters();
        }
        
        /**
         * {@inheritDoc}
         * @see org.picocontainer.MutablePicoContainer#getLifecycleState()
         */
        public LifecycleState getLifecycleState() {
            return DefaultClassLoadingPicoContainer.this.getLifecycleState();
        }

        /**
         * {@inheritDoc}
         * @see org.picocontainer.MutablePicoContainer#getName(java.lang.String)
         */
        public String getName() {
            return DefaultClassLoadingPicoContainer.this.getName();
        }        
    }

}