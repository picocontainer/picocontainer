/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.injectors;

import static org.picocontainer.injectors.AnnotatedMethodInjection.getInjectionAnnotation;
import static org.picocontainer.injectors.AnnotatedMethodInjection.AnnotatedMethodInjector.makeAnnotationNames;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Named;

import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.annotations.Bind;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.containers.JSRPicoContainer;
import org.picocontainer.parameters.AccessibleObjectParameterSet;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.JSR330ComponentParameter;
import org.picocontainer.parameters.MethodParameters;


/**
 * A {@link org.picocontainer.InjectionType} for Guice-style annotated fields.
 * The factory creates {@link AnnotatedFieldInjector}.
 *
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class AnnotatedFieldInjection extends AbstractInjectionType {

	private final Class<? extends Annotation>[] injectionAnnotations;
	

    public AnnotatedFieldInjection(Class<? extends Annotation>... injectionAnnotations) {
        this.injectionAnnotations = injectionAnnotations;
    }

    @SuppressWarnings("unchecked")
	public AnnotatedFieldInjection() {
    	this(getInjectionAnnotation("javax.inject.Inject"), getInjectionAnnotation("org.picocontainer.annotations.Inject"));
    }
    

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor,
                                                   LifecycleStrategy lifecycle,
                                                   Properties componentProps,
                                                   Object key,
                                                   Class<T> impl,
                                                   ConstructorParameters constructorParams, FieldParameters[] fieldParams, MethodParameters[] methodParams) throws PicoCompositionException {
        boolean useNames = AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.USE_NAMES, true);
        
        boolean requireConsumptionOfAllParameters = !(AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.ALLOW_UNUSED_PARAMETERS, false));
        
        return wrapLifeCycle(monitor.newInjector(new AnnotatedFieldInjector(key, impl, fieldParams, monitor,
                useNames, requireConsumptionOfAllParameters, injectionAnnotations)), lifecycle);
    }

    /**
     * Injection happens after instantiation, and through fields marked as injection points via an Annotation.
     * The default annotation of org.picocontainer.annotations.@Inject can be overridden.
     */
    public static class AnnotatedFieldInjector<T> extends AbstractFieldInjector<T> {

        private final Class<? extends Annotation>[] injectionAnnotations;
        private String injectionAnnotationNames;

        public AnnotatedFieldInjector(Object key, Class<T> impl, FieldParameters[] parameters, ComponentMonitor monitor,
                                      boolean useNames, boolean requireConsumptionOfAllParameters, Class<? extends Annotation>... injectionAnnotations) {

            super(key, impl, monitor, useNames, requireConsumptionOfAllParameters, parameters);
            this.injectionAnnotations = injectionAnnotations;
        }

        @Override
        protected void initializeInjectionMembersAndTypeLists() {
            injectionMembers = new ArrayList<AccessibleObject>();
            List<Annotation> bindingIds = new ArrayList<Annotation>();
            final List<Type> typeList = new ArrayList<Type>();
            Class<?> drillInto = getComponentImplementation();
            while (drillInto != Object.class) {
                final Field[] fields = getFields(drillInto);
                for (final Field field : fields) {
                    if (isAnnotatedForInjection(field)) {
                        injectionMembers.add(field);
                        typeList.add(box(field.getGenericType()));
                        bindingIds.add(getBinding(field));
                    }
                }
                drillInto = drillInto.getSuperclass();
            }
            injectionTypes = typeList.toArray(new Type[0]);
            bindings = bindingIds.toArray(new Annotation[0]);
        }

        private Annotation getBinding(Field field) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().isAnnotationPresent(Bind.class)) {
                    return annotation;
                }
            }
            return null;
        }

        protected final boolean isAnnotatedForInjection(Field field) {
            for (Class<? extends Annotation> injectionAnnotation : injectionAnnotations) {
                if (field.isAnnotationPresent(injectionAnnotation)) {
                    return true;
                }
            }
            return false;
        }
        

        private Field[] getFields(final Class<?> clazz) {
            return AccessController.doPrivileged(new PrivilegedAction<Field[]>() {
                public Field[] run() {
                    return clazz.getDeclaredFields();
                }
            });
        }
        
        /**
         * Allows Different swapping of types.
         * @return
         */
        @Override
        protected Parameter constructDefaultComponentParameter() {
        	return JSR330ComponentParameter.DEFAULT;
        }
                

        /**
         * Performs the actual injection.
         */
        @Override
        protected Object injectIntoMember(AccessibleObject member, Object componentInstance, Object toInject)
                throws IllegalAccessException, InvocationTargetException {
            final Field field = (Field) member;
            
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
				public Void run() {
		            field.setAccessible(true);
		            return null;
				}
            });
            
            field.set(componentInstance, toInject);
            return null;
        }
        
        /**
         * Allows swapping of parameter to a component parameter specified by {@linkplain javax.inject.Named} annotations
         * or different JSR330-based qualifiers
         * <p>{@inheritDoc}</p>
         */
        @Override
        protected AccessibleObjectParameterSet getParameterToUseForObject(final AccessibleObject targetInjectionMember, final AccessibleObjectParameterSet... currentParameter) {
        	if (currentParameter == null || currentParameter.length == 0) {
        		return null;
        	}
        	
        	//Field injection only handles one parameter per accessible object.
        	AccessibleObjectParameterSet targetParameter = currentParameter[0];
        	
        	//Allow composition script operator to override what's in code.  
        	//TODO:  Is this a good idea?  @Named is a horrible way to lock in the code, so this provides
        	//flexibility if you're stuck with code you can't change.... but it might
        	//make for some wild bugs where maintenance programmers only see the @Named annotation
        	//and look no further.  -MR
			Field targetField = (Field)targetInjectionMember;
        	if (targetParameter.getParams()[0] == ComponentParameter.DEFAULT || targetParameter.getParams()[0] == JSR330ComponentParameter.DEFAULT) {
        		if (targetInjectionMember.isAnnotationPresent(Named.class)) {
        			Named annotation = targetInjectionMember.getAnnotation(Named.class);
        					
        			ComponentParameter newParameter = new ComponentParameter(annotation.value());
        			return new AccessibleObjectParameterSet(targetField.getDeclaringClass(), targetField.getName(), newParameter);
        		}
        		
        		Annotation qualifier = JSRPicoContainer.getQualifier(targetInjectionMember.getAnnotations());
        		if (qualifier != null) {
        			ComponentParameter newParameter = new ComponentParameter(qualifier.annotationType().getName());
        			return new AccessibleObjectParameterSet(targetField.getDeclaringClass(), targetField.getName(), newParameter);
        		}
        		
        	}
        	
        	return targetParameter;
    	}        
        

        @Override
        public String getDescriptor() {
            if (injectionAnnotationNames == null) {
                injectionAnnotationNames = makeAnnotationNames(injectionAnnotations);
            }
            return "AnnotatedFieldInjector["+injectionAnnotationNames+"]-";
        }

        
        @Override
        protected NameBinding makeParameterNameImpl(final AccessibleObject member) { 
            return new NameBinding() {
                public String getName() {
                    return ((Field) member).getName();
                }
            };
        }

        protected Object memberInvocationReturn(Object lastReturn, AccessibleObject member, Object instance) {
            return instance;
        }
    }
}
