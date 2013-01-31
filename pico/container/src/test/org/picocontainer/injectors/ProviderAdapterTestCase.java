package org.picocontainer.injectors;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProviderAdapterTestCase {
	
	public static class A {
		
	}
	
	public static class ProviderA implements javax.inject.Provider<A> {

		public A get() {
			return new A();
		}
		
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetComponentKey() {
		ProviderAdapter providerAdapter = new ProviderAdapter(new ProviderA());
		assertEquals(A.class, providerAdapter.getComponentKey());
	}

}
