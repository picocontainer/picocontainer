/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.injectors;

import static com.picocontainer.injectors.PrimitiveMemberChecker.isPrimitiveArgument;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.Parameter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.Parameter.Resolver;
import com.picocontainer.annotations.Bind;
import com.picocontainer.parameters.AccessibleObjectParameterSet;
import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

/**
 * Injection will happen in a member with multiple arguments on the component.
 * Member can be either Constructor or Method of course. See subclasses.
 *
 * @author Paul Hammant
 *
 */
@SuppressWarnings("serial")
public abstract class MultiArgMemberInjector<T> extends AbstractInjector<T> {

    private transient Paranamer paranamer;

	private final boolean useAllParameters;

    public MultiArgMemberInjector(final Object key,
                                final Class<T> impl,
                                final AccessibleObjectParameterSet[] parameters,
                                final ComponentMonitor monitor,
                                final boolean useNames,
                                final boolean useAllParameters
                                ) {
        super(key, impl, monitor, useNames, parameters);
		this.useAllParameters = useAllParameters;
    }

    protected Paranamer getParanamer() {
        if (paranamer == null) {
            paranamer = new CachingParanamer(new AnnotationParanamer(new AdaptiveParanamer()));
        }
        return paranamer;
    }

    protected Object[] getMemberArguments(final PicoContainer container, final AccessibleObject member, final Type[] parameterTypes, final Annotation[] bindings, final Type into) {
        boxParameters(parameterTypes);
        //Object[] result = new Object[parameterTypes.length];
        List<Object> result = new ArrayList<Object>(parameterTypes.length);
        AccessibleObjectParameterSet objectParameterSet = this.getParameterToUseForObject(member, parameters);

        Parameter[] currentParameters = objectParameterSet.getParams();

        for (int i = 0; i < currentParameters.length; i++) {
            try {
				Object parameterResult = getParameter(container, member, i, parameterTypes[i], bindings[i], currentParameters[i], null, into);
				if (parameterResult != Parameter.NULL_RESULT) {
					result.add(parameterResult);
				}
			} catch (AmbiguousComponentResolutionException e) {
				e.setComponent(getComponentImplementation());
				e.setMember(member);
				e.setParameterNumber(i);
				throw e;
			}
        }

        return result.toArray();
    }


	/**
	 * Allow injector-based substitution of the parameters defined in the composition script
	 */
	@Override
	protected Parameter[] interceptParametersToUse(final Parameter[] currentParameters, final AccessibleObject member) {
		return currentParameters;
	}

	protected void boxParameters(final Type[] parameterTypes) {
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = box(parameterTypes[i]);
        }
    }

    protected Object getParameter(final PicoContainer container, final AccessibleObject member, final int i, final Type parameterType, final Annotation binding,
                                  final Parameter currentParameter, final ComponentAdapter<?> injecteeAdapter, final Type into) {



        ParameterNameBinding expectedNameBinding = new ParameterNameBinding(getParanamer(), member, i);
        Resolver resolver = currentParameter.resolve(container, this, injecteeAdapter, parameterType, expectedNameBinding, useNames(), binding);

        if (!resolver.isResolved()) {
        	if (!this.useAllParameters) {
        		return Parameter.NULL_RESULT;
        	}
        }

        Object result = resolver.resolveInstance(into);
        nullCheck(member, i, expectedNameBinding, result);

        return result;
    }

    /**
     * Throws an exception if the &quot;resolved&quot; parameter is null <em>unless</em>
     * {@link #useAllParameters} is set to false.
     *
     * @param member
     * @param i
     * @param expectedNameBinding
     * @param result
     */
    @SuppressWarnings("synthetic-access")
    protected void nullCheck(final AccessibleObject member, final int i, final ParameterNameBinding expectedNameBinding, final Object result) {

        if (result == null && !isNullParamAllowed(member, i)) {
            throw new ParameterCannotBeNullException(i, member, expectedNameBinding.getName());
        }
    }

    /**
     * Checks to see if a null parameter is allowed in the given
     * constructor/field/method.  The default version allows null
     * if the target object is not a primitive type.
     * @param member constructor method or field
     * @param i parameter #.
     * @return true if the null parameter might be allowed.
     */
    protected boolean isNullParamAllowed(final AccessibleObject member, final int i) {
        return !(isPrimitiveArgument(member, i));
    }

    protected Annotation[] getBindings(final Annotation[][] annotationss) {
        Annotation[] retVal = new Annotation[annotationss.length];
        for (int i = 0; i < annotationss.length; i++) {
            Annotation[] annotations = annotationss[i];
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().getAnnotation(Bind.class) != null) {
                    retVal[i] = annotation;
                    break;
                }
            }
        }
        return retVal;
    }

    public static class ParameterCannotBeNullException extends PicoCompositionException {
        private final String name;
        private ParameterCannotBeNullException(final int ix, final AccessibleObject member, final String name) {
            super("Parameter " + ix + " of '" + member + "' named '" + name + "' cannot be null");
            this.name = name;
        }
        public String getParameterName() {
            return name;
        }
    }

}
