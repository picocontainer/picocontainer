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
 * convenience class providing static methods to conveniently create injectors
 * ( like org.junit.Assert )
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
     */

    public static ComponentAdapter constructor(final Object key, final Class<?> impl, Parameter... parameters) {
        return new ConstructorInjector(key, impl, parameters);
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
     */
    public static ComponentAdapter constructor(final Object key, final Class impl, Parameter[] parameters, ComponentMonitor monitor,
                                               boolean useNames) throws AbstractInjector.NotConcreteRegistrationException {
        return new ConstructorInjector(key, impl, parameters, monitor, useNames);
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
     */
    public static ComponentAdapter constructor(final Object key, final Class impl, Parameter[] parameters, ComponentMonitor monitor,
                                              boolean useNames, boolean rememberChosenCtor) throws AbstractInjector.NotConcreteRegistrationException {
        return new ConstructorInjector(key, impl, parameters, monitor,
                useNames, rememberChosenCtor);
    }

    /**
     * Convenience method to create annotated field injector
     *
     * @param key
     * @param impl
     * @param parameters
     * @param componentMonitor
     * @param injectionAnnotation
     * @param useNames
     * @return annotated field injector instance.
     */
    public static ComponentAdapter annotatedField(Object key,
                                                  Class<?> impl,
                                                  Parameter[] parameters,
                                                  ComponentMonitor componentMonitor,
                                                  Class<? extends Annotation> injectionAnnotation, boolean useNames) {
        return componentMonitor.newInjector(new AnnotatedFieldInjector(key, impl, parameters, componentMonitor, useNames, injectionAnnotation));
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
    public static ComponentAdapter annotatedMethod(Object key,
                                                   Class<?> impl,
                                                   Parameter[] parameters,
                                                   ComponentMonitor monitor,
                                                   Class<? extends Annotation> injectionAnnotation, boolean useNames) {
        return monitor.newInjector(new AnnotatedMethodInjector(key, impl, parameters, monitor, useNames, injectionAnnotation));

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
    public static ComponentAdapter composite(Object key, Class<?> impl, Parameter[] parameters, ComponentMonitor monitor,
                                             boolean useNames, org.picocontainer.Injector... injectors) {
        return monitor.newInjector(new CompositeInjector(key, impl, parameters, monitor, useNames, injectors));
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
    public static ComponentAdapter method(final Object key, final Class impl, Parameter[] parameters, ComponentMonitor monitor,
                                          String methodName, boolean useNames) throws AbstractInjector.NotConcreteRegistrationException {
        return monitor.newInjector(new MethodInjector(key, impl, parameters, monitor, methodName, useNames));
    }

    /**
     * convenience method to create multi component adapter
     *
     * @param key
     * @param impl
     * @param parameters
     * @param componentMonitor
     * @param setterPrefix
     * @param useNames
     * @return MultiInjector component adapter instance.
     */

    public static ComponentAdapter multi(Object key,
                                         Class impl,
                                         Parameter[] parameters,
                                         ComponentMonitor componentMonitor, String setterPrefix, boolean useNames) {
        return componentMonitor.newInjector(new MultiInjector(key, impl, parameters, componentMonitor, setterPrefix, useNames));
    }

    /**
     * convenience method to create named field injector
     *
     * @param key
     * @param impl
     * @param parameters
     * @param componentMonitor
     * @param fieldNames
     * @return named field component injector instance.
     */
    public static ComponentAdapter namedField(Object key,
                                              Class<?> impl,
                                              Parameter[] parameters,
                                              ComponentMonitor componentMonitor,
                                              String fieldNames) {
        return componentMonitor.newInjector(new NamedFieldInjector(key, impl, parameters, componentMonitor, fieldNames));
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
    public static ComponentAdapter setter(final Object key,
                                          final Class impl,
                                          Parameter[] parameters,
                                          ComponentMonitor monitor,
                                          String prefix, boolean useNames) throws AbstractInjector.NotConcreteRegistrationException {
        return monitor.newInjector(new SetterInjector(key, impl, parameters, monitor, prefix, useNames));
    }

    /**
     * conveniently create typed field injector
     *
     * @param key
     * @param impl
     * @param parameters
     * @param componentMonitor
     * @param classNames
     * @return typed field injector instance.
     */
    public static ComponentAdapter typedField(Object key,
                                              Class<?> impl,
                                              Parameter[] parameters,
                                              ComponentMonitor componentMonitor,
                                              String classNames) {
        return componentMonitor.newInjector(new TypedFieldInjector(key, impl, parameters, componentMonitor, classNames));
    }
}
