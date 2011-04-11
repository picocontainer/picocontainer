package org.picocontainer.parameters;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;

import com.googlecode.jtype.Generic;

/**
 * Will match all objects as parameters that have the given class-based annotation marker. 
 * <h2>Example</h2>
 * Assuming the given classes:
 * <pre>
 * {@literal @Singleton}
 * class A {
 * 
 * }
 * 
 * {@literal @Singleton}
 * class B {
 * 
 * }
 * 
 * class C {
 * 
 * }
 * 
 * public class SingletonRegistry {
 * 
 * 		public Object[] allSingletons;
 * 
 * 		public SingletonRegistry(Object[] allSingletons) {
 * 			this.allSingletons = allSingletons;
 *      }
 * }
 * </pre>
 * <p>In your assembly script then use:</p>
 * <pre>
 *     MutablePicoContainer pico = new PicoBuilder().build();
 *     pico.addComponent(A.class)
 *     		.addClass(B.class)
 *     		.addClass(C.class)
 *     		.addClass(SingletonRegistry.class, SingletonRegistry.class,
 *           	//Collect all classes marked singleton.
 *        	 	new AnnotationCollectionParameter(Singleton.class);
 *     
 *     SingletonRegistry registry = pico.getComponent(SingletonRegistry.class);
 *     //Should have A &amp; B, but not C.
 *     assertEquals(2,registry.allSingletons.length);
 * </pre>
 * 
 * @author Michael Rimov
 */
@SuppressWarnings("serial")
public class AnnotationCollectionComponentParameter extends CollectionComponentParameter
		implements Parameter, Serializable {

	private final Class<? extends Annotation> annotationType;

	public AnnotationCollectionComponentParameter(Class<? extends Annotation> annotationType) {
		super();
		this.annotationType = annotationType;
	}

	public AnnotationCollectionComponentParameter(Class<? extends Annotation> annotationType,boolean emptyCollection) {
		super(emptyCollection);
		this.annotationType = annotationType;
		
	}

	public AnnotationCollectionComponentParameter(Class<? extends Annotation> annotationType, Class<?> keyType,
			Generic<?> componentValueType, boolean emptyCollection) {
		super(keyType, componentValueType, emptyCollection);
		this.annotationType = annotationType;
	}

	public AnnotationCollectionComponentParameter(Class<? extends Annotation> annotationType, 
			Generic<?> componentValueType, boolean emptyCollection) {
		super(componentValueType, emptyCollection);
		this.annotationType = annotationType;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected boolean evaluate(ComponentAdapter adapter) {
		return (adapter != null 
				&& adapter.getComponentImplementation().isAnnotationPresent(annotationType));
	}

}
