/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.injectors;

import java.lang.annotation.Annotation;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.Parameter;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;

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

    public static <T> ComponentAdapter<T> constructor(final Object key, final Class<T> impl, final Parameter... parameters) {
        return new ConstructorInjection.ConstructorInjector<T>(key, impl, new ConstructorParameters(parameters));
    }

    /**
     * Creates a ConstructorInjector
     *
     * @param key            the search key for this implementation
     * @param impl the concrete implementation
     * @param monitor                 the component monitor used by this addAdapter
     * @param useNames                use argument names when looking up dependencies
     * @param parameters              the parameters to use for the initialization
     * @throws com.picocontainer.injectors.AbstractInjector.NotConcreteRegistrationException
     *                              if the implementation is not a concrete class.
     * @throws NullPointerException if one of the parameters is <code>null</code>
     * @return
     */
    public static <T> ComponentAdapter<T> constructor(final Object key, final Class<T> impl, final ComponentMonitor monitor, final boolean useNames,
                                                      final Parameter... parameters) {
        return new ConstructorInjection.ConstructorInjector<T>(monitor, useNames, key, impl, new ConstructorParameters(parameters));
    }

    /**
     * Creates a ConstructorInjector
     *
     * @param key            the search key for this implementation
     * @param impl the concrete implementation
     * @param monitor                 the component monitor used by this addAdapter
     * @param useNames                use argument names when looking up dependencies
     * @param rememberChosenCtor      remember the chosen constructor (to speed up second/subsequent calls)
     * @param parameters              the parameters to use for the initialization
     * @throws com.picocontainer.injectors.AbstractInjector.NotConcreteRegistrationException
     *                              if the implementation is not a concrete class.
     * @throws NullPointerException if one of the parameters is <code>null</code>
     * @return
     */
    public static <T> ComponentAdapter<T> constructor(final Object key, final Class<T> impl, final ComponentMonitor monitor, final boolean useNames, final boolean rememberChosenCtor,
                                                      final Parameter... parameters) {
        return new ConstructorInjection.ConstructorInjector<T>(monitor, useNames, rememberChosenCtor, key, impl, new ConstructorParameters(parameters) );
    }

    /**
     * Convenience method to create annotated field injector
     *
     * @param key
     * @param impl
     * @param parameters
     * @param monitor
     * @param useNames
     * @param injectionAnnotations
     * @return annotated field injector instance.
     */
    public static <T> ComponentAdapter<T> annotatedField(final Object key, final Class<T> impl, final FieldParameters[] parameters, final ComponentMonitor monitor,
                                                         final boolean useNames, final Class<? extends Annotation>... injectionAnnotations) {
        return monitor.newInjector(new AnnotatedFieldInjection.AnnotatedFieldInjector<T>(key, impl, parameters, monitor, useNames, true, injectionAnnotations));
    }

    /**
     * convenience method to create annotated method injector
     *
     * @param key
     * @param impl
     * @param parameters
     * @param monitor
     * @param useNames
     * @param injectionAnnotations
     * @return method injector instance.
     */
    public static <T> ComponentAdapter<T> annotatedMethod(final Object key, final Class<T> impl, final MethodParameters[] parameters, final ComponentMonitor monitor,
                                                          final boolean useNames, final boolean useAllParameters,  final Class<? extends Annotation>... injectionAnnotations) {
        return monitor.newInjector(new AnnotatedMethodInjection.AnnotatedMethodInjector<T>(key, impl, parameters, monitor, useNames, useAllParameters, injectionAnnotations));

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
    public static <T> ComponentAdapter<T> composite(final Object key, final Class<T> impl, final ComponentMonitor monitor,
                                             final boolean useNames, final com.picocontainer.Injector... injectors) {
        return monitor.newInjector(new CompositeInjection.CompositeInjector<T>(key, impl, monitor, useNames, injectors));
    }


    /**
     * convenience method to create method injector
     *
     * @param key
     * @param impl
     * @param monitor
     * @param methodName
     * @param useNames
     * @param parameters
     * @return method injector instance.
     * @throws AbstractInjector.NotConcreteRegistrationException
     *
     */
    public static <T> ComponentAdapter<T> method(final Object key, final Class<T> impl, final ComponentMonitor monitor, final String methodName, final boolean useNames,
    			final boolean requireConsumptionOfAllParameters, final MethodParameters... parameters) {
        return monitor.newInjector(new MethodInjection.MethodInjector<T>(key, impl, monitor, methodName, useNames, requireConsumptionOfAllParameters, parameters));
    }

    /**
     * convenience method to create multi component adapter
     *
     * @param key
     * @param impl
     * @param monitor
     * @param setterPrefix
     * @param useNames
     * @param parameters
     * @return MultiInjector component adapter instance.
     */

    public static <T> ComponentAdapter<T> multi(final Object key, final Class<T> impl, final ComponentMonitor monitor, final String setterPrefix, final boolean useNames, final boolean requireConsumptionOfallParameters,
                                                final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) {
        return monitor.newInjector(new MultiInjection.MultiInjector<T>(key, impl, monitor, setterPrefix, useNames, requireConsumptionOfallParameters, constructorParams, fieldParams, methodParams));
    }

    /**
     * convenience method to create named field injector
     *
     * @param key
     * @param impl
     * @param monitor
     * @param fieldNames
     * @param parameters
     * @return named field component injector instance.
     */
    public static <T> ComponentAdapter<T> namedField(final Object key, final Class<T> impl, final ComponentMonitor monitor, final String fieldNames,
                                                     final FieldParameters... parameters) {
        return monitor.newInjector(new NamedFieldInjection.NamedFieldInjector<T>(key, impl, monitor, fieldNames, true, parameters));
    }

    /**
     * convenience method to create setter injector
     *
     * @param key
     * @param impl
     * @param monitor
     * @param prefix
     * @param useNames
     * @param parameters
     * @return setter injector instance.
     * @throws AbstractInjector.NotConcreteRegistrationException
     *
     */
    public static <T> ComponentAdapter<T> setter(final Object key, final Class<T> impl, final ComponentMonitor monitor, final String prefix, final boolean useNames, final boolean requireConsumptionOfAllParameters,
                                                 final MethodParameters... parameters) {
        return monitor.newInjector(new SetterInjection.SetterInjector<T>(key, impl, monitor, prefix, useNames, "", requireConsumptionOfAllParameters, parameters));
    }

    /**
     * conveniently create typed field injector
     *
     * @param key
     * @param impl
     * @param monitor
     * @param classNames
     * @param parameters
     * @return typed field injector instance.
     */
    public static <T> ComponentAdapter<T> typedField(final Object key, final Class<T> impl, final ComponentMonitor monitor, final String classNames,
                                                     final FieldParameters... parameters) {
        return monitor.newInjector(new TypedFieldInjection.TypedFieldInjector<T>(key, impl, monitor, classNames, true, parameters));
    }
}
