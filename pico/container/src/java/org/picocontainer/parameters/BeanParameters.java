package org.picocontainer.parameters;

import org.picocontainer.Parameter;

/**
 * Parameter where the name is the bean property name, and this class automatically converts the name to
 * the setter method it is looking for.
 * @author Mike
 *
 */
@SuppressWarnings("serial")
public class BeanParameters extends MethodParameters {

	public BeanParameters(final String name, final Parameter... params) {
		super(convertPropertyNameToSetterName(name), params);
	}


	public BeanParameters(final Class<?> targetType, final String name, final Parameter... params) {
		super(targetType, convertPropertyNameToSetterName(name), params);
	}

	private static String convertPropertyNameToSetterName(final String name) {
		if (name == null) {
			throw new NullPointerException("name");
		}


		return "set" + Character.toUpperCase(name.charAt(0)) +
				(name.length() > 1 ? name.substring(1) : "");
	}

}
