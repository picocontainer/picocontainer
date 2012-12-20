package org.picocontainer.containers;

import java.lang.annotation.Annotation;

import javax.inject.Named;
import javax.inject.Qualifier;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;

@SuppressWarnings("serial")
public class JSRPicoContainer extends AbstractDelegatingMutablePicoContainer{

	public JSRPicoContainer(MutablePicoContainer delegate) {
		super(delegate);
	}
	
	@Override
	public JSRPicoContainer addComponent(Object implOrInstance) {
		Object key = determineKey(implOrInstance);
		
		addComponent(key, implOrInstance);
		return this;
	}
	
	public JSRPicoContainer addComponent(Object implOrInstance, Parameter... parameters) {
		Object key = determineKey(implOrInstance);
		
		addComponent(key, implOrInstance, parameters);
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
		Class<?> instanceClass =  (implOrInstance instanceof Class) ? (Class)implOrInstance : implOrInstance.getClass();
		
		
		Object key = instanceClass;
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
	
	
	/**
	 * Covariant return override;
	 */
	public JSRPicoContainer addComponent(Object key,
            Object implOrInstance,
            Parameter... parameters) throws PicoCompositionException {
		 super.addComponent(key, implOrInstance,parameters);
		 return this;
	}

	@Override
	public MutablePicoContainer makeChildContainer() {
		MutablePicoContainer childDelegate = getDelegate().makeChildContainer();
		return new JSRPicoContainer(childDelegate);
	}

	
}
