package com.picocontainer.web.providers;

import static org.junit.Assert.*;

import javax.servlet.ServletContext;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.picocontainer.ComponentFactory;
import com.picocontainer.PicoClassNotFoundException;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.behaviors.Locking;
import com.picocontainer.gems.jmx.JMXExposing;

@RunWith(JMock.class)
public class ComponentFactoryProvidersTestCase {

	private Mockery context = new JUnit4Mockery();

	private ServletContext servletContext;	
	
	@Before
	public void setUp() throws Exception {
		servletContext = context.mock(ServletContext.class);
	}

	@After
	public void tearDown() throws Exception {
		servletContext = null;
	}

	@Test
	public void testNullParameterValue() {
		ComponentFactoryProviders providers = new ComponentFactoryProviders();
		ComponentFactory[] createdProviders = providers.constructProvider(servletContext, null);
		assertNotNull(createdProviders);
		assertEquals(0, createdProviders.length);
	}
	
	@Test
	public void testOneProvider() {
		ComponentFactoryProviders providers = new ComponentFactoryProviders();
		ComponentFactory[] createdProviders = providers.constructProvider(servletContext, Caching.class.getName());
		assertEquals(1, createdProviders.length);
		assertEquals(Caching.class,createdProviders[0].getClass());
	}

	@Test
	public void testMultipleProviders() {
		ComponentFactoryProviders providers = new ComponentFactoryProviders();
		ComponentFactory[] createdProviders = providers.constructProvider(servletContext, Locking.class.getName() + ","+  Caching.class.getName() + "," + JMXExposing.class.getName());
		assertEquals(3, createdProviders.length);
		assertEquals(Locking.class,createdProviders[0].getClass());
		assertEquals(Caching.class,createdProviders[1].getClass());
		assertEquals(JMXExposing.class,createdProviders[2].getClass());
	}
	
	@Test(expected=PicoClassNotFoundException.class)
	public void testInvalidNameException() {
		new ComponentFactoryProviders().constructProvider(servletContext, Locking.class.getName() + ","+ "org.example.NonexistantClass");
	}
}
