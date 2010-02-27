package org.picocontainer.gems.adapters;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoBuilder;
import org.picocontainer.lifecycle.StartableLifecycleStrategy;
import org.picocontainer.monitors.ConsoleComponentMonitor;


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
        DelegateAdaptorFactory factory = new DelegateAdaptorFactory();
        Properties getSizeProps = DelegateAdaptorFactory.createDelegateProprties(testMap, "size");
        
        ComponentAdapter<Integer> ca = factory.createComponentAdapter(monitor, new StartableLifecycleStrategy(monitor),
        		getSizeProps, Integer.class, Integer.class, Parameter.DEFAULT);
        
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
