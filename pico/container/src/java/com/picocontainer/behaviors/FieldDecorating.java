/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaibe                                            *
 *****************************************************************************/

package com.picocontainer.behaviors;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Properties;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.Decorator;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;


/**
 * Behavior for Field Decorating. This factory will create {@link com.picocontainer.behaviors.FieldDecorating.FieldDecorated} that will
 * allow you to decorate fields on the component instance that has been created
 *
 * @author Paul Hammant
 */
public abstract class FieldDecorating extends AbstractBehavior implements Decorator {
    private final Class<?> fieldClass;

    public FieldDecorating(final Class<?> fieldClass) {
        this.fieldClass = fieldClass;
    }

    @Override
	public <T> ComponentAdapter<T> createComponentAdapter(
            final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final Properties componentProps, final Object key, final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams)
            throws PicoCompositionException {
        return monitor.changedBehavior(new FieldDecorated<T>(
                super.createComponentAdapter(monitor, lifecycle, componentProps,
                        key, impl, constructorParams, fieldParams, methodParams),
                fieldClass, this));
    }

    @SuppressWarnings("serial")
    public static class FieldDecorated<T> extends AbstractChangedBehavior<T> {
        private final Class<?> fieldClass;
        private final Decorator decorator;

        public FieldDecorated(final ComponentAdapter<T> delegate, final Class<?> fieldClass, final Decorator decorator) {
            super(delegate);
            this.fieldClass = fieldClass;
            this.decorator = decorator;
        }

        @Override
		public T getComponentInstance(final PicoContainer container, final Type into)
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