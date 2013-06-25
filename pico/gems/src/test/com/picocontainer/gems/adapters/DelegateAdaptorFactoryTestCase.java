package com.picocontainer.gems.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.lifecycle.StartableLifecycleStrategy;
import com.picocontainer.monitors.ConsoleComponentMonitor;


public class DelegateAdaptorFactoryTestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateComponentAdapter() {

		HashMap<String,String> testMap = new HashMap<String,String>();
		testMap.put("a", "a value");
		testMap.put("b", "b value");

		ConsoleComponentMonitor monitor = new ConsoleComponentMonitor();
        DelegateInjectionType factory = new DelegateInjectionType();
        Properties getSizeProps = DelegateInjectionType.createDelegateProprties(testMap, "size");

        ComponentAdapter<Integer> ca = factory.createComponentAdapter(monitor, new StartableLifecycleStrategy(monitor),
        		getSizeProps, Integer.class, Integer.class, null, null, null);

        assertTrue(ca instanceof DelegateMethodAdapter);

        Integer result = ca.getComponentInstance(new DefaultPicoContainer(), null);
        assertNotNull(result);
        assertEquals(2, result.intValue());
	}

	@Test
	public void testWithPicoIntegration() {
		HashMap<String,String> testMap = new HashMap<String,String>();
		testMap.put("a", "a value");
		testMap.put("b", "b value");

		MutablePicoContainer pico = new PicoBuilder().withLifecycle().withCaching().build();

	}

}
