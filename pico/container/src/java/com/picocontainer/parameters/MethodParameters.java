package com.picocontainer.parameters;

import com.picocontainer.Parameter;

@SuppressWarnings("serial")
public class MethodParameters extends AccessibleObjectParameterSet {

	public MethodParameters(final String name, final Parameter... params) {
		super(name, params);
	}

	/**
	 * Allows you to specify a specific type that this parameter binds to, for example, allows
	 * @param targetType
	 * @param name
	 * @param params
	 */
	public MethodParameters(final Class<?> targetType, final String name, final Parameter... params) {
		super(targetType, name, params);
	}

}
