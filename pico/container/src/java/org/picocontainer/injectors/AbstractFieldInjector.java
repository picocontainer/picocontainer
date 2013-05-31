/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.injectors;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.parameters.FieldParameters;

@SuppressWarnings("serial")
public abstract class AbstractFieldInjector<T> extends IterativeInjector<T> {

	public AbstractFieldInjector(final Object componentKey, final Class<?> componentImplementation,
			final ComponentMonitor monitor, final boolean useNames, boolean requireConsumptionOfallParameters, final FieldParameters... parameters)
			throws NotConcreteRegistrationException {
		super(componentKey, componentImplementation, monitor, useNames, requireConsumptionOfallParameters, parameters);
	}

	@Override
	final protected void unsatisfiedDependencies(final PicoContainer container,
			final Set<Type> unsatisfiableDependencyTypes, final List<AccessibleObject> unsatisfiableDependencyMembers) {
		final StringBuilder sb = new StringBuilder(this.getComponentImplementation().getName())
				.append(" has unsatisfied dependency for fields [");
		for (int i = 0; i < unsatisfiableDependencyMembers.size(); i++) {
			final AccessibleObject accessibleObject = unsatisfiableDependencyMembers.get(i);
			final Field m = (Field) accessibleObject;
			sb
					.append(" ")
					.append(m.getDeclaringClass().getName())
					.append(".")
					.append(m.getName())
					.append(" (field's type is ")
					.append(m.getType().getName())
					.append(") ");
		}
		final String container1 = container.toString();
		throw new UnsatisfiableDependenciesException(sb.toString() + "] from " + container1);
	}
	

	@Override
	protected boolean isAccessibleObjectEqualToParameterTarget(AccessibleObject testObject,
			Parameter currentParameter) {
		if (currentParameter.getTargetName() == null) {
			return false;
		}		
		
		if (!(testObject instanceof Field)) {
			throw new PicoCompositionException(testObject + " must be a field to use setter injection");
		}
		
		Field testField = (Field)testObject;			
		return testField.getName().equals(currentParameter.getTargetName());
	}
}