package org.picocontainer.parameters;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.util.Arrays;

import org.picocontainer.Parameter;

/***
 * Serialization Warning.  While using a specific target type is more of an edge case, it
 * isn't serializable and will have to be re-applied after deserialization.
 * @author Michael Rimov
 *
 */
@SuppressWarnings("serial")
public class AccessibleObjectParameterSet implements Serializable {


	public static final AccessibleObjectParameterSet[] EMPTY = new AccessibleObjectParameterSet[]{};
	
	/**
	 * Can't be serialized
	 */
	private transient Class<?> targetType;

	private final Parameter[] params;
	
	private final String name;
	

	public AccessibleObjectParameterSet(String name, Parameter... params) {
		this.name = name;
		this.params = params;
	}
	
	public AccessibleObjectParameterSet(Class<?> targetType, String name, Parameter... params) {
		this.targetType = targetType;
		this.name = name;
		this.params = params;
	}

	public Class<?> getTargetType() {
		return targetType;
	}

	public void setTargetType(Class<?> targetType) {
		this.targetType = targetType;
	}

	public Parameter[] getParams() {
		return params;
	}

	public String getName() {
		return name;
	}


	@Override
	public String toString() {
		return "AccessibleObjectParameterSet [targetType=" + targetType + ", params=" + Arrays.toString(params)
				+ ", name=" + name + "]";
	}

}