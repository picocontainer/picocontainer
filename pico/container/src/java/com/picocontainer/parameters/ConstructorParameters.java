/**
 *
 */
package com.picocontainer.parameters;

import java.util.Arrays;

import com.picocontainer.Parameter;

/**
 * @author Mike
 *
 */
@SuppressWarnings("serial")
public class ConstructorParameters extends AccessibleObjectParameterSet {

	/**
	 * Reference this in your constructor parameters if you wish the no-arg constructor to be used.
	 */
	public static final ConstructorParameters NO_ARG_CONSTRUCTOR = new ConstructorParameters(new Parameter[0]);


	/**
	 * Constructs constructor parameters with the given component and constant parameters.
	 * @param params the parameters in constructor parmeter order.
	 */
	public ConstructorParameters(final Parameter[] params) {
		super(null, params);
	}


	public ConstructorParameters(final Parameter parameter) {
		super(null, new Parameter[] {parameter});
	}

	public ConstructorParameters() {
		this((Parameter[])null);
	}


	@Override
	public String toString() {
		return "ConstructorParameters Parameters = " + Arrays.deepToString(this.getParams());
	}


}
