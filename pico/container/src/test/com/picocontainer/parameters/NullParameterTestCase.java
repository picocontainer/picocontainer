package com.picocontainer.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.Parameter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.Parameter.Resolver;
import com.picocontainer.parameters.NullParameter;
import com.picocontainer.visitors.TraversalCheckingVisitor;

public class NullParameterTestCase {

	private NullParameter instance;

	@Before
	public void setUp() throws Exception {
		instance = NullParameter.INSTANCE;
	}

	@After
	public void tearDown() throws Exception {
		instance = null;
	}

	@Test
	public void testAccept() {
		final List<Parameter> parametersVisited = new ArrayList<Parameter>();
		TraversalCheckingVisitor visitor = new TraversalCheckingVisitor() {
			@Override
			public void visitParameter(final Parameter parameter) {
				parametersVisited.add(parameter);
			}

		};
		instance.accept(visitor);
		assertEquals(1, parametersVisited.size());
		assertEquals(instance, parametersVisited.get(0));
	}

	@Test
	public void testResolve() {
		//Doesn't realy matter what's passed in, it'll get the same result :)
		Resolver result = instance.resolve(null, null, null, String.class, null, false, null);

		assertNotNull(result);
		assertTrue(result.isResolved());
		assertNull(result.resolveInstance(ComponentAdapter.NOTHING.class));

		result = instance.resolve(null, null, null, Void.TYPE, null, false, null);
		assertNotNull(result);
		assertFalse(result.isResolved());
	}

	@Test
	public void testVerify() {
		instance.verify(null, null, String.class, null, false, null);

		try {

			instance.verify(null, null, Integer.TYPE, null, false, null);
			fail("Should have thrown PicoCompositionException  when verifying primitive types");
		} catch (PicoCompositionException ex) {
			assertNotNull(ex.getMessage());
		}
	}

}
