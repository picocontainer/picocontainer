/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package org.picocontainer.injectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.inject.Named;

import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.containers.JSRPicoContainer;
import org.picocontainer.parameters.AccessibleObjectParameterSet;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.JSR330ComponentParameter;
import org.picocontainer.parameters.MethodParameters;

import com.thoughtworks.xstream.annotations.Annotations;

/**
 * A {@link org.picocontainer.InjectionType} for Guice-style annotated methods.
 * The factory creates {@link AnnotatedMethodInjector}.
 *
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class AnnotatedMethodInjection extends AbstractInjectionType {

	private final Class<? extends Annotation>[] injectionAnnotations;
    private final boolean useNames;

    @SuppressWarnings("unchecked")
	public AnnotatedMethodInjection(Class<? extends Annotation> injectionAnnotation, boolean useNames) {
        this(useNames, injectionAnnotation);
    }

    public AnnotatedMethodInjection(boolean useNames, Class<? extends Annotation>... injectionAnnotations) {
        this.injectionAnnotations = injectionAnnotations;
        this.useNames = useNames;
    }

    @SuppressWarnings("unchecked")
	public AnnotatedMethodInjection() {
        this(false, org.picocontainer.annotations.Inject.class, getInjectionAnnotation("javax.inject.Inject"));
    }

	/**
     * Create a {@link org.picocontainer.injectors.SetterInjection.SetterInjector}.
     * 
     * @param monitor
	 * @param lifecycle
	 * @param componentProps
	 * @param key The component's key
	 * @param impl The class of the bean.
     * @return Returns a new {@link org.picocontainer.injectors.SetterInjection.SetterInjector}.
     * @throws org.picocontainer.PicoCompositionException if dependencies cannot
     *             be solved or if the implementation is an interface or an
     *             abstract class.
     */
    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycle, Properties componentProps,
                                                   Object key, Class<T> impl, ConstructorParameters constructorParams, FieldParameters[] fieldParams, MethodParameters[] methodParams)
            throws PicoCompositionException {
    	

        boolean requireConsumptionOfAllParameters = !(AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.ALLOW_UNUSED_PARAMETERS, false));
    	
        return wrapLifeCycle(monitor.newInjector(new AnnotatedMethodInjector<T>(key, impl, methodParams, monitor, useNames, requireConsumptionOfAllParameters, injectionAnnotations)), lifecycle);
    }

     @SuppressWarnings("unchecked")
	static Class<? extends Annotation> getInjectionAnnotation(String className) {
        try {
            return (Class<? extends Annotation>) AnnotatedMethodInjection.class.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            // JSR330 not in classpath.  No matter carry on without it with a kludge:
            return org.picocontainer.annotations.Inject.class;
        }
    }

    public static class AnnotatedMethodInjector<T> extends MethodInjection.MethodInjector<T> {

        private final Class<? extends Annotation>[] injectionAnnotations;
        
        private String injectionAnnotationNames;
        
        private transient volatile Collection<Method> injectingMethods = null;
        

        public AnnotatedMethodInjector(Object key, Class<T> impl, MethodParameters[] parameters, ComponentMonitor monitor,
                                       boolean useNames, boolean useAllParameters, Class<? extends Annotation>... injectionAnnotations) {
            super(key, impl, monitor, "", useNames, useAllParameters, parameters);
            this.injectionAnnotations = injectionAnnotations;
        }
        
        

        @Override
        protected final boolean isInjectorMethod(Class<?> originalType, Method method) {
        	
        	if (injectingMethods == null) {
        		synchronized (this) {
        			if (injectingMethods == null) {
        				injectingMethods = new InjectableMethodSelector(injectionAnnotations).retreiveAllInjectableMethods(originalType) ;
        			}        			
        		}
        	}
        	
        	if (injectingMethods.contains(method)) {
        		return true;
        	}
        	
            return false;
        }
       
     
		@Override
        public synchronized String getDescriptor() {
            if (injectionAnnotationNames == null) {
                injectionAnnotationNames = makeAnnotationNames(injectionAnnotations);
            }
            return "AnnotatedMethodInjector[" + injectionAnnotationNames + "]-";
        }

		
        static String makeAnnotationNames(Class<? extends Annotation>[] injectionAnnotations) {
            StringBuilder sb = new StringBuilder();
            for (Class<? extends Annotation> injectionAnnotation : injectionAnnotations) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                String name = injectionAnnotation.getName();
                sb.append(name.substring(0, name.lastIndexOf(".")+1)).append("@").append(name.substring(name.lastIndexOf(".")+1));
            }
            return sb.toString();
        }



		@Override
		protected void makeAccessibleIfDesired(final Method method) {
			if (Modifier.isPublic(method.getModifiers())) {
				return;
			}
			AccessController.doPrivileged(new PrivilegedAction<Void>() {

				public Void run() {
					method.setAccessible(true);
					return null;
				}
				
			});
		}
		
       
		protected Parameter[] interceptParametersToUse(final Parameter[] currentParameters, AccessibleObject member) {
			Method targetMethod = (Method)member;
			Annotation[][] allAnnotations = targetMethod.getParameterAnnotations();
			
			if (currentParameters.length != allAnnotations.length) {
				throw new PicoCompositionException("Internal error, parameter lengths, not the same as the annotation lengths");
			}
			
			//Make this function side-effect free.
			Parameter[] returnValue = Arrays.copyOf(currentParameters, currentParameters.length);
			
			
			for (int i = 0; i < returnValue.length; i++) {
				//Allow composition scripts to override annotations 
				//See comment in org.picocontainer.injectors.AnnotatedFieldInjection.AnnotatedFieldInjector.getParameterToUseForObject(AccessibleObject, AccessibleObjectParameterSet...)
				//for possible issues with this.
				if (returnValue[i] != ComponentParameter.DEFAULT && returnValue[i] != JSR330ComponentParameter.DEFAULT) {
					continue;
				}
				
				Named namedAnnotation = getNamedAnnotation(allAnnotations[i]);
        		if (namedAnnotation != null) {
        			returnValue[i] = new ComponentParameter(namedAnnotation.value());
        		} else {
            		Annotation qualifier = JSRPicoContainer.getQualifier(allAnnotations[i]);
            		if (qualifier != null) {
            			returnValue[i] = new ComponentParameter(qualifier.annotationType().getName());
            		}
        		}

        		//Otherwise don't modify it.
			}
			
			return returnValue;
		}
		
		@Override
		protected Parameter constructDefaultComponentParameter() {
			return JSR330ComponentParameter.DEFAULT;
		}


		private Named getNamedAnnotation(Annotation[] annotations) {
			for (Annotation eachAnnotation : annotations) {
				if (eachAnnotation.annotationType().equals(Named.class)) {
					return (Named) eachAnnotation;
				}
			}
			return null;
		}
		
    }
}