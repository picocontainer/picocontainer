package org.picocontainer.containers;

import static org.junit.Assert.*;

import javax.inject.Named;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

public class JSRPicoContainerTestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	public static class A {
		
	}
	
	@Named("test")
	public static class B {
		
	}
	
	@SomeQualifier
	public static class C {
		
	}
	
	@Test
	public void testJSR330KeyDetermination() {
		MutablePicoContainer mpc = new JSRPicoContainer(new PicoBuilder().withCaching().withLifecycle().build());
		
		mpc.addComponent(A.class)
			.addComponent(B.class)
			.addComponent(C.class);
		
		assertNotNull(mpc.getComponentAdapter(A.class));
		assertNotNull(mpc.getComponentAdapter("test"));
		assertNull(mpc.getComponentAdapter(B.class));
		assertNull(mpc.getComponentAdapter(C.class));
		assertNotNull(mpc.getComponentAdapter(SomeQualifier.class.getName()));
	}

}
