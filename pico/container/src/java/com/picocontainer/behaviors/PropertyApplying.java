/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.behaviors;


import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.PicoClassNotFoundException;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;

/**
 * A {@link com.picocontainer.ComponentFactory} that creates
 * {@link PropertyApplicator} instances.
 *
 * @author Aslak Helles&oslash;y
 */
@SuppressWarnings("serial")
public final class PropertyApplying extends AbstractBehavior {

    @Override
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor,
            final LifecycleStrategy lifecycle, final Properties componentProps, final Object key,
            final Class<T> impl, final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
        ComponentAdapter<T> decoratedAdapter =
                super.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams);
        removePropertiesIfPresent(componentProps, Characteristics.PROPERTY_APPLYING);
        return monitor.changedBehavior(new PropertyApplicator<T>(decoratedAdapter));
    }

    @Override
	public <T> ComponentAdapter<T> addComponentAdapter(final ComponentMonitor monitor,
            final LifecycleStrategy lifecycle, final Properties componentProps, final ComponentAdapter<T> adapter) {
        removePropertiesIfPresent(componentProps, Characteristics.PROPERTY_APPLYING);
        return monitor.changedBehavior(new PropertyApplicator<T>(
                super.addComponentAdapter(monitor, lifecycle, componentProps, adapter)));
    }

    /**
     * Decorating component adapter that can be used to set additional properties
     * on a component in a bean style. These properties must be managed manually
     * by the user of the API, and will not be managed by PicoContainer. This class
     * is therefore <em>not</em> the same as {@link com.picocontainer.injectors.SetterInjection.SetterInjector},
     * which is a true Setter Injection adapter.
     * <p/>
     * This adapter is mostly handy for setting various primitive properties via setters;
     * it is also able to set javabean properties by discovering an appropriate
     * {@link java.beans.PropertyEditor} and using its <code>setAsText</code> method.
     * <p/>
     * <em>
     * Note that this class doesn't cache instances. If you want caching,
     * use a {@link com.picocontainer.behaviors.Caching.Cached} around this one.
     * </em>
     *
     * @author Aslak Helles&oslash;y
     * @author Mauro Talevi
     */
    @SuppressWarnings("serial")
    public static class PropertyApplicator<T> extends AbstractChangedBehavior<T> {
        private Map<String, String> properties;
        private transient Map<String, Method> setters = null;

        /**
         * Construct a PropertyApplicator.
         *
         * @param delegate the wrapped {@link com.picocontainer.ComponentAdapter}
         * @throws com.picocontainer.PicoCompositionException {@inheritDoc}
         */
        public PropertyApplicator(final ComponentAdapter<T> delegate) throws PicoCompositionException {
            super(delegate);
        }

        /**
         * Get a component instance and set given property values.
         *
         * @return the component instance with any properties of the properties map set.
         * @throws com.picocontainer.PicoCompositionException {@inheritDoc}
         * @throws com.picocontainer.PicoCompositionException  {@inheritDoc}
         * @throws com.picocontainer.PicoCompositionException
         *                                     {@inheritDoc}
         * @see #setProperties(java.util.Map)
         */
        @Override
		public T getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
            final T componentInstance = super.getComponentInstance(container, into);
            if (setters == null) {
                setters = getSetters(getComponentImplementation());
            }

            if (properties != null) {
                ComponentMonitor monitor = currentMonitor();
                Set<String> propertyNames = properties.keySet();
                for (String propertyName : propertyNames) {
                    final Object propertyValue = properties.get(propertyName);
                    Method setter = setters.get(propertyName);

                    Object valueToInvoke = this.getSetterParameter(propertyName, propertyValue, componentInstance, container);

                    try {
                        monitor.invoking(container, PropertyApplicator.this, setter, componentInstance, new Object[] {valueToInvoke});
                        long startTime = System.currentTimeMillis();
                        setter.invoke(componentInstance, valueToInvoke);
                        monitor.invoked(container,
                                                 PropertyApplicator.this,
                                                 setter, componentInstance, System.currentTimeMillis() - startTime, null, new Object[] {valueToInvoke});
                    } catch (final Exception e) {
                        monitor.invocationFailed(setter, componentInstance, e);
                        throw new PicoCompositionException("Failed to set property " + propertyName + " to " + propertyValue + ": " + e.getMessage(), e);
                    }
                }
            }
            return componentInstance;
        }

        public String getDescriptor() {
            return "PropertyApplied";
        }

        private Map<String, Method> getSetters(final Class<?> clazz) {
            Map<String, Method> result = new HashMap<String, Method>();
            Method[] methods = getMethods(clazz);
            for (Method method : methods) {
                if (isSetter(method)) {
                    result.put(getPropertyName(method), method);
                }
            }
            return result;
        }

        private Method[] getMethods(final Class<?> clazz) {
            return (Method[]) AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    return clazz.getMethods();
                }
            });
        }


        private String getPropertyName(final Method method) {
            final String name = method.getName();
            String result = name.substring(3);
            if(result.length() > 1 && !Character.isUpperCase(result.charAt(1))) {
                result = "" + Character.toLowerCase(result.charAt(0)) + result.substring(1);
            } else if(result.length() == 1) {
                result = result.toLowerCase();
            }
            return result;
        }

        private boolean isSetter(final Method method) {
            final String name = method.getName();
            return name.length() > 3 &&
                    name.startsWith("set") &&
                    method.getParameterTypes().length == 1;
        }

        private Object convertType(final PicoContainer container, final Method setter, final String propertyValue) {
            if (propertyValue == null) {
                return null;
            }
            Class<?> type = setter.getParameterTypes()[0];
            String typeName = type.getName();

            Object result = convert(typeName, propertyValue, Thread.currentThread().getContextClassLoader());

            if (result == null) {

                // check if the propertyValue is a key of a component in the container
                // if so, the typeName of the component and the setters parameter typeName
                // have to be compatible

                // TODO: null check only because of test-case, otherwise null is impossible
                if (container != null) {
                    Object component = container.getComponentInto(propertyValue, NOTHING.class);
                    if (component != null && type.isAssignableFrom(component.getClass())) {
                        return component;
                    }
                }
            }
            return result;
        }

        /**
         * Converts a String value of a named type to an object.
         * Works with primitive wrappers, String, File, URL types, or any type that has
         * an appropriate {@link java.beans.PropertyEditor}.
         *
         * @param typeName    name of the type
         * @param value       its value
         * @param classLoader used to load a class if typeName is "class" or "java.lang.Class" (ignored otherwise)
         * @return instantiated object or null if the type was unknown/unsupported
         */
        public static Object convert(final String typeName, final String value, final ClassLoader classLoader) {
            if (typeName.equals(Boolean.class.getName()) || typeName.equals(boolean.class.getName())) {
                return Boolean.valueOf(value);
            } else if (typeName.equals(Byte.class.getName()) || typeName.equals(byte.class.getName())) {
                return Byte.valueOf(value);
            } else if (typeName.equals(Short.class.getName()) || typeName.equals(short.class.getName())) {
                return Short.valueOf(value);
            } else if (typeName.equals(Integer.class.getName()) || typeName.equals(int.class.getName())) {
                return Integer.valueOf(value);
            } else if (typeName.equals(Long.class.getName()) || typeName.equals(long.class.getName())) {
                return Long.valueOf(value);
            } else if (typeName.equals(Float.class.getName()) || typeName.equals(float.class.getName())) {
                return Float.valueOf(value);
            } else if (typeName.equals(Double.class.getName()) || typeName.equals(double.class.getName())) {
                return Double.valueOf(value);
            } else if (typeName.equals(Character.class.getName()) || typeName.equals(char.class.getName())) {
                return value.toCharArray()[0];
            } else if (typeName.equals(String.class.getName()) || typeName.equals("string")) {
                return value;
            } else if (typeName.equals(File.class.getName()) || typeName.equals("file")) {
                return new File(value);
            } else if (typeName.equals(URL.class.getName()) || typeName.equals("url")) {
                try {
                    return new URL(value);
                } catch (MalformedURLException e) {
                    throw new PicoCompositionException(e);
                }
            } else if (typeName.equals(Class.class.getName()) || typeName.equals("class")) {
                return loadClass(classLoader, value);
            } else {
                final Class<?> clazz = loadClass(classLoader, typeName);
                final PropertyEditor editor = PropertyEditorManager.findEditor(clazz);
                if (editor != null) {
                    editor.setAsText(value);
                    return editor.getValue();
                }
            }
            return null;
        }

        private static Class<?> loadClass(final ClassLoader classLoader, final String typeName) {
            try {
                return classLoader.loadClass(typeName);
            } catch (ClassNotFoundException e) {
                throw new PicoClassNotFoundException(typeName, e);
            }
        }


        /**
         * Sets the bean property values that should be set upon creation.
         *
         * @param properties bean properties
         */
        public void setProperties(final Map<String, String> properties) {
            this.properties = properties;
        }

        /**
         * Converts and validates the given property value to an appropriate object
         * for calling the bean's setter.
         * @param propertyName String the property name on the component that
         * we will be setting the value to.
         * @param propertyValue Object the property value that we've been given. It
         * may need conversion to be formed into the value we need for the
         * component instance setter.
         * @param componentInstance the component that we're looking to provide
         * the setter to.
         * @return Object: the final converted object that can
         * be used in the setter.
         * @param container
         */
        private Object getSetterParameter(final String propertyName, final Object propertyValue,
            final Object componentInstance, final PicoContainer container) {

            if (propertyValue == null) {
                return null;
            }

            Method setter = setters.get(propertyName);

            //We can assume that there is only one object (as per typical setters)
            //because the Setter introspector does that job for us earlier.
            Class<?> setterParameter = setter.getParameterTypes()[0];

            Object convertedValue;

            Class<? extends Object> givenParameterClass = propertyValue.getClass();

            //
            //If property value is a string or a true primative then convert it to whatever
            //we need.  (String will convert to string).
            //
            convertedValue = convertType(container, setter, propertyValue.toString());

            //Otherwise, check the parameter type to make sure we can
            //assign it properly.
            if (convertedValue == null) {
                if (setterParameter.isAssignableFrom(givenParameterClass)) {
                    convertedValue = propertyValue;
                } else {
                    throw new ClassCastException("Setter: " + setter.getName() + " for addComponent: "
                        + componentInstance.toString() + " can only take objects of: " + setterParameter.getName()
                        + " instead got: " + givenParameterClass.getName());
                }
            }
            return convertedValue;
        }

        public void setProperty(final String name, final String value) {
            if (properties == null) {
                properties = new HashMap<String, String>();
            }
            properties.put(name, value);
        }

    }
}
