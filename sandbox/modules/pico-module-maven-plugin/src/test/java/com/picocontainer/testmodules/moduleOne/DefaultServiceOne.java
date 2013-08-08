/**
 * 
 */
package com.picocontainer.testmodules.moduleOne;

import java.io.Serializable;

@SuppressWarnings("serial")
public class DefaultServiceOne implements ServiceOne, Serializable {

	private final String value;

	public DefaultServiceOne(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
