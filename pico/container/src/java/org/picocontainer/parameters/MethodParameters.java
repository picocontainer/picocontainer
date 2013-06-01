package org.picocontainer.parameters;

import org.picocontainer.Parameter;

@SuppressWarnings("serial")
public class MethodParameters extends AccessibleObjectParameterSet {

	public MethodParameters(String name, Parameter... params) {
		super(name, params);
	}

	/**
	 * Allows you to specify a specific type that this parameter binds to, for example, allows
	 * @param targetType
	 * @param name
	 * @param params
	 */
	public MethodParameters(Class<?> targetType, String name, Parameter... params) {
		super(targetType, name, params);
	}

}
