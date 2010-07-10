/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package org.picocontainer.injectors;

import org.picocontainer.*;
import org.picocontainer.Injector;
import org.picocontainer.behaviors.AbstractBehavior;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;


/**
 * A {@link org.picocontainer.InjectionType} for JavaBeans.
 * The factory creates {@link SetterInjector}.
 *
 * @author J&ouml;rg Schaible
 */
@SuppressWarnings("serial")
public class SetterInjection extends AbstractInjectionType {

    private final String prefix;

    public SetterInjection(String prefix) {
        this.prefix = prefix;
    }

    public SetterInjection() {
        this("set");
    }

    /**
     * Create a {@link SetterInjector}.
     * 
     * @param monitor
     * @param lifecycle
     * @param componentProps
     * @param key The component's key
     * @param impl The class of the bean.
     * @param parameters Any parameters for the setters. If null the adapter
     *            solves the dependencies for all setters internally. Otherwise
     *            the number parameters must match the number of the setter.
     * @return Returns a new {@link SetterInjector}.
     * @throws PicoCompositionException if dependencies cannot be solved
     */
    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycle, Properties componentProps, Object key, Class<T> impl, Parameter... parameters)
            throws PicoCompositionException {
        boolean useNames = AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.USE_NAMES, true);
        SetterInjector<T> setterInjector = new SetterInjector<T>(key, impl, monitor, prefix, useNames, parameters);
        Injector<T> injector = monitor.newInjector(setterInjector);
        return wrapLifeCycle(injector, lifecycle);
    }

    /**
     * Instantiates components using empty constructors and
     * <a href="http://picocontainer.org/setter-injection.html">Setter Injection</a>.
     * For easy setting of primitive properties, also see {@link org.picocontainer.behaviors.PropertyApplying.PropertyApplicator}.
     * <p/>
     * <em>
     * Note that this class doesn't cache instances. If you want caching,
     * use a {@link org.picocontainer.behaviors.Caching.Cached} around this one.
     * </em>
     * </p>
     *
     * @author Aslak Helles&oslash;y
     * @author J&ouml;rg Schaible
     * @author Mauro Talevi
     * @author Paul Hammant
     */
    @SuppressWarnings("serial")
    public static class SetterInjector<T> extends IterativeInjector<T> {

        protected final String prefix;

        /**
         * Constructs a SetterInjector
         *
         * @param key            the search key for this implementation
         * @param impl the concrete implementation
         * @param monitor                 the component monitor used by this addAdapter
         * @param prefix                  the prefix to use (e.g. 'set')
         * @param useNames
         * @param parameters              the parameters to use for the initialization
         * @throws org.picocontainer.injectors.AbstractInjector.NotConcreteRegistrationException
         *                              if the implementation is not a concrete class.
         * @throws NullPointerException if one of the parameters is <code>null</code>
         */
        public SetterInjector(final Object key,
                              final Class impl,
                              ComponentMonitor monitor, String prefix, boolean useNames,
                              Parameter... parameters) throws  NotConcreteRegistrationException {
            super(key, impl, monitor, useNames, parameters);
            this.prefix = prefix;
        }

        protected Object memberInvocationReturn(Object lastReturn, AccessibleObject member, Object instance) {
            return member != null && ((Method)member).getReturnType()!=void.class ? lastReturn : instance;
        }

        @Override
        protected Object injectIntoMember(AccessibleObject member, Object componentInstance, Object toInject)
            throws IllegalAccessException, InvocationTargetException {
            return ((Method)member).invoke(componentInstance, toInject);
        }

        @Override
        protected boolean isInjectorMethod(Method method) {
            String methodName = method.getName();
            return methodName.length() >= getInjectorPrefix().length() + 1 && methodName.startsWith(getInjectorPrefix()) && Character.isUpperCase(methodName.charAt(getInjectorPrefix().length()));
        }

        protected String getInjectorPrefix() {
            return prefix;
        }

        @Override
        public String getDescriptor() {
            return "SetterInjector-";
        }


    }
}
