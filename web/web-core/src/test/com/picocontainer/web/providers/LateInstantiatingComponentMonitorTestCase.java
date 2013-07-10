package com.picocontainer.web.providers;

import static org.junit.Assert.*;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.picocontainer.ComponentMonitor;
import com.picocontainer.containers.TransientPicoContainer;

@RunWith(JMock.class)
public class LateInstantiatingComponentMonitorTestCase {
	
	private Mockery context = new JUnit4Mockery();
	
	private ComponentMonitor delegate = null;
	
	private HttpServletRequest servletRequest = null;

	@Before
	public void setUp() throws Exception {
		delegate = context.mock(ComponentMonitor.class);
		servletRequest = context.mock(HttpServletRequest.class);
		
	}

	@After
	public void tearDown() throws Exception {
		delegate = null;
	}

	@Test
	public void testNonClassOrNonStringKeyGoesStraightToDelegate() {
		final TransientPicoContainer tpc = new TransientPicoContainer();
		final Object key = new Object();
		context.checking(new Expectations() {{
			oneOf(delegate).noComponentFound(with(same(tpc)), with(same(key)));
			will(returnValue(null));
		}});
		LateInstantiatingComponentMonitor testMonitor = new LateInstantiatingComponentMonitor(delegate);
		assertNull(testMonitor.noComponentFound(tpc, key));
	}
	
	@Test
	public void testNonClassKeyReturnsNullIfNoDelegate() {
		final TransientPicoContainer tpc = new TransientPicoContainer();
		final Object key = new Object();
		LateInstantiatingComponentMonitor testMonitor = new LateInstantiatingComponentMonitor();
		assertNull(testMonitor.noComponentFound(tpc, key));
	}
	
	@Test
	public void testJavaLangStuffIsIgnoredAndGoesStraightToDelegate() {
		final TransientPicoContainer tpc = new TransientPicoContainer();
		final Class<Object> key = Object.class;
		context.checking(new Expectations() {{
			oneOf(delegate).noComponentFound(with(same(tpc)), with(same(key)));
			will(returnValue(null));
		}});
		LateInstantiatingComponentMonitor testMonitor = new LateInstantiatingComponentMonitor(delegate);
		assertNull(testMonitor.noComponentFound(tpc, key));		
	}
	
	@Test
	public void testJavaLangStuffReturnsNullIfNoDelegate() {
		final TransientPicoContainer tpc = new TransientPicoContainer();
		final Class<Object> key = Object.class;
		LateInstantiatingComponentMonitor testMonitor = new LateInstantiatingComponentMonitor();
		assertNull(testMonitor.noComponentFound(tpc, key));		
		
	}
	
	@Test
	@SuppressWarnings("rawtypes")
	public void testClassKeyIsInstantiatedUsingChildPicoContainer() {
		final TransientPicoContainer tpc = new TransientPicoContainer();
		final Class<HashMap> key = HashMap.class;
		LateInstantiatingComponentMonitor testMonitor = new LateInstantiatingComponentMonitor();
		HashMap result = (HashMap)testMonitor.noComponentFound(tpc, key);
		assertNotNull(result);
		//Verify no component was registered in parent.
		assertNull(tpc.getComponentAdapter(HashMap.class));
	}
	
	@Test
	public void testStringKeyIsInstantiatedUSingRequestFromString() {
		final TransientPicoContainer tpc = new TransientPicoContainer();
		final String key = "pico.testing";

		context.checking(new Expectations() {{
			oneOf(servletRequest).getParameter(key);
			will(returnValue("pico.result"));
		}});
		tpc.addComponent(HttpServletRequest.class, servletRequest);
		LateInstantiatingComponentMonitor testMonitor = new LateInstantiatingComponentMonitor();
		String resultKey = (String)testMonitor.noComponentFound(tpc, key);
		assertEquals("pico.result", resultKey);
	}
	
	public static abstract class TestAbstract {
		
	}
	
	@Test
	public void testMonitorWontTryToInstantiateAbstractClass() {
		final TransientPicoContainer tpc = new TransientPicoContainer();
		final Class<TestAbstract> key = TestAbstract.class;
		LateInstantiatingComponentMonitor testMonitor = new LateInstantiatingComponentMonitor();
		assertNull(testMonitor.noComponentFound(tpc, key));
	}
	
	@Test
	public void testMonitorWontTryToInstantiateInterface() {
		final TransientPicoContainer tpc = new TransientPicoContainer();
		final Class<TestInterface> key = TestInterface.class;
		LateInstantiatingComponentMonitor testMonitor = new LateInstantiatingComponentMonitor();
		assertNull(testMonitor.noComponentFound(tpc, key));
	}
	
	
	

}
