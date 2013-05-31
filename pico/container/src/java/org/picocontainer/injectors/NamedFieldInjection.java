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
import org.picocontainer.annotations.Bind;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.picocontainer.Characteristics.immutable;

/**
 * A {@link org.picocontainer.InjectionType} for named fields.
 *
 * Use like so: pico.as(injectionFieldNames("field1", "field2")).addComponent(...)
 *
 * The factory creates {@link org.picocontainer.injectors.NamedFieldInjection.NamedFieldInjector}.
 *
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class NamedFieldInjection extends AbstractInjectionType {


    private static final String INJECTION_FIELD_NAMES = "injectionFieldNames";
	private final boolean requireConsumptionOfallParameters;
    
    public NamedFieldInjection() {
    	requireConsumptionOfallParameters = true;
    }
    
    public NamedFieldInjection(boolean requireConsumptionOfallParameters) {
		this.requireConsumptionOfallParameters = requireConsumptionOfallParameters;
    	
    }

    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor,
                                                   LifecycleStrategy lifecycle,
                                                   Properties componentProps,
                                                   Object key,
                                                   Class<T> impl,
                                                   ConstructorParameters constructorParams, FieldParameters[] fieldParams, MethodParameters[] methodParams) throws PicoCompositionException {
        String fieldNames = (String) componentProps.remove(INJECTION_FIELD_NAMES);
        if (fieldNames == null) {
            fieldNames = "";
        }
        return wrapLifeCycle(monitor.newInjector(new NamedFieldInjector(key, impl, monitor, fieldNames, requireConsumptionOfallParameters, fieldParams
        )), lifecycle);
    }

    public static Properties injectionFieldNames(String... fieldNames) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldNames.length; i++) {
            sb.append(" ").append(fieldNames[i]);
        }
        Properties retVal = new Properties();
        return immutable(INJECTION_FIELD_NAMES, sb.toString().trim());
    }

    /**
     * Injection happens after instantiation, and fields are marked as
     * injection points via a named field.
     */
    public static class NamedFieldInjector<T> extends AbstractFieldInjector<T> {

        private final List<String> fieldNames;

        public NamedFieldInjector(Object key,
                                  Class<T> impl,
                                  ComponentMonitor monitor, 
                                  String fieldNames,
                                  boolean requireConsumptionOfAllParameters,
                                  FieldParameters... parameters) {
            super(key, impl, monitor, true, requireConsumptionOfAllParameters, parameters);
            this.fieldNames = Arrays.asList(fieldNames.trim().split(" "));
        }

        @Override
        protected void initializeInjectionMembersAndTypeLists() {
            injectionMembers = new ArrayList<AccessibleObject>();
            List<Annotation> bindingIds = new ArrayList<Annotation>();
            final List<Type> typeList = new ArrayList<Type>();
            final Field[] fields = getFields();
            for (final Field field : fields) {
                if (isNamedForInjection(field)) {
                    injectionMembers.add(field);
                    typeList.add(box(field.getType()));
                    bindingIds.add(getBinding(field));
                }
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

        protected boolean isNamedForInjection(Field field) {
            return fieldNames.contains(field.getName());
        }

        private Field[] getFields() {
            return AccessController.doPrivileged(new PrivilegedAction<Field[]>() {
                public Field[] run() {
                    return getComponentImplementation().getDeclaredFields();
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
            return "NamedFieldInjector-";
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

        List<String> getInjectionFieldNames() {
            return Collections.unmodifiableList(fieldNames);
        }
    }
}