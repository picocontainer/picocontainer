/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.Parameter;

import java.lang.annotation.Annotation;

/**
 * Convenience class providing static methods to conveniently create injectors
 *
 * @author Konstantin Pribluda
 */
public class Injector {
    /**
     * Constructor injector that uses no monitor and no lifecycle adapter.  This is a more
     * convenient constructor for use when instantiating a constructor injector directly.
     *
     * @param key            the search key for this implementation
     * @param impl the concrete implementation
     * @param parameters              the parameters used for initialization
     * @return
     */

    public static <T> ComponentAdapter<T> constructor(final Object key, final Class<T> impl, Parameter... parameters) {
        return new ConstructorInjection.ConstructorInjector<T>(key, impl, parameters);
    }

    /**
     * Creates a ConstructorInjector
     *
     * @param key            the search key for this implementation
     * @param impl the concrete implementation
     * @param parameters              the parameters to use for the initialization
     * @param monitor                 the component monitor used by this addAdapter
     * @param useNames                use argument names when looking up dependencies
     * @throws org.picocontainer.injectors.AbstractInjector.NotConcreteRegistrationException
     *                              if the implementation is not a concrete class.
     * @throws NullPointerException if one of the parameters is <code>null</code>
     * @return
     */
    public static <T> ComponentAdapter<T> constructor(final Object key, final Class<T> impl, Parameter[] parameters, ComponentMonitor monitor,
                                               boolean useNames) throws AbstractInjector.NotConcreteRegistrationException {
        return new ConstructorInjection.ConstructorInjector<T>(key, impl, parameters, monitor, useNames);
    }

    /**
     * Creates a ConstructorInjector
     *
     * @param key            the search key for this implementation
     * @param impl the concrete implementation
     * @param parameters              the parameters to use for the initialization
     * @param monitor                 the component monitor used by this addAdapter
     * @param useNames                use argument names when looking up dependencies
     * @param rememberChosenCtor      remember the chosen constructor (to speed up second/subsequent calls)
     * @throws org.picocontainer.injectors.AbstractInjector.NotConcreteRegistrationException
     *                              if the implementation is not a concrete class.
     * @throws NullPointerException if one of the parameters is <code>null</code>
     * @return
     */
    public static <T> ComponentAdapter<T> constructor(final Object key, final Class<T> impl, Parameter[] parameters, ComponentMonitor monitor,
                                              boolean useNames, boolean rememberChosenCtor) throws AbstractInjector.NotConcreteRegistrationException {
        return new ConstructorInjection.ConstructorInjector<T>(key, impl, parameters, monitor,
                useNames, rememberChosenCtor);
    }

    /**
     * Convenience method to create annotated field injector
     *
     * @param key
     * @param impl
     * @param parameters
     * @param monitor
     * @param injectionAnnotation
     * @param useNames
     * @return annotated field injector instance.
     */
    public static <T> ComponentAdapter<T> annotatedField(Object key, Class<T> impl, Parameter[] parameters, ComponentMonitor monitor,
                                                  Class<? extends Annotation> injectionAnnotation, boolean useNames) {
        return monitor.newInjector(new AnnotatedFieldInjection.AnnotatedFieldInjector<T>(key, impl, parameters, monitor, useNames, injectionAnnotation));
    }

    /**
     * convenience method to create annotated method injector
     *
     * @param key
     * @param impl
     * @param parameters
     * @param monitor
     * @param injectionAnnotation
     * @param useNames
     * @return method injector instance.
     */
    public static <T> ComponentAdapter<T> annotatedMethod(Object key, Class<T> impl, Parameter[] parameters, ComponentMonitor monitor,
                                                   Class<? extends Annotation> injectionAnnotation, boolean useNames) {
        return monitor.newInjector(new AnnotatedMethodInjection.AnnotatedMethodInjector<T>(key, impl, parameters, monitor, useNames, injectionAnnotation));

    }


    /**
     * creates composite injector
     *
     * @param key
     * @param impl
     * @param parameters
     * @param monitor
     * @param useNames
     * @param injectors
     * @return composite injector instance.
     */
    public static <T> ComponentAdapter<T> composite(Object key, Class<T> impl, Parameter[] parameters, ComponentMonitor monitor,
                                             boolean useNames, org.picocontainer.Injector... injectors) {
        return monitor.newInjector(new CompositeInjection.CompositeInjector<T>(key, impl, parameters, monitor, useNames, injectors));
    }


    /**
     * convenience method to create method injector
     *
     * @param key
     * @param impl
     * @param parameters
     * @param monitor
     * @param methodName
     * @param useNames
     * @return method injector instance.
     * @throws AbstractInjector.NotConcreteRegistrationException
     *
     */
    public static <T> ComponentAdapter<T> method(final Object key, final Class<T> impl, Parameter[] parameters, ComponentMonitor monitor,
                                          String methodName, boolean useNames) throws AbstractInjector.NotConcreteRegistrationException {
        return monitor.newInjector(new MethodInjection.MethodInjector<T>(key, impl, parameters, monitor, methodName, useNames));
    }

    /**
     * convenience method to create multi component adapter
     *
     * @param key
     * @param impl
     * @param parameters
     * @param monitor
     * @param setterPrefix
     * @param useNames
     * @return MultiInjector component adapter instance.
     */

    public static <T> ComponentAdapter<T> multi(Object key, Class<T> impl, Parameter[] parameters,
                                         ComponentMonitor monitor, String setterPrefix, boolean useNames) {
        return monitor.newInjector(new MultiInjection.MultiInjector<T>(key, impl, parameters, monitor, setterPrefix, useNames));
    }

    /**
     * convenience method to create named field injector
     *
     * @param key
     * @param impl
     * @param parameters
     * @param monitor
     * @param fieldNames
     * @return named field component injector instance.
     */
    public static <T> ComponentAdapter<T> namedField(Object key, Class<T> impl, Parameter[] parameters, ComponentMonitor monitor,
                                              String fieldNames) {
        return monitor.newInjector(new NamedFieldInjection.NamedFieldInjector<T>(key, impl, parameters, monitor, fieldNames));
    }

    /**
     * convenience method to create setter injector
     *
     * @param key
     * @param impl
     * @param parameters
     * @param monitor
     * @param prefix
     * @param useNames
     * @return setter injector instance.
     * @throws AbstractInjector.NotConcreteRegistrationException
     *
     */
    public static <T> ComponentAdapter<T> setter(final Object key, final Class<T> impl, Parameter[] parameters, ComponentMonitor monitor,
                                          String prefix, boolean useNames) throws AbstractInjector.NotConcreteRegistrationException {
        return monitor.newInjector(new SetterInjection.SetterInjector<T>(key, impl, parameters, monitor, prefix, useNames));
    }

    /**
     * conveniently create typed field injector
     *
     * @param key
     * @param impl
     * @param parameters
     * @param monitor
     * @param classNames
     * @return typed field injector instance.
     */
    public static <T> ComponentAdapter<T> typedField(Object key, Class<T> impl, Parameter[] parameters, ComponentMonitor monitor,
                                              String classNames) {
        return monitor.newInjector(new TypedFieldInjection.TypedFieldInjector<T>(key, impl, parameters, monitor, classNames));
    }
}
