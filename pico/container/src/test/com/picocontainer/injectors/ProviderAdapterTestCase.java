package com.picocontainer.injectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.googlecode.jtype.Generic;
import com.picocontainer.JTypeHelper;
import com.picocontainer.injectors.ProviderAdapter;

public class ProviderAdapterTestCase {

	public static class A {

	}

	public static class ProviderA implements javax.inject.Provider<A> {

		public A get() {
			return new A();
		}

	}

	@Test
	public void testGetComponentKey() {
		ProviderAdapter providerAdapter = new ProviderAdapter(new ProviderA());
		assertEquals(A.class, providerAdapter.getComponentKey());
	}

	@Test
	public void testImplementationType() {
		ProviderAdapter providerAdapter = new ProviderAdapter(new ProviderA());
		assertEquals(ProviderA.class, providerAdapter.getComponentImplementation());
		assertFalse(JTypeHelper.isAssignableFrom(Generic.get(A.class),  providerAdapter.getComponentImplementation()));
	}

}
