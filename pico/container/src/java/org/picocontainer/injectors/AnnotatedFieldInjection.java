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

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.Characteristics;
import org.picocontainer.annotations.Bind;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.annotations.Inject;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.lang.annotation.Annotation;

import static org.picocontainer.injectors.AnnotatedMethodInjection.getInjectionAnnotation;
import static org.picocontainer.injectors.AnnotatedMethodInjection.AnnotatedMethodInjector.makeAnnotationNames;


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

    public AnnotatedFieldInjection() {
        this(Inject.class, getInjectionAnnotation("javax.inject.Inject"));
    }

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor,
                                                   LifecycleStrategy lifecycle,
                                                   Properties componentProps,
                                                   Object key,
                                                   Class<T> impl,
                                                   Parameter... parameters) throws PicoCompositionException {
        boolean useNames = AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.USE_NAMES, true);
        return wrapLifeCycle(monitor.newInjector(new AnnotatedFieldInjector(key, impl, parameters, monitor,
                useNames, injectionAnnotations)), lifecycle);
    }

    /**
     * Injection happens after instantiation, and through fields marked as injection points via an Annotation.
     * The default annotation of org.picocontainer.annotations.@Inject can be overridden.
     */
    public static class AnnotatedFieldInjector<T> extends AbstractFieldInjector<T> {

        private final Class<? extends Annotation>[] injectionAnnotations;
        private String injectionAnnotationNames;

        public AnnotatedFieldInjector(Object key, Class<T> impl, Parameter[] parameters, ComponentMonitor monitor,
                                      boolean useNames, Class<? extends Annotation>... injectionAnnotations) {

            super(key, impl, monitor, useNames, parameters);
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
                        typeList.add(box(field.getType()));
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
                if (field.getAnnotation(injectionAnnotation) != null) {
                    return true;
                }
            }
            return false;
        }


        private Field[] getFields(final Class clazz) {
            return AccessController.doPrivileged(new PrivilegedAction<Field[]>() {
                public Field[] run() {
                    return clazz.getDeclaredFields();
                }
            });
        }

        protected Object injectIntoMember(AccessibleObject member, Object componentInstance, Object toInject)
                throws IllegalAccessException, InvocationTargetException {
            Field field = (Field) member;
            field.setAccessible(true);
            field.set(componentInstance, toInject);
            return null;
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
