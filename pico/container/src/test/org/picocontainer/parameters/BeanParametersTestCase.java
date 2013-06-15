package org.picocontainer.parameters;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BeanParametersTestCase {


	@Test
	public void testSetterificationOfPropertyName() {
		BeanParameters param = new BeanParameters("test");
		assertEquals("setTest", param.getName());
	}

	public void testSetterConversionIfPropertyIsOneCharacterLong() {
		BeanParameters param = new BeanParameters("i");
		assertEquals("setI", param.getName());
	}

}
