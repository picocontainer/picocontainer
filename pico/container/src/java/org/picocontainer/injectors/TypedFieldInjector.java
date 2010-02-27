/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.NameBinding;
import org.picocontainer.Parameter;
import org.picocontainer.annotations.Bind;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Injection happens after instantiation, and fields are marked as
 * injection points via a field type.
 */
@SuppressWarnings("serial")
public class TypedFieldInjector extends IterativeInjector {

    private final List<String> classes;

    public TypedFieldInjector(Object key,
                                  Class<?> impl,
                                  Parameter[] parameters,
                                  ComponentMonitor componentMonitor,
                                  String classNames) {
        super(key, impl, parameters, componentMonitor, true);
        this.classes = Arrays.asList(classNames.trim().split(" "));
    }

    @Override
    protected void initializeInjectionMembersAndTypeLists() {
        injectionMembers = new ArrayList<AccessibleObject>();
        List<Annotation> bindingIds = new ArrayList<Annotation>();
        final List<Type> typeList = new ArrayList<Type>();
        final Field[] fields = getFields();
        for (final Field field : fields) {
            if (isTypedForInjection(field)) {
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

    protected boolean isTypedForInjection(Field field) {
        return classes.contains(field.getType().getName());
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
        return "TypedFieldInjector-";
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

    List<String> getInjectionFieldTypes() {
        return Collections.unmodifiableList(classes);
    }


}