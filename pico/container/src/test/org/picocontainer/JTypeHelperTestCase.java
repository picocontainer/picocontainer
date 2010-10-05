package org.picocontainer;


import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.adapters.SimpleNamedBindingAnnotationTestCase.Apple;

import com.googlecode.jtype.Generic;

public class JTypeHelperTestCase {

	public interface HttpServletRequest {
		
	}
	
	@Test
	public void testIsAssignableFromForParameterizedInterfaces() {
		//Used to cause NPE.
		assertFalse(JTypeHelper.isAssignableFrom(new Generic<List<HttpServletRequest>>() {}, Apple.class));
	}

}
