package org.picocontainer.containers;

import java.lang.annotation.Annotation;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.AdaptingBehavior;
import org.picocontainer.behaviors.OptInCaching;
import org.picocontainer.injectors.ProviderAdapter;
import org.picocontainer.lifecycle.JavaEE5LifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

/**
 * This class requires the JSR330 JAR to be in the classpath upon load.  It provides automatic
 * key generation based on {@link javax.inject.Named Named} and {@link javax.inject.Qualifier Qualifier} annotations.  It
 * handles JSR compliant {@link javax.inject.Provider Provider} classes, and supports turning
 * on caching for {@link javax.inject.Singleton Singleton} marked classes.
 * @author Michael Rimov
 *
 */
@SuppressWarnings("serial")
public class JSRPicoContainer extends AbstractDelegatingMutablePicoContainer{

	/**
	 * Wraps a {@link org.picocontainer.DefaultPicoContainer DefaultPicoContainer} with Opt-in caching
	 */
	public JSRPicoContainer() {
		this(new NullComponentMonitor());
	}
	
	public JSRPicoContainer(PicoContainer parent) {
		this(parent, new NullComponentMonitor());
	}
	
	public JSRPicoContainer(ComponentMonitor monitor) {
		this(null, monitor);
	}
	
	public JSRPicoContainer(PicoContainer parent, ComponentMonitor monitor) {
		super(new DefaultPicoContainer(parent, new JavaEE5LifecycleStrategy(monitor), monitor, new OptInCaching(), new AdaptingBehavior()));
	}

	/**
	 * Allows you to wrap automatic-key generation and 
	 * @param delegate
	 */
	public JSRPicoContainer(MutablePicoContainer delegate) {
		super(delegate);
	}
	
	/**
	 * Necessary adapter to fit MutablePicoContainer interface.
	 */
	@Override
	public JSRPicoContainer addComponent(Object implOrInstance) {
		Object key = determineKey(implOrInstance);
		
		addComponent(key, implOrInstance);
		return this;
	}
	
	/**
	 * Method that determines the key of the object by examining the implementation for
	 * JSR 330 annotations.  If none are found,the implementation's class name is used
	 * as the key.
	 * 
	 * @param implOrInstance
	 * @param parameters
	 * @return
	 */
	public JSRPicoContainer addComponent(Object implOrInstance, Parameter... parameters) {
		Object key = determineKey(implOrInstance);
		
		addComponent(key, implOrInstance, parameters);
		return this;
	}
	
	/**
	 * 
	 * @param implOrInstance
	 * @param constructorParams
	 * @param fieldParams
	 * @param methodParams
	 * @return
	 */
	public JSRPicoContainer addComponent(Object implOrInstance, ConstructorParameters constructorParams, FieldParameters[] fieldParams, MethodParameters[] methodParams) {
		Object key = determineKey(implOrInstance);
		
		addComponent(key, implOrInstance, constructorParams, fieldParams, methodParams);
		return this;
	}

	

	/**
	 * Determines the key of the object.  It may have JSR 330 qualifiers or {@linkplain javax.inject.Named} annotations.
	 * If none of these exist, the key is the object's class.
	 * @param implOrInstance either an actual object instance or more often a class to be constructed and injection
	 * by the container.
	 * @return the object's determined key. 
	 */
	protected Object determineKey(Object implOrInstance) {
		if (implOrInstance == null) {
			throw new NullPointerException("implOrInstance");
		}
		
		Class<?> instanceClass =  (implOrInstance instanceof Class) ? (Class)implOrInstance : implOrInstance.getClass();
		
		//Determine the key based on the provider's return type
		Object key;
		if (implOrInstance instanceof javax.inject.Provider || implOrInstance instanceof org.picocontainer.injectors.Provider) {
			key = ProviderAdapter.determineProviderReturnType(implOrInstance);			
		} else {
			key = instanceClass;
		}
		
		//BUT, Named annotation or a Qualifier annotation will 
		//override the normal value;
		if (instanceClass.isAnnotationPresent(Named.class)) {
			key = instanceClass.getAnnotation(Named.class).value();
		} else {
			Annotation qualifier = getQualifier(instanceClass.getAnnotations());
			if (qualifier != null) {
				key = qualifier.annotationType().getName();
			}
		}
		
		return key;
	}

	/**
	 * Retrieves the qualifier among the annotations that a particular field/method/class has.  Returns the first
	 * qualifier it finds or null if no qualifiers seem to exist.
	 * @param attachedAnnotation
	 * @return
	 */
	public static Annotation getQualifier(Annotation[] attachedAnnotation) {
		for (Annotation eachAnnotation: attachedAnnotation) {
			if (eachAnnotation.annotationType().isAnnotationPresent(Qualifier.class)) {
				return eachAnnotation;
			}
		}
		
		return null;
	}
	
	
	@Override
	public MutablePicoContainer makeChildContainer() {
		MutablePicoContainer childDelegate = getDelegate().makeChildContainer();
		return new JSRPicoContainer(childDelegate);
	}

	@Override
	public MutablePicoContainer addProvider(Provider<?> provider) {
		Object key = determineKey(provider);
		super.addProvider(key, provider);
		return this;
	}
	
	protected void applyInstanceAnnotations(Class<?> objectImplementation) {
		if (objectImplementation.isAnnotationPresent(Singleton.class)) {
			as(Characteristics.CACHE);
		}
	}

	/**
	 * Covariant return override;
	 */
	public JSRPicoContainer addComponent(Object key,
            Object implOrInstance,
            Parameter... parameters) throws PicoCompositionException {
		if (key == null) {
			throw new NullPointerException("key");
		}
		
		if (implOrInstance == null) {
			throw new NullPointerException("implOrInstance");
		}
		
		applyInstanceAnnotations( implOrInstance instanceof Class ? (Class<?>)implOrInstance : implOrInstance.getClass() );
		super.addComponent(key, implOrInstance, parameters);
		return this;
	}
	
	



	@Override
	public MutablePicoContainer addComponent(Object key, Object implOrInstance,
			ConstructorParameters constructorParams, FieldParameters[] fieldParams, MethodParameters[] methodParams) {
		if (implOrInstance == null) {
			throw new NullPointerException("implOrInstance");
		}
		
		applyInstanceAnnotations( implOrInstance instanceof Class ? (Class<?>)implOrInstance : implOrInstance.getClass() );

		super.addComponent(key, implOrInstance, constructorParams, fieldParams, methodParams);
		return this;
	}
	
	
	
}
