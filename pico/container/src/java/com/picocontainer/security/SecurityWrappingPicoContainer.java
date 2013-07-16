package com.picocontainer.security;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.security.AccessControlException;
import java.security.Permission;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.inject.Provider;

import com.googlecode.jtype.Generic;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.NameBinding;
import com.picocontainer.Parameter;
import com.picocontainer.PicoContainer;
import com.picocontainer.PicoVisitor;
import com.picocontainer.lifecycle.LifecycleState;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;

public final class SecurityWrappingPicoContainer implements MutablePicoContainer {

	private static final class DefaultAccessWrapper implements AccessControllerWrapper {
		public void checkPermission(final Permission checkingPermission) throws AccessControlException {
			 SecurityManager security = System.getSecurityManager();
		     if (security != null) {
		    	 security.checkPermission(checkingPermission);
		     }
		}
	}
	
	private final MutablePicoContainer pico;
	
	private final Permission readCheck;
	
	private final Permission writeCheck;
	
	
	
	private final AccessControllerWrapper accessWrapper;

	public SecurityWrappingPicoContainer(String scope, MutablePicoContainer pico) {
		if (scope == null) {
			scope = "";
		}
		
		
		this.pico = pico;
		if (pico == null) {
			throw new NullPointerException("pico");
		}
		
		readCheck = new PicoAccessPermission(scope, PicoAccessPermission.READ);
		writeCheck = new PicoAccessPermission(scope, PicoAccessPermission.WRITE);
		
		accessWrapper = new DefaultAccessWrapper();
					
	}
	
	
	
	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read-write</tt> permission.
	 */
	public void accept(PicoVisitor visitor) {
		checkReadWritePermission();
		visitor.visitContainer(this);
		pico.accept(visitor);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public MutablePicoContainer addAdapter(ComponentAdapter<?> componentAdapter) {
		checkWritePermission();
		return pico.addAdapter(componentAdapter);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public MutablePicoContainer addChildContainer(PicoContainer child) {
		checkWritePermission();
		return pico.addChildContainer(child);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public MutablePicoContainer addComponent(Object implOrInstance) {
		checkWritePermission();
		return pico.addComponent(implOrInstance);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public MutablePicoContainer addComponent(Object key, Object implOrInstance,
			ConstructorParameters constructorParams, FieldParameters[] fieldParams, MethodParameters[] methodParams) {
		checkWritePermission();
		return pico.addComponent(key, implOrInstance, constructorParams, fieldParams, methodParams);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public MutablePicoContainer addComponent(Object key, Object implOrInstance, Parameter... constructorParameters) {
		checkWritePermission();
		return pico.addComponent(key, implOrInstance, constructorParameters);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public MutablePicoContainer addConfig(String name, Object val) {
		checkWritePermission();
		return pico.addConfig(name, val);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public MutablePicoContainer addProvider(Object key, Provider<?> provider) {
		checkWritePermission();
		return pico.addProvider(key, provider);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public MutablePicoContainer addProvider(Provider<?> provider) {
		checkWritePermission();
		return pico.addProvider(provider);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public MutablePicoContainer as(Properties... properties) {
		checkWritePermission();
		return pico.as(properties);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public <T> BindWithOrTo<T> bind(Class<T> type) {
		checkWritePermission();
		return pico.bind(type);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public MutablePicoContainer change(Properties... properties) {
		checkWritePermission();
		return pico.change(properties);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public ComponentMonitor changeMonitor(ComponentMonitor monitor) {
		checkWritePermission();
		return pico.changeMonitor(monitor);
	}

	private void checkReadPermission() {
		accessWrapper.checkPermission(readCheck);
		
	}

	private void checkReadWritePermission() {
		checkReadPermission();
		checkWritePermission();
		
	}

	private void checkWritePermission() {
		accessWrapper.checkPermission(writeCheck);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public void dispose() {
		checkWritePermission();
		pico.dispose();
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public <T> T getComponent(Class<T> componentType) {
		checkReadPermission();
		return pico.getComponent(componentType);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public <T> T getComponent(Class<T> componentType, Class<? extends Annotation> binding) {
		checkReadPermission();
		return pico.getComponent(componentType, binding);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public <T> T getComponent(Class<T> componentType, Class<? extends Annotation> binding, Type into) {
		checkReadPermission();
		return pico.getComponent(componentType, binding, into);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public <T> T getComponent(Generic<T> componentType) {
		checkReadPermission();
		return pico.getComponent(componentType);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public Object getComponent(Object keyOrType) {
		checkReadPermission();
		return pico.getComponent(keyOrType);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, Class<? extends Annotation> binding) {
		checkReadPermission();
		return pico.getComponentAdapter(componentType, binding);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public <T> ComponentAdapter<T> getComponentAdapter(Class<T> componentType, NameBinding nameBinding) {
		checkReadPermission();
		return pico.getComponentAdapter(componentType, nameBinding);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public <T> ComponentAdapter<T> getComponentAdapter(Generic<T> componentType, Class<? extends Annotation> binding) {
		checkReadPermission();
		return pico.getComponentAdapter(componentType, binding);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public <T> ComponentAdapter<T> getComponentAdapter(Generic<T> componentType, NameBinding nameBinding) {
		checkReadPermission();
		return pico.getComponentAdapter(componentType, nameBinding);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public ComponentAdapter<?> getComponentAdapter(Object key) {
		checkReadPermission();
		return pico.getComponentAdapter(key);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public Collection<ComponentAdapter<?>> getComponentAdapters() {
		checkReadPermission();
		return pico.getComponentAdapters();
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public <T> List<ComponentAdapter<T>> getComponentAdapters(Class<T> componentType) {
		checkReadPermission();
		return pico.getComponentAdapters(componentType);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public <T> List<ComponentAdapter<T>> getComponentAdapters(Class<T> componentType,
			Class<? extends Annotation> binding) {
		checkReadPermission();
		return pico.getComponentAdapters(componentType, binding);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public <T> List<ComponentAdapter<T>> getComponentAdapters(Generic<T> componentType) {
		checkReadPermission();
		return pico.getComponentAdapters(componentType);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public <T> List<ComponentAdapter<T>> getComponentAdapters(Generic<T> componentType,
			Class<? extends Annotation> binding) {
		checkReadPermission();
		return pico.getComponentAdapters(componentType, binding);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public <T> T getComponentInto(Class<T> componentType, Type into) {
		checkReadPermission();
		return pico.getComponentInto(componentType, into);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public <T> T getComponentInto(Generic<T> componentType, Type into) {
		checkReadPermission();
		return pico.getComponentInto(componentType, into);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public Object getComponentInto(Object keyOrType, Type into) {
		checkReadPermission();
		return pico.getComponentInto(keyOrType, into);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public List<Object> getComponents() {
		checkReadPermission();
		return pico.getComponents();
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public <T> List<T> getComponents(Class<T> componentType) {
		checkReadPermission();
		return pico.getComponents(componentType);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public LifecycleState getLifecycleState() {
		checkReadPermission();		
		return pico.getLifecycleState();
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public String getName() {
		checkReadPermission();
		return pico.getName();
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>read</tt> permission.
	 */
	public PicoContainer getParent() {
		checkReadPermission();
		return pico.getParent();
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public MutablePicoContainer makeChildContainer() {
		checkWritePermission();
		return pico.makeChildContainer();
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public boolean removeChildContainer(PicoContainer child) {
		checkWritePermission();
		return pico.removeChildContainer(child);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public <T> ComponentAdapter<T> removeComponent(Object key) {
		checkWritePermission();
		return pico.removeComponent(key);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public <T> ComponentAdapter<T> removeComponentByInstance(T componentInstance) {
		checkWritePermission();
		return pico.removeComponentByInstance(componentInstance);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public void setLifecycleState(LifecycleState lifecycleState) {
		checkWritePermission();
		pico.setLifecycleState(lifecycleState);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public void setName(String name) {
		checkWritePermission();
		pico.setName(name);
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public void start() {
		checkWritePermission();
		pico.start();
	}

	/**
	 * {@inheritDoc}
	 * <p>Requires <tt>write</tt> permission.
	 */
	public void stop() {
		checkWritePermission();
		pico.stop();
	}
	
}
