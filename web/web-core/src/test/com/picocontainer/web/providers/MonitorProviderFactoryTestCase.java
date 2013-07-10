package com.picocontainer.web.providers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.servlet.ServletContext;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.picocontainer.ComponentMonitor;
import com.picocontainer.gems.monitors.Slf4jComponentMonitor;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.web.providers.defaults.NullMonitorProvider;
import com.picocontainer.web.providers.defaults.Slf4jMonitorProvider;

@RunWith(JMock.class)
public class MonitorProviderFactoryTestCase {

	private Mockery context = new JUnit4Mockery();

	private ServletContext servletContext;

	private MonitorProviderFactory providerFactory = null;

	private static ComponentMonitor mockMonitor;

	private static int factoryCount = 0;

	public static class MockComponentMonitorFactory implements MonitorProvider {

		public ComponentMonitor get() {
			factoryCount++;
			return mockMonitor;
		}

	}

	@Before
	public void setUp() throws Exception {
		factoryCount = 0;
		servletContext = context.mock(ServletContext.class);
		providerFactory = new MonitorProviderFactory();
		mockMonitor = context.mock(ComponentMonitor.class);
	}

	@After
	public void tearDown() throws Exception {
		servletContext = null;
	}

	@Test
	public void testSingleMonitorConstruction() {
		ComponentMonitor monitor = providerFactory.constructProvider(servletContext,
				NullMonitorProvider.class.getName());
		assertNotNull(monitor);
		assertTrue(monitor instanceof NullComponentMonitor);
	}

	@Test
	public void testWrappedMonitorConstruction() {
		ComponentMonitor monitor = providerFactory.constructProvider(servletContext,
				Slf4jMonitorProvider.class.getName() + "," + MockComponentMonitorFactory.class.getName());
		
		assertNotNull(monitor);
		assertTrue(monitor instanceof Slf4jComponentMonitor);
		assertEquals(1, factoryCount);
	}
	
	@Test(expected=ProviderSetupException.class)
	public void testDelegateMustBeLastOrExceptionThrown() {
		providerFactory.constructProvider(servletContext,
				NullMonitorProvider.class.getName() + "," + Slf4jMonitorProvider.class.getName());
		
	}
	

}
