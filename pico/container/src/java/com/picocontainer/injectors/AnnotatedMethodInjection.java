/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package com.picocontainer.injectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Properties;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.Parameter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.behaviors.AbstractBehavior;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.JSR330ComponentParameter;
import com.picocontainer.parameters.MethodParameters;

/**
 * A {@link com.picocontainer.InjectionType} for Guice-style annotated methods.
 * The factory creates {@link AnnotatedMethodInjector}.
 *
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class AnnotatedMethodInjection extends AbstractInjectionType {

	private final Class<? extends Annotation>[] injectionAnnotations;
    private final boolean useNames;

    @SuppressWarnings("unchecked")
	public AnnotatedMethodInjection(final Class<? extends Annotation> injectionAnnotation, final boolean useNames) {
        this(useNames, injectionAnnotation);
    }

    public AnnotatedMethodInjection(final boolean useNames, final Class<? extends Annotation>... injectionAnnotations) {
        this.injectionAnnotations = injectionAnnotations;
        this.useNames = useNames;
    }

    @SuppressWarnings("unchecked")
	public AnnotatedMethodInjection() {
        this(false, com.picocontainer.annotations.Inject.class, getInjectionAnnotation("javax.inject.Inject"));
    }

	/**
     * Create a {@link com.picocontainer.injectors.SetterInjection.SetterInjector}.
     *
     * @param monitor
	 * @param lifecycle
	 * @param componentProps
	 * @param key The component's key
	 * @param impl The class of the bean.
     * @return Returns a new {@link com.picocontainer.injectors.SetterInjection.SetterInjector}.
     * @throws com.picocontainer.PicoCompositionException if dependencies cannot
     *             be solved or if the implementation is an interface or an
     *             abstract class.
     */
    public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final Properties componentProps,
                                                   final Object key, final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams)
            throws PicoCompositionException {


        boolean requireConsumptionOfAllParameters = !(AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.ALLOW_UNUSED_PARAMETERS, false));

        return wrapLifeCycle(monitor.newInjector(new AnnotatedMethodInjector<T>(key, impl, methodParams, monitor, useNames, requireConsumptionOfAllParameters, injectionAnnotations)), lifecycle);
    }

     @SuppressWarnings("unchecked")
	static Class<? extends Annotation> getInjectionAnnotation(final String className) {
        try {
            return (Class<? extends Annotation>) AnnotatedMethodInjection.class.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            // JSR330 not in classpath.  No matter carry on without it with a kludge:
            return com.picocontainer.annotations.Inject.class;
        }
    }

    public static class AnnotatedMethodInjector<T> extends MethodInjection.MethodInjector<T> {

        private final Class<? extends Annotation>[] injectionAnnotations;

        private String injectionAnnotationNames;

        private transient volatile Collection<Method> injectingMethods = null;


        public AnnotatedMethodInjector(final Object key, final Class<T> impl, final MethodParameters[] parameters, final ComponentMonitor monitor,
                                       final boolean useNames, final boolean useAllParameters, final Class<? extends Annotation>... injectionAnnotations) {
            super(key, impl, monitor, "", useNames, useAllParameters, parameters);
            this.injectionAnnotations = injectionAnnotations;
        }



        @Override
        protected final boolean isInjectorMethod(final Class<?> originalType, final Method method) {

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


        static String makeAnnotationNames(final Class<? extends Annotation>[] injectionAnnotations) {
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
            AnnotationInjectionUtils.setMemberAccessible(method);
		}


		@Override
		protected Parameter constructDefaultComponentParameter() {
			return JSR330ComponentParameter.DEFAULT;
		}



		@Override
		protected boolean allowedMethodBasedOnFilter(final Class<?> injectionTypeFilter, final Method method) {
			if (injectionTypeFilter != null && ! injectionTypeFilter.equals(method.getDeclaringClass())) {
				return false;
			}

			return true;
		}

		@Override
	    protected Parameter[] interceptParametersToUse(final Parameter[] currentParameters, final AccessibleObject member) {
	    	return AnnotationInjectionUtils.interceptParametersToUse(currentParameters, member);
	    }



    }
}