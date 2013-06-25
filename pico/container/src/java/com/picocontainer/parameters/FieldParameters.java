package com.picocontainer.parameters;

import com.picocontainer.Parameter;

public class FieldParameters extends AccessibleObjectParameterSet {

	public FieldParameters(final Class<?> targetType, final String name, final Parameter... params) {
		super(targetType, name, params);
	}

	public FieldParameters(final String name, final Parameter... params) {
		super(name, params);
	}

}
