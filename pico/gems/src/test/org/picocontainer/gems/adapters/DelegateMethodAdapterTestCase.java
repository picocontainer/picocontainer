package org.picocontainer.gems.adapters;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.gems.util.DelegateMethod;


public class DelegateMethodAdapterTestCase {

	/**
	 * While this test cases tests the component adapter's functionality, it is hardly indicative of the power
	 * with which this adapter is capable.  Please read the javadocs for DelegateMethodAdapter.
	 * @see org.picocontainer.gems.adapters.DelegateMethodAdapter
	 */
	@Test
	public void testDelegateInvocation() {
		
		MutablePicoContainer pico = new PicoBuilder().withCaching().withLifecycle().build();
		
		Map<String,String> testMap = new HashMap<String,String>();
		testMap.put("A", "A Value");
		testMap.put("B", "B Value");
		
		DelegateMethod method = new DelegateMethod(Map.class, "get", "A");
		DelegateMethodAdapter adapter = new DelegateMethodAdapter(String.class, testMap, method);
		pico.addAdapter(adapter);
		
		String result = pico.getComponent(String.class);
		assertEquals("A Value", result);		
	}
}
