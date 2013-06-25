/**
 * 
 */
package com.picocontainer.testmodules.moduleTwo;

import java.io.Serializable;

@SuppressWarnings("serial")
public class DefaultServiceTwo implements ServiceTwo, Serializable {

	private final String value;

	public DefaultServiceTwo(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
