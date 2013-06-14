/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file. 
 ******************************************************************************/
package org.picocontainer.classname;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Provider;

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
import org.picocontainer.PicoException;
import org.picocontainer.PicoVisitor;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.containers.AbstractDelegatingMutablePicoContainer;
import org.picocontainer.lifecycle.LifecycleState;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;
import org.picocontainer.security.CustomPermissionsURLClassLoader;

import com.googlecode.jtype.Generic;

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

    private transient URLClassLoader componentClassLoader;
    private transient boolean componentClassLoaderLocked;

    protected final Map<String, PicoContainer> namedChildContainers = new HashMap<String, PicoContainer>();

    public DefaultClassLoadingPicoContainer(ClassLoader classLoader, ComponentFactory componentFactory, PicoContainer parent) {
        super(new DefaultPicoContainer(parent, componentFactory));
        parentClassLoader = classLoader;
    }

    public DefaultClassLoadingPicoContainer(ClassLoader classLoader, MutablePicoContainer delegate) {
        super(delegate);
        parentClassLoader = classLoader;

    }

    public DefaultClassLoadingPicoContainer(ClassLoader classLoader, PicoContainer parent, ComponentMonitor monitor) {
        super(new DefaultPicoContainer(parent, new Caching()));
        parentClassLoader = classLoader;
        ((ComponentMonitorStrategy) getDelegate()).changeMonitor(monitor);
    }

    public DefaultClassLoadingPicoContainer(ComponentFactory componentFactory) {
        super(new DefaultPicoContainer((PicoContainer) null, componentFactory));
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

    public DefaultClassLoadingPicoContainer(ComponentFactory componentFactory, LifecycleStrategy lifecycle,
            PicoContainer parent, ClassLoader cl, ComponentMonitor monitor) {

        super(new DefaultPicoContainer(parent, lifecycle, monitor, componentFactory));
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

	@Override
	public void dispose() {
    	try {
        	if (componentClassLoader != null) {
    				Method closeMethod = CustomPermissionsURLClassLoader.class.getMethod("close");
    				closeMethod.invoke(componentClassLoader);
        	}
    	} catch (NoSuchMethodException e) {
			//Ignore -- if we're on NoSuchMethodExceptionjust means we're not on JDK 1.7 for deployment.
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw  (RuntimeException)e;
			}
			throw new RuntimeException("Error disposing " + this, e);
		} finally {
			componentClassLoader = null;
			getDelegate().dispose();
		}
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
    public final Object getComponent(Object keyOrType) {
        return getComponentInto(keyOrType, ComponentAdapter.NOTHING.class);
    }

    public final Object getComponentInto(Object keyOrType, Type into) {

        if (keyOrType instanceof ClassName) {
            keyOrType = loadClass((ClassName) keyOrType);
        }

        Object instance = getDelegate().getComponentInto(keyOrType, into);

        if (instance != null) {
            return instance;
        }

        ComponentAdapter<?> componentAdapter = null;
        if (keyOrType.toString().startsWith("*")) {
            String candidateClassName = keyOrType.toString().substring(1);
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
            return getComponentInstanceFromChildren(keyOrType, into);
        }
    }

    private Object getComponentInstanceFromChildren(Object key, Type into) {
        String keyPath = key.toString();
        int ix = keyPath.indexOf('/');
        if (ix != -1) {
            String firstElement = keyPath.substring(0, ix);
            String remainder = keyPath.substring(ix + 1, keyPath.length());
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
            super.addComponent(loadClass((ClassName) implOrInstance));
        } else {
            super.addComponent(implOrInstance);
        }
        return this;
    }

    public MutablePicoContainer addComponent(Object key, Object implOrInstance,
            Parameter... parameters) {
        super.addComponent(classNameToClassIfApplicable(key),
                classNameToClassIfApplicable(implOrInstance), parameters);
        return this;
    }

    private Object classNameToClassIfApplicable(Object key) {
        if (key instanceof ClassName) {
            key = loadClass((ClassName) key);
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
            componentClassLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                public URLClassLoader run() {
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

    private Class<?> loadClass(final ClassName className) {
        ClassLoader classLoader = getComponentClassLoader();
        // this is deliberately not a doPrivileged operation.
        String cn = getClassName(className.toString());
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

    public ComponentAdapter<?> getComponentAdapter(Object key) {
        Object key2 = key;
        if (key instanceof ClassName) {
            key2 = loadClass((ClassName) key);
        }
        return super.getComponentAdapter(key2);
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

        public <T> BindWithOrTo<T> bind(Class<T> type) {
            return new DefaultPicoContainer.DpcBindWithOrTo<T>(DefaultClassLoadingPicoContainer.this, type);
        }

        public MutablePicoContainer addComponent(Object key, Object implOrInstance,
                Parameter... parameters) {
            delegate.addComponent(classNameToClassIfApplicable(key),
                    classNameToClassIfApplicable(implOrInstance), parameters);
            return DefaultClassLoadingPicoContainer.this;
        }
        
        public MutablePicoContainer addComponent(Object key, Object implOrInstance, ConstructorParameters constructorParams, FieldParameters[] fieldParams, MethodParameters[] methodParams) {
            delegate.addComponent(classNameToClassIfApplicable(key),
                    classNameToClassIfApplicable(implOrInstance), constructorParams, fieldParams, methodParams);
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

        public MutablePicoContainer addProvider(Provider<?> provider) {
            delegate.addProvider(provider);
            return DefaultClassLoadingPicoContainer.this;
        }
        
        public MutablePicoContainer addProvider(Object key, Provider<?> provider) {
        	delegate.addProvider(key, provider);
        	return DefaultClassLoadingPicoContainer.this;
        }

        public ComponentAdapter removeComponent(Object key) {
            return delegate.removeComponent(key);
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

        public Object getComponent(Object keyOrType) {
            return getComponentInto(keyOrType, ComponentAdapter.NOTHING.class);
        }

        public Object getComponentInto(Object keyOrType, Type into) {
            return DefaultClassLoadingPicoContainer.this.getComponentInto(keyOrType, into);
        }

        public <T> T getComponent(Class<T> componentType) {
            return DefaultClassLoadingPicoContainer.this.getComponent(Generic.get(componentType));
        }

        public <T> T getComponent(Generic<T> componentType) {
            return DefaultClassLoadingPicoContainer.this.getComponent(componentType);
        }

        public <T> T getComponentInto(Class<T> componentType, Type into) {
            return DefaultClassLoadingPicoContainer.this.getComponentInto(componentType, into);
        }

        public <T> T getComponentInto(Generic<T> componentType, Type into) {
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

        public ComponentAdapter<?> getComponentAdapter(Object key) {
            return DefaultClassLoadingPicoContainer.this.getComponentAdapter(key);
        }

        public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, NameBinding componentNameBinding) {
            return DefaultClassLoadingPicoContainer.this.getComponentAdapter(Generic.get(componentType), componentNameBinding);
        }

        public <T> ComponentAdapter<T> getComponentAdapter(Generic<T> componentType, NameBinding componentNameBinding) {
            return DefaultClassLoadingPicoContainer.this.getComponentAdapter(componentType, componentNameBinding);
        }

        public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, Class<? extends Annotation> binding) {
            return DefaultClassLoadingPicoContainer.this.getComponentAdapter(Generic.get(componentType), binding);
        }

        public <T> ComponentAdapter<T> getComponentAdapter(Generic<T> componentType, Class<? extends Annotation> binding) {
            return DefaultClassLoadingPicoContainer.this.getComponentAdapter(componentType, binding);
        }

        public Collection<ComponentAdapter<?>> getComponentAdapters() {
            return DefaultClassLoadingPicoContainer.this.getComponentAdapters();
        }

        public <T> List<ComponentAdapter<T>> getComponentAdapters(Class<T> componentType) {
            return DefaultClassLoadingPicoContainer.this.getComponentAdapters(Generic.get(componentType));
        }

        public <T> List<ComponentAdapter<T>> getComponentAdapters(Generic<T> componentType) {
            return DefaultClassLoadingPicoContainer.this.getComponentAdapters(componentType);
        }

        public <T> List<ComponentAdapter<T>> getComponentAdapters(Class<T> componentType,
                Class<? extends Annotation> binding) {
            return DefaultClassLoadingPicoContainer.this.getComponentAdapters(Generic.get(componentType), binding);
        }

        public <T> List<ComponentAdapter<T>> getComponentAdapters(Generic<T> componentType,
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
        	throw new PicoCompositionException("Cannot have  .as().start()  Register a component or delete the as() statement");
        }

        public void stop() {
        	throw new PicoCompositionException("Cannot have  .as().stop()  Register a component or delete the as() statement");
        }
        
        public void dispose() {
        	throw new PicoCompositionException("Cannot have  .as().dispose()  Register a component or delete the as() statement");
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
         * @see org.picocontainer.MutablePicoContainer#getName()
         */
        public String getName() {
            return DefaultClassLoadingPicoContainer.this.getName();
        }

        public void changeMonitor(ComponentMonitor monitor) {
            DefaultClassLoadingPicoContainer.this.changeMonitor(monitor);
        }



    }

    public int visit(ClassName thisClassesPackage, String regex, boolean recursive, ClassNameVisitor classNameVisitor) {
        Class clazz = loadClass(thisClassesPackage);
        /* File Seperator of '\\' can cause bogus results in Windows -- So we keep it to forward slash since Windows
         * can handle it.  
         * -MR
         */
        String pkgName = clazz.getPackage().getName().replace(".", "/");  
        CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
        if(codeSource == null) {
            throw new CannotListClassesInAJarException();
        }

        String codeSourceRoot = codeSource.getLocation().getFile();
        String fileName = codeSourceRoot + '/' + pkgName;
        File file = new File(fileName);
        Pattern compiledPattern = Pattern.compile(regex);
        
        if (file.exists()) {
            if (file.isFile()) {
                file = file.getParentFile();
            }
            return visit(file, pkgName, compiledPattern, recursive, classNameVisitor);
        } else {
            return visit(pkgName, codeSourceRoot, compiledPattern, recursive, classNameVisitor);
        }
    }

    public int visit(String pkgName, String codeSourceRoot, Pattern compiledPattern, boolean recursive, ClassNameVisitor classNameVisitor) {
        int found = 0;
        ZipFile zip = null;
        try {
            zip = new ZipFile(new File(codeSourceRoot));
            for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(pkgName) && entryName.endsWith(".class")) {
                    String name =  entryName.substring(pkgName.length()+1);
                    if (name.endsWith("XStream.class")) {
                        System.out.println();
                    }
                    int length = name.split("/").length;
                    if (length == 1 || recursive) {
                        found = visit(pkgName, compiledPattern, classNameVisitor, found, entryName.replace("/","."), null);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	try {
	        	if (zip != null) {
	        		zip.close();
	        	} 
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        }
        return found;
    }


    private int visit(String pkgName, Pattern pattern, ClassNameVisitor classNameVisitor, int foundSoFar, String fileName, String absolutePath) {
        boolean matches = pattern.matcher(fileName).matches();
        if (matches) {
            if (absolutePath != null) {
                String fqn = absolutePath.substring(absolutePath.indexOf(pkgName));                
                fileName = fqn.substring(0, fqn.indexOf(".class")).replace('/', '.');;
            } else {
                fileName = fileName.substring(0, fileName.indexOf(".class"));
            }
            classNameVisitor.classFound(loadClass(new ClassName(fileName)));
            foundSoFar++;
        }
        return foundSoFar;
    }
    

    public int visit(File pkgDir, String pkgName, String regex, boolean recursive, ClassNameVisitor classNameVisitor) {
        Pattern pattern = Pattern.compile(regex);
        return visit(pkgDir, pkgName, pattern, recursive, classNameVisitor);
    }

    public int visit(File pkgDir, String pkgName, Pattern pattern, boolean recursive, ClassNameVisitor classNameVisitor) {
        int found = 0;
        File files[] = pkgDir.listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (recursive) {
                        found = found + visit(file, pkgName, pattern, recursive, classNameVisitor);
                    }
                } else {
                    String name = file.getName();
                    boolean matches = pattern.matcher(name).matches();
                    if (matches) {
                        String fullPath = file.getAbsolutePath().replace('\\', '/'); //Wasted effort on *nix, but needed for windows.
                        String fqn = fullPath.substring(fullPath.indexOf(pkgName));
                        classNameVisitor.classFound(loadClass(new ClassName(fqn.substring(0, fqn.indexOf(".class")).replace("/", "."))));
                        found++;
                    }
                }
            }
        }
        return found;
    }

    public interface ClassNameVisitor {
         void classFound(Class<?> clazz);
    }

    public static class CannotListClassesInAJarException extends PicoException {

    }


}