package com.picocontainer.web.struts2;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.opensymphony.xwork2.Action;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.containers.TransientPicoContainer;

@RunWith(JMock.class)
public class StrutsActionInstantiatingComponentMonitorTestCase {
	
	private Mockery context = new JUnit4Mockery();
	
	private ComponentMonitor delegate = null;

	@Before
	public void setUp() throws Exception {
		delegate = context.mock(ComponentMonitor.class);
		
	}

	@After
	public void tearDown() throws Exception {
		delegate = null;
	}

	@Test
	public void testNonActionClassesGiveDelegateAChanceBeforeDefaultConstructor() {
		final TransientPicoContainer tpc = new TransientPicoContainer();
		final Class<String> key = String.class;
		final String returnValue = "This is a test";
		context.checking(new Expectations() {{
			oneOf(delegate).noComponentFound(with(same(tpc)), with(same(key)));
			will(returnValue(returnValue));
		}});
		StrutsActionInstantiatingComponentMonitor testMonitor = new StrutsActionInstantiatingComponentMonitor(delegate);
		assertSame(returnValue, testMonitor.noComponentFound(tpc, key));
	}
	
	@Test
	public void testNonActionClassesWillUseDefaultConstructorIfDelegateStillReturnsNull() {
		final TransientPicoContainer tpc = new TransientPicoContainer();
		final Class<String> key = String.class;
		context.checking(new Expectations() {{
			oneOf(delegate).noComponentFound(with(same(tpc)), with(same(key)));
			will(returnValue(null));
		}});
		StrutsActionInstantiatingComponentMonitor testMonitor = new StrutsActionInstantiatingComponentMonitor(delegate);
		String result = (String)testMonitor.noComponentFound(tpc, key);
		assertNotNull(result);
		assertTrue(result.isEmpty());
		
	}
	
	@Test
	public void testWillUsePicoContainerToHandleActions() {
		final TransientPicoContainer tpc = new TransientPicoContainer();
		tpc.addComponent(String.class, "This is a test");
		
		final Class<TestAction> key = TestAction.class;

		//No delegate call here.
		
		StrutsActionInstantiatingComponentMonitor testMonitor = new StrutsActionInstantiatingComponentMonitor(delegate);
		TestAction result = (TestAction)testMonitor.noComponentFound(tpc, key);
		assertNotNull(result);
		
		//Verify no registration in root pico took place
		//Todo can't do that because of actions -> PWR stuff
		//assertNull(tpc.getComponentAdapter(TestAction.class));
	}
	
	
	@Test
	public void testWillUsePicoContainerToHandleResults() {
		final TransientPicoContainer tpc = new TransientPicoContainer();
		final Class<TestResult> key = TestResult.class;

		//No delegate call here.
		
		StrutsActionInstantiatingComponentMonitor testMonitor = new StrutsActionInstantiatingComponentMonitor(delegate);
		TestResult result = (TestResult)testMonitor.noComponentFound(tpc, key);
		assertNotNull(result);		

		//Verify no registration in root pico took place
		//Todo can't do that because of actions -> PWR stuff
		//assertNull(tpc.getComponentAdapter(TestResult.class));
	}	
	
	
	public abstract static class AbstractAction implements Action {
		
	}
	
	@Test
	public void testWillNotAttemptToInstantiateAbstractActions() {
		final TransientPicoContainer tpc = new TransientPicoContainer();
		final Class<AbstractAction> key = AbstractAction.class;

		context.checking(new Expectations() {{
			oneOf(delegate).noComponentFound(with(same(tpc)), with(same(key)));
			will(returnValue(null));
		}});
		
		StrutsActionInstantiatingComponentMonitor testMonitor = new StrutsActionInstantiatingComponentMonitor(delegate);
		assertNull(testMonitor.noComponentFound(tpc, key));
	}

	

}
