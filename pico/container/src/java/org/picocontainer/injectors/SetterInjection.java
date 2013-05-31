/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
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
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Properties;
import java.util.Set;


/**
 * A {@link org.picocontainer.InjectionType} for JavaBeans.
 * The factory creates {@link SetterInjector}.
 *
 * @author J&ouml;rg Schaible
 */
@SuppressWarnings("serial")
public class SetterInjection extends AbstractInjectionType {

    private final String prefix;
    private boolean optional;
    private String notThisOneThough;

    public SetterInjection(String prefix) {
        this.prefix = prefix;
    }

    public SetterInjection() {
        this("set");
    }

    /**
     * Specify a prefix and an exclusion
     * @param prefix the prefix like 'set'
     * @param notThisOneThough to exclude, like 'setMetaClass' for Groovy
     */
    public SetterInjection(String prefix, String notThisOneThough) {
        this(prefix);
        this.notThisOneThough = notThisOneThough;
    }

    /**
     * Create a {@link SetterInjector}.
     * 
     * @param monitor
     * @param lifecycle
     * @param componentProps
     * @param key The component's key
     * @param impl The class of the bean.
     * @return Returns a new {@link SetterInjector}.
     * @throws PicoCompositionException if dependencies cannot be solved
     */
    public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycle, Properties componentProps,
                                           Object key, Class<T> impl, ConstructorParameters constructorParams, FieldParameters[] fieldParams, MethodParameters[] methodParams) throws PicoCompositionException {
        boolean useNames = AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.USE_NAMES, true);
        SetterInjector<T> setterInjector = new SetterInjector<T>(key, impl, monitor, prefix, useNames,
                notThisOneThough != null ? notThisOneThough : "", optional, methodParams);
        Injector<T> injector = monitor.newInjector(setterInjector);
        return wrapLifeCycle(injector, lifecycle);
    }

    public SetterInjection withInjectionOptional() {
        optional = true;
        return this;
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
    public static class SetterInjector<T> extends IterativeInjector<T> {

        protected final String prefix;
        private final boolean optional;
        private final String notThisOneThough;

        /**
         * Constructs a SetterInjector
         *
         *
         * @param key            the search key for this implementation
         * @param impl the concrete implementation
         * @param monitor                 the component monitor used by this addAdapter
         * @param prefix                  the prefix to use (e.g. 'set')
         * @param useNames                use parameter names
         * @param notThisOneThough
         * @param optional                not all setters need to be injected
         * @param parameters              the parameters to use for the initialization
         * @throws org.picocontainer.injectors.AbstractInjector.NotConcreteRegistrationException
         *                              if the implementation is not a concrete class.
         * @throws NullPointerException if one of the parameters is <code>null</code>
         */
        public SetterInjector(final Object key,
                              final Class impl,
                              ComponentMonitor monitor, String prefix, boolean useNames, String notThisOneThough,
                              boolean optional, MethodParameters... parameters) throws  NotConcreteRegistrationException {
            super(key, impl, monitor, useNames, parameters);
            this.prefix = prefix;
            this.notThisOneThough = notThisOneThough != null ? notThisOneThough : "";
            this.optional = optional;
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
            return methodName.length() >= getInjectorPrefix().length() + 1 // long enough
                    && methodName.startsWith(getInjectorPrefix())
                    && !methodName.equals(notThisOneThough)
                    && Character.isUpperCase(methodName.charAt(getInjectorPrefix().length()));
        }

        @Override
        protected void unsatisfiedDependencies(PicoContainer container, Set<Type> unsatisfiableDependencyTypes, List<AccessibleObject> unsatisfiableDependencyMembers) {
            if (!optional) {
                throw new UnsatisfiableDependenciesException(this.getComponentImplementation().getName() + " has unsatisfied dependencies " + unsatisfiableDependencyTypes
                        + " for members " + unsatisfiableDependencyMembers + " from " + container);
            }
        }

        protected String getInjectorPrefix() {
            return prefix;
        }

        @Override
        public String getDescriptor() {
            return "SetterInjector-";
        }

		@Override
		protected boolean isAccessibleObjectEqualToParameterTarget(AccessibleObject testObject,
				Parameter currentParameter) {
			if (currentParameter.getTargetName() == null) {
				return false;
			}
			
			if (!(testObject instanceof Method)) {
				throw new PicoCompositionException(testObject + " must be a method to use setter injection");
			}
			
			String targetProperty = currentParameter.getTargetName();
			
			//Convert to setter name.
			String testProperty = "set" + Character.toUpperCase(targetProperty.charAt(0));
			if (targetProperty.length() > 0) {
				testProperty = testProperty + targetProperty.substring(1);
			}
			
			Method targetMethod = (Method)testObject;			
			return targetMethod.getName().equals(testProperty);
		}


    }
}
