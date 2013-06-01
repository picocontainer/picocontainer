package org.picocontainer.parameters;

import org.picocontainer.Parameter;

public class FieldParameters extends AccessibleObjectParameterSet {

	public FieldParameters(Class<?> targetType, String name, Parameter... params) {
		super(targetType, name, params);
	}

	public FieldParameters(String name, Parameter... params) {
		super(name, params);
	}

}
