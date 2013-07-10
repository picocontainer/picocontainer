package com.picocontainer.parameters;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.DefaultPicoContainer.LateInstance;
import com.picocontainer.DefaultPicoContainer.KnowsContainerAdapter;
import com.picocontainer.Parameter.Resolver;
import com.picocontainer.containers.JSRPicoContainer;
public class BasicComponentParameterTestCase {

	private DefaultPicoContainer pico;
	
	@Before
	public void setUp() throws Exception {
		pico = new DefaultPicoContainer();
	}

	@After
	public void tearDown() throws Exception {
		pico = null;
	}
	
	public static class NeedsString  {
		
		public final String value;

		public NeedsString(String value) {
			this.value = value;
			
		}
	}

	@Test
	public void testLateInstanceWrappedInsideKnowsContainersDoesntCauseClassCastException() {
		
		//Hack: Make use of JSRPicoCOntainer calling private constructors.
		JSRPicoContainer temp = new JSRPicoContainer();
		temp.addComponent(LateInstance.class, LateInstance.class, new ConstantParameter(String.class), new ConstantParameter("Testing"));
		
		LateInstance adapter = temp.getComponent(LateInstance.class);
		
		ComponentAdapter<String> test = new KnowsContainerAdapter(adapter, pico);
		
		
		pico.addAdapter(test)
			.addComponent(NeedsString.class);
		
		ComponentAdapter<?> forAdapter = pico.getComponentAdapter(NeedsString.class);
		
		BasicComponentParameter param = new BasicComponentParameter();
		Resolver resolver = param.resolve(pico, forAdapter, null, String.class, null, false, null);
		assertTrue(resolver.isResolved());
		
		String result = (String) resolver.resolveInstance(null);
		assertEquals("Testing", result);
	}

}
