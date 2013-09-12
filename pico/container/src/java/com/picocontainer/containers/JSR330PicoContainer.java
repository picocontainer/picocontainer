package com.picocontainer.containers;

import java.lang.annotation.Annotation;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.Parameter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.behaviors.AdaptingBehavior;
import com.picocontainer.behaviors.OptInCaching;
import com.picocontainer.injectors.ProviderAdapter;
import com.picocontainer.injectors.StaticsInitializedReferenceSet;
import com.picocontainer.lifecycle.JavaEE5LifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;

/**
 * This class requires the JSR330 JAR to be in the classpath upon load.  It provides automatic
 * key generation based on {@link javax.inject.Named Named} and {@link javax.inject.Qualifier Qualifier} annotations.  It
 * handles JSR compliant {@link javax.inject.Provider Provider} classes, and supports turning
 * on caching for {@link javax.inject.Singleton Singleton} marked classes.
 * @author Michael Rimov
 *
 */
@SuppressWarnings("serial")
public class JSR330PicoContainer extends AbstractDelegatingMutablePicoContainer{

	/**
	 * Wraps a {@link com.picocontainer.DefaultPicoContainer DefaultPicoContainer} with Opt-in caching
	 */
	public JSR330PicoContainer() {
		this(new NullComponentMonitor());
	}

	public JSR330PicoContainer(final PicoContainer parent) {
		this(parent, new NullComponentMonitor(), new StaticsInitializedReferenceSet());
	}

	public JSR330PicoContainer(final ComponentMonitor monitor) {
		this(null, monitor, new StaticsInitializedReferenceSet());
	}

	public JSR330PicoContainer(final PicoContainer parent, final ComponentMonitor monitor, final StaticsInitializedReferenceSet referenceSet) {
		super(new DefaultPicoContainer(parent, new JavaEE5LifecycleStrategy(monitor), monitor, new OptInCaching(), new AdaptingBehavior(referenceSet)));
	}

	/**
	 * Allows you to wrap automatic-key generation and
	 * @param delegate
	 */
	public JSR330PicoContainer(final MutablePicoContainer delegate) {
		super(delegate);
	}

	/**
	 * Necessary adapter to fit MutablePicoContainer interface.
	 */
	@Override
	public JSR330PicoContainer addComponent(final Object implOrInstance) {
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
	public JSR330PicoContainer addComponent(final Object implOrInstance, final Parameter... parameters) {
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
	public JSR330PicoContainer addComponent(final Object implOrInstance, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) {
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
	protected Object determineKey(final Object implOrInstance) {
		if (implOrInstance == null) {
			throw new NullPointerException("implOrInstance");
		}

		Class<?> instanceClass =  (implOrInstance instanceof Class) ? (Class<?>)implOrInstance : implOrInstance.getClass();

		//Determine the key based on the provider's return type
		Object key;
		if (implOrInstance instanceof javax.inject.Provider || implOrInstance instanceof com.picocontainer.injectors.Provider) {
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
	public static Annotation getQualifier(final Annotation[] attachedAnnotation) {
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
		return new JSR330PicoContainer(childDelegate);
	}

	@Override
	public MutablePicoContainer addProvider(final Provider<?> provider) {
		Object key = determineKey(provider);
		super.addProvider(key, provider);
		return this;
	}

	protected void applyInstanceAnnotations(final Class<?> objectImplementation) {
		if (objectImplementation.isAnnotationPresent(Singleton.class)) {
			as(Characteristics.CACHE);
		}
	}

	/**
	 * Covariant return override;
	 */
	@Override
	public JSR330PicoContainer addComponent(final Object key,
            final Object implOrInstance,
            final Parameter... parameters) throws PicoCompositionException {
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
	public MutablePicoContainer addComponent(final Object key, final Object implOrInstance,
			final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) {
		if (implOrInstance == null) {
			throw new NullPointerException("implOrInstance");
		}

		applyInstanceAnnotations( implOrInstance instanceof Class ? (Class<?>)implOrInstance : implOrInstance.getClass() );

		super.addComponent(key, implOrInstance, constructorParams, fieldParams, methodParams);
		return this;
	}



}
