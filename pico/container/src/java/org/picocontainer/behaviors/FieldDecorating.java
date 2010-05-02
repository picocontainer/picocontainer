/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaibe                                            *
 *****************************************************************************/

package org.picocontainer.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.Decorator;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Properties;


/**
 * Behavior for Field Decorating. This factory will create {@link org.picocontainer.behaviors.FieldDecorating.FieldDecorated} that will
 * allow you to decorate fields on the component instance that has been created
 *
 * @author Paul Hammant
 */
public abstract class FieldDecorating extends AbstractBehavior implements Decorator {
    private final Class<?> fieldClass;

    public FieldDecorating(Class<?> fieldClass) {
        this.fieldClass = fieldClass;
    }

    public <T> ComponentAdapter<T> createComponentAdapter(
            ComponentMonitor monitor, LifecycleStrategy lifecycle, Properties componentProps, final Object key, final Class<T> impl, final Parameter... parameters)
            throws PicoCompositionException {
        return monitor.newBehavior(new FieldDecorated<T>(
                super.createComponentAdapter(monitor, lifecycle, componentProps,
                        key, impl, parameters),
                fieldClass, this));
    }

    @SuppressWarnings("serial")
    public static class FieldDecorated<T> extends AbstractChangedBehavior<T> {
        private final Class<?> fieldClass;
        private final Decorator decorator;

        public FieldDecorated(ComponentAdapter<T> delegate, Class<?> fieldClass, Decorator decorator) {
            super(delegate);
            this.fieldClass = fieldClass;
            this.decorator = decorator;
        }

        public T getComponentInstance(final PicoContainer container, Type into)
                throws PicoCompositionException {
            T instance = super.getComponentInstance(container, into);
            Field[] fields = instance.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getType() == fieldClass) {
                    Object value = decorator.decorate(instance);
                    field.setAccessible(true);
                    try {
                        field.set(instance, value);
                    } catch (IllegalAccessException e) {
                        throw new PicoCompositionException(e);
                    }
                }
            }
            return instance;
        }


        public String getDescriptor() {
            return "FieldDecorated";
        }
    }
}