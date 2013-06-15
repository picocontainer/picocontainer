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
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.annotations.Bind;
import org.picocontainer.behaviors.AbstractBehavior;
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


    public AnnotatedFieldInjection(final Class<? extends Annotation>... injectionAnnotations) {
        this.injectionAnnotations = injectionAnnotations;
    }

    @SuppressWarnings("unchecked")
	public AnnotatedFieldInjection() {
    	this(getInjectionAnnotation("javax.inject.Inject"), getInjectionAnnotation("org.picocontainer.annotations.Inject"));
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor,
                                                   final LifecycleStrategy lifecycle,
                                                   final Properties componentProps,
                                                   final Object key,
                                                   final Class<T> impl,
                                                   final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
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

        public AnnotatedFieldInjector(final Object key, final Class<T> impl, final FieldParameters[] parameters, final ComponentMonitor monitor,
                                      final boolean useNames, final boolean requireConsumptionOfAllParameters, final Class<? extends Annotation>... injectionAnnotations) {

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
                	if (Modifier.isStatic(field.getModifiers())) {
                		continue;
                	}

                    if (isAnnotatedForInjection(field)) {
                        injectionMembers.add(field);
                    }
                }
                drillInto = drillInto.getSuperclass();
            }

            //Sort for injection.
            Collections.sort(injectionMembers, new JSRAccessibleObjectOrderComparator());
            for (AccessibleObject eachMember : injectionMembers) {
            	Field field = (Field)eachMember;
                typeList.add(box(field.getGenericType()));
                bindingIds.add(getBinding(field));

            }

            injectionTypes = typeList.toArray(new Type[0]);
            bindings = bindingIds.toArray(new Annotation[0]);

        }

        /**
         * Sorry, can't figure out how else to test injection member order without
         * this function or some other ugly hack to get at the private data structure.
         * At least I made it read only?  :D  -MR
         * @return
         */
        @SuppressWarnings("unchecked")
		public List<AccessibleObject> getInjectionMembers() {
        	return injectionMembers != null ? Collections.unmodifiableList(injectionMembers) : Collections.EMPTY_LIST;
        }

        public static Annotation getBinding(final Field field) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().isAnnotationPresent(Bind.class)) {
                    return annotation;
                }
            }
            return null;
        }

        protected final boolean isAnnotatedForInjection(final Field field) {
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
        protected Object injectIntoMember(final AccessibleObject member, final Object componentInstance, final Object toInject)
                throws IllegalAccessException, InvocationTargetException {
            final Field field = (Field) member;

            AnnotationInjectionUtils.setMemberAccessible(member);

            field.set(componentInstance, toInject);
            return null;
        }



		@Override
		protected Parameter[] interceptParametersToUse(final Parameter[] currentParameters, final AccessibleObject member) {
			return AnnotationInjectionUtils.interceptParametersToUse(currentParameters, member);
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

        @Override
		protected Object memberInvocationReturn(final Object lastReturn, final AccessibleObject member, final Object instance) {
            return instance;
        }

    }
}
